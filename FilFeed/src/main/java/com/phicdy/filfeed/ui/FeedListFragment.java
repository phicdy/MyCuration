package com.phicdy.filfeed.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.phicdy.filfeed.R;
import com.phicdy.filfeed.db.DatabaseAdapter;
import com.phicdy.filfeed.rss.Feed;
import com.phicdy.filfeed.rss.UnreadCountManager;
import com.phicdy.filfeed.task.NetworkTaskManager;

import java.io.File;
import java.util.ArrayList;

public class FeedListFragment extends Fragment {

    private TextView tvAllUnreadArticleCount;
    private LinearLayout allUnread;
    private PullToRefreshListView feedsListView;
    private RssFeedListAdapter rssFeedListAdapter;
    private OnFeedListFragmentListener mListener;

    private ArrayList<Feed> feeds = new ArrayList<>();
    private ArrayList<Feed> allFeeds = new ArrayList<>();
    private ArrayList<Feed> hideList = new ArrayList<>();
    private DatabaseAdapter dbAdapter;
    private UnreadCountManager unreadManager;

    // Manage hide feed status
    private boolean isHided = true;

    private int numOfAllFeeds = 0;

    private static final String LOG_TAG = "FilFeed.FeedList";

    public static FeedListFragment newInstance() {
        return new FeedListFragment();
    }

    public FeedListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        unreadManager = UnreadCountManager.getInstance(getActivity());
        dbAdapter = DatabaseAdapter.getInstance(getActivity());
        allFeeds = dbAdapter.getAllFeedsWithNumOfUnreadArticles();
        // For show/hide
        if (allFeeds.size() != 0) {
            addShowHideLine(allFeeds);
        }
        generateHidedFeedList();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
        if (NetworkTaskManager.getInstance(getActivity()).isUpdatingFeed()) {
            feedsListView.setRefreshing(true);
            updateProgress();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (NetworkTaskManager.getInstance(getActivity()).isUpdatingFeed()) {
            feedsListView.onRefreshComplete();
        }
    }

    private void setAllListener() {
        // When an feed selected, display unread articles in the feed
        feedsListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        mListener.onListClicked(position-1);
                    }

                });
        feedsListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                mListener.onRefreshList();
            }
        });
        allUnread = (LinearLayout)getActivity().findViewById(R.id.ll_all_unread);
        allUnread.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.onAllUnreadClicked();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed_list, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFeedListFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        feedsListView = (PullToRefreshListView) getActivity().findViewById(R.id.feedList);
        TextView emptyView = (TextView)getActivity().findViewById(R.id.emptyView);
        feedsListView.setEmptyView(emptyView);
        getActivity().registerForContextMenu(feedsListView.getRefreshableView());
        setAllListener();

        refreshList();
        numOfAllFeeds = dbAdapter.getNumOfFeeds();

        tvAllUnreadArticleCount = (TextView)getActivity().findViewById(R.id.allUnreadCount);
        if (numOfAllFeeds == 0) {
            allUnread.setVisibility(View.GONE);
        }else {
            updateAllUnreadArticlesCount();
        }

        // Set ListView
        rssFeedListAdapter = new RssFeedListAdapter(feeds, getActivity());
        feedsListView.setAdapter(rssFeedListAdapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void refreshList() {
        generateHidedFeedList();
        if (isHided) {
            rssFeedListAdapter = new RssFeedListAdapter(feeds, getActivity());
        }else {
            rssFeedListAdapter = new RssFeedListAdapter(allFeeds, getActivity());
        }
        feedsListView.setAdapter(rssFeedListAdapter);
        rssFeedListAdapter.notifyDataSetChanged();
        updateAllUnreadArticlesCount();
    }

    private void generateHidedFeedList() {
        if (dbAdapter == null) {
            dbAdapter = DatabaseAdapter.getInstance(getActivity());
        }
        long start = System.currentTimeMillis();

        if (allFeeds.isEmpty()) {
            return;
        }
        feeds = (ArrayList<Feed>)allFeeds.clone();
        hideList = new ArrayList<>();
        for (Feed feed : allFeeds) {
            int numOfUnreadArticles = unreadManager.getUnreadCount(feed.getId());
            if (numOfUnreadArticles == 0) {
                hideList.add(feed);
            }
        }
        if((allFeeds.size()-1) != hideList.size() && hideList.size() != 0) {
            for(Feed feed : hideList) {
                feeds.remove(feed);
            }
        }
    }

    public void onRefreshComplete() {
        feedsListView.onRefreshComplete();
        updateProgress();
    }

    public void addFeed(Feed newFeed) {
        deleteShowHideLineIfNeeded();
        if (newFeed.getUnreadAriticlesCount() > 0) {
            feeds.add(newFeed);
            addShowHideLine(feeds);
        }
        allFeeds.add(newFeed);
        addShowHideLine(allFeeds);
        unreadManager.addFeed(newFeed);
        rssFeedListAdapter.notifyDataSetChanged();
    }

    private void deleteShowHideLineIfNeeded() {
        if (feeds != null && feeds.size() > 0) {
            int lastIndex = feeds.size() - 1;
            if (feeds.get(lastIndex).getId() == Feed.DEFAULT_FEED_ID) {
                feeds.remove(lastIndex);
            }
        }
        if (allFeeds != null && allFeeds.size() > 0) {
            int lastIndex = allFeeds.size() - 1;
            if (allFeeds.get(lastIndex).getId() == Feed.DEFAULT_FEED_ID) {
                allFeeds.remove(lastIndex);
            }
        }
    }

    private void addShowHideLine(ArrayList<Feed> feeds) {
        feeds.add(new Feed());
    }

    public void removeFeedAtPosition(int position) {
        if (isHided) {
            Feed deletedFeed = feeds.get(position);
            dbAdapter.deleteFeed(deletedFeed.getId());
            unreadManager.deleteFeed(deletedFeed.getId());
            feeds.remove(position);
            for (int i = 0;i < allFeeds.size();i++) {
                if (allFeeds.get(i).getId() == deletedFeed.getId()) {
                    allFeeds.remove(i);
                }
            }
        }else {
            Feed deletedFeed = allFeeds.get(position);
            dbAdapter.deleteFeed(deletedFeed.getId());
            unreadManager.deleteFeed(deletedFeed.getId());
            allFeeds.remove(position);
            for (int i = 0;i < feeds.size();i++) {
                if (feeds.get(i).getId() == deletedFeed.getId()) {
                    feeds.remove(i);
                }
            }
        }
        rssFeedListAdapter.notifyDataSetChanged();
    }

    public void updateProgress() {
        NetworkTaskManager networkTaskManager = NetworkTaskManager.getInstance(getActivity());
        int updatedFeed = numOfAllFeeds - networkTaskManager.getFeedRequestCountInQueue();
        if ((updatedFeed == numOfAllFeeds) && !networkTaskManager.isUpdatingFeed()) {
            updatedFeed = 0;
        }
        if (feedsListView != null ){
            feedsListView.getLoadingLayoutProxy()
                    .setRefreshingLabel(getString(R.string.loading) + "(" + updatedFeed + "/" + numOfAllFeeds + ")");
        }
    }

    public int getFeedIdAtPosition (int position) {
        if (position < 0) {
            return -1;
        }

        if (isHided) {
            if (feeds == null || position > feeds.size()-1) {
                return -1;
            }
            return feeds.get(position).getId();
        }else {
            if (allFeeds == null || position > allFeeds.size()-1) {
                return -1;
            }
        }
        return allFeeds.get(position).getId();
    }

    public String getFeedTitleAtPosition (int position) {
        if (position < 0 || feeds == null || position > feeds.size()-1) {
            return null;
        }
        if (isHided) {
            feeds.get(position).getTitle();
        }
        return allFeeds.get(position).getTitle();
    }


    public String getFeedUrlAtPosition (int position) {
        if (position < 0) {
            return null;
        }
        if (isHided) {
            if (feeds == null || position > feeds.size()-1) {
                return null;
            }
            return feeds.get(position).getUrl();
        }else {
            if (allFeeds == null || position > allFeeds.size()-1) {
                return null;
            }
            return allFeeds.get(position).getUrl();
        }

    }

    public boolean changeHideStatus() {
        if (isHided) {
            isHided = false;
            rssFeedListAdapter = new RssFeedListAdapter(allFeeds, getActivity());
        }else {
            isHided = true;
            rssFeedListAdapter = new RssFeedListAdapter(feeds, getActivity());
        }

        // Set ListView
        feedsListView.setAdapter(rssFeedListAdapter);
        return isHided;
    }

    public void updateAllUnreadArticlesCount() {
        if (tvAllUnreadArticleCount != null) {
            tvAllUnreadArticleCount.setText(String.valueOf(unreadManager.getTotal()));
        }
    }

    public void updateFeedTitle(int feedId, String newTitle) {
        for (Feed feed : allFeeds) {
            if (feed.getId() == feedId) {
                feed.setTitle(newTitle);
                break;
            }
        }
        for (Feed feed : feeds) {
            if (feed.getId() == feedId) {
                feed.setTitle(newTitle);
                break;
            }
        }
        rssFeedListAdapter.notifyDataSetChanged();
    }

    public interface OnFeedListFragmentListener {
        public void onListClicked(int position);
        public void onRefreshList();
        public void onAllUnreadClicked();
    }

    /**
     *
     * @author kyamaguchi Display RSS Feeds List
     */
    class RssFeedListAdapter extends ArrayAdapter<Feed> {
        public RssFeedListAdapter(ArrayList<Feed> feeds, Context context) {
            super(context, R.layout.feeds_list, feeds);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
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

            String iconPath = feed.getIconPath();
            if (isHided && ((position+1) == feeds.size())) {
                holder.feedIcon.setVisibility(View.INVISIBLE);
                holder.feedCount.setVisibility(View.GONE);
                holder.feedTitle.setText(R.string.show_all_feeds);
            } else if (!isHided && ((position+1) == allFeeds.size())) {
                holder.feedIcon.setVisibility(View.INVISIBLE);
                holder.feedCount.setVisibility(View.GONE);
                holder.feedTitle.setText(R.string.hide_feeds);
            }else if(iconPath == null || iconPath.equals(Feed.DEDAULT_ICON_PATH)) {
                holder.feedIcon.setImageResource(R.drawable.no_icon);
                holder.feedTitle.setText(feed.getTitle());
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
            holder.feedCount.setText(String.valueOf(unreadManager.getUnreadCount(feed.getId())));

            return (row);
        }

        private class ViewHolder {
            ImageView feedIcon;
            TextView feedTitle;
            TextView feedCount;
        }
    }

}
