package com.phicdy.mycuration.view.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.presenter.FeedListPresenter;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.phicdy.mycuration.task.NetworkTaskManager;
import com.phicdy.mycuration.util.PreferenceHelper;
import com.phicdy.mycuration.view.FeedListView;

import java.io.File;
import java.util.ArrayList;

public class FeedListFragment extends Fragment implements FeedListView {

    private FeedListPresenter presenter;
    private TextView tvAllUnreadArticleCount;
    private LinearLayout allUnread;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView feedsListView;
    private TextView emptyView;

    private RssFeedListAdapter rssFeedListAdapter;
    private OnFeedListFragmentListener mListener;

    private DatabaseAdapter dbAdapter;
    private UnreadCountManager unreadManager;
    private BroadcastReceiver receiver;

    private static final int DELETE_FEED_MENU_ID = 1000;
    private static final int EDIT_FEED_TITLE_MENU_ID = 1001;

    private static final String FEEDS_KEY = "feedsKey";
    private static final String ALL_FEEDS_KEY = "allFeedsKey";

    private static final String LOG_TAG = "FilFeed.FeedList";

    public FeedListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        unreadManager = UnreadCountManager.getInstance();
        dbAdapter = DatabaseAdapter.getInstance();
        NetworkTaskManager networkTaskManager = NetworkTaskManager.INSTANCE;
        setRetainInstance(true);
        PreferenceHelper helper = PreferenceHelper.INSTANCE;
        presenter = new FeedListPresenter(helper.getAutoUpdateInMainUi(),
                dbAdapter, networkTaskManager, unreadManager);
        presenter.setView(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putParcelableArrayList(FEEDS_KEY, presenter.getFeeds());
            outState.putParcelableArrayList(ALL_FEEDS_KEY, presenter.getAllFeeds());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setBroadCastReceiver();
        presenter.resume();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_FEED_MENU_ID, 0, R.string.delete_rss);
        menu.add(0, EDIT_FEED_TITLE_MENU_ID, 1, R.string.edit_rss_title);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();

        switch (item.getItemId()) {
            case DELETE_FEED_MENU_ID:
                presenter.onDeleteFeedMenuClicked(info.position);
                return true;
            case EDIT_FEED_TITLE_MENU_ID:
                presenter.onEditFeedMenuClicked(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void showEditTitleDialog(final int position, @NonNull String feedTitle) {
        final View addView = View.inflate(getActivity(), R.layout.edit_feed_title, null);
        EditText editTitleView = (EditText) addView.findViewById(R.id.editFeedTitle);
        editTitleView.setText(feedTitle);

        new AlertDialog.Builder(getActivity())
            .setTitle(R.string.edit_rss_title)
            .setView(addView)
            .setPositiveButton(R.string.save,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editTitleView = (EditText) addView
                                .findViewById(R.id.editFeedTitle);
                        String newTitle = editTitleView.getText().toString();
                        presenter.onEditFeedOkButtonClicked(newTitle, position);
                    }

                }).setNegativeButton(R.string.cancel, null).show();
    }

    @Override
    public void setRefreshing(boolean doScroll) {
        swipeRefreshLayout.setRefreshing(doScroll);
    }

    @Override
    public void init(@NonNull ArrayList<Feed> feeds) {
        if (feeds.size() == 0) emptyView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        if (getActivity() != null) {
            rssFeedListAdapter = new RssFeedListAdapter(feeds, getActivity());
            feedsListView.setAdapter(rssFeedListAdapter);
            rssFeedListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setTotalUnreadCount(int count) {
        if (tvAllUnreadArticleCount != null) {
            tvAllUnreadArticleCount.setText(String.valueOf(count));
        }
    }

    @Override
    public void onRefreshCompleted() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showEditFeedTitleEmptyErrorToast() {
        Toast.makeText(getActivity(), getString(R.string.empty_title), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showEditFeedFailToast() {
        Toast.makeText(getActivity(), getString(R.string.edit_rss_title_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showEditFeedSuccessToast() {
        Toast.makeText(getActivity(), getString(R.string.edit_rss_title_success), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showDeleteSuccessToast() {
        Toast.makeText(getActivity(), getString(R.string.finish_delete_rss_success), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showDeleteFailToast() {
        Toast.makeText(getActivity(), getString(R.string.finish_delete_rss_fail), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showAddFeedSuccessToast() {
        Toast.makeText(getActivity(), R.string.add_rss_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showGenericAddFeedErrorToast() {
        Toast.makeText(getActivity(), R.string.add_rss_error_generic, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showInvalidUrlAddFeedErrorToast() {
        Toast.makeText(getActivity(), R.string.add_rss_error_invalid_url, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notifyDataSetChanged() {
        rssFeedListAdapter.notifyDataSetChanged();
    }

    @Override
    public void showAllUnreadView() {
        allUnread.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideAllUnreadView() {
        allUnread.setVisibility(View.GONE);
    }

    @Override
    public void showDeleteFeedAlertDialog(final int position) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_rss_alert)
                .setPositiveButton(R.string.delete,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                presenter.onDeleteOkButtonClicked(position);
                            }
                        }).setNegativeButton(R.string.cancel, null).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.pause();
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private void setAllListener() {
        // When an feed selected, display unread articles in the feed
        feedsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                presenter.onFeedListClicked(position, mListener);
            }

        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.onRefresh();
            }
        });
        allUnread.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.onAllUnreadClicked();
            }
        });
    }

    private void setBroadCastReceiver() {
        // receive num of unread articles from Update Task
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // Set num of unread articles and update UI
                String action = intent.getAction();
                if (action.equals(NetworkTaskManager.FINISH_UPDATE_ACTION)) {
                    Log.d(LOG_TAG, "onReceive");
                    presenter.onFinishUpdate();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(NetworkTaskManager.FINISH_UPDATE_ACTION);
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed_list, container, false);
        feedsListView = (ListView) view.findViewById(R.id.feedList);
        emptyView = (TextView) view.findViewById(R.id.emptyView);
        feedsListView.setEmptyView(emptyView);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_container);
        tvAllUnreadArticleCount = (TextView) view.findViewById(R.id.allUnreadCount);
        allUnread = (LinearLayout) view.findViewById(R.id.ll_all_unread);
        registerForContextMenu(feedsListView);
        setAllListener();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFeedListFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter.activityCreated();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFeedListFragmentListener {
        void onListClicked(int feedId);
        void onAllUnreadClicked();
    }

    /**
     *
     * @author phicdy Display RSS Feeds List
     */
    private class RssFeedListAdapter extends ArrayAdapter<Feed> {
        RssFeedListAdapter(ArrayList<Feed> feeds, Context context) {
            super(context, R.layout.feeds_list, feeds);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;

            // Use contentView and setup ViewHolder
            View row = convertView;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.feeds_list, parent, false);
                holder = new ViewHolder();
                holder.feedIcon = (ImageView) row.findViewById(R.id.feedIcon);
                holder.feedTitle = (TextView) row.findViewById(R.id.feedTitle);
                holder.feedCount = (TextView) row.findViewById(R.id.feedCount);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            Feed feed = this.getItem(position);
            String iconPath = null;
            if (feed != null) {
                iconPath = feed.getIconPath();
            }
            holder.feedIcon.setVisibility(View.VISIBLE);
            holder.feedCount.setVisibility(View.VISIBLE);
            if (presenter.isAllRssShowView(position+1)) {
                holder.feedIcon.setVisibility(View.INVISIBLE);
                holder.feedCount.setVisibility(View.GONE);
                holder.feedTitle.setText(R.string.show_all_rsses);
            } else if (presenter.isHideReadRssView(position+1)) {
                holder.feedIcon.setVisibility(View.INVISIBLE);
                holder.feedCount.setVisibility(View.GONE);
                holder.feedTitle.setText(R.string.hide_rsses);
            }else if(iconPath == null || iconPath.equals(Feed.DEDAULT_ICON_PATH)) {
                holder.feedIcon.setImageResource(R.drawable.no_icon);
                if (feed != null) {
                    holder.feedTitle.setText(feed.getTitle());
                }
            }else {
                File file = new File(iconPath);
                if (file.exists()) {
                    Bitmap bmp = BitmapFactory.decodeFile(file.getPath());
                    holder.feedIcon.setImageBitmap(bmp);
                } else {
                    dbAdapter.saveIconPath(feed.getSiteUrl(), Feed.DEDAULT_ICON_PATH);
                }
                holder.feedTitle.setText(feed.getTitle());
            }

            // set RSS Feed unread article count
            if (feed != null) {
                holder.feedCount.setText(String.valueOf(unreadManager.getUnreadCount(feed.getId())));
            }

            return row;
        }

        private class ViewHolder {
            ImageView feedIcon;
            TextView feedTitle;
            TextView feedCount;
        }
    }

}
