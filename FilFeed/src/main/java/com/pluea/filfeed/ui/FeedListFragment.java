package com.pluea.filfeed.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.pluea.filfeed.R;
import com.pluea.filfeed.db.DatabaseAdapter;
import com.pluea.filfeed.rss.Feed;
import com.pluea.filfeed.task.UpdateTaskManager;

import java.io.File;
import java.util.ArrayList;

public class FeedListFragment extends Fragment {

    private PullToRefreshListView feedsListView;
    private RssFeedListAdapter rssFeedListAdapter;
    private OnFeedListFragmentListener mListener;

    private ArrayList<Feed> feeds = new ArrayList<>();
    private ArrayList<Feed> allFeeds = new ArrayList<>();
    private ArrayList<Feed> hideList = new ArrayList<>();
    private DatabaseAdapter dbAdapter;

    // Manage hide feed status
    private boolean isHided = true;

    private int numOfAllFeeds = 0;

    public static FeedListFragment newInstance() {
        return new FeedListFragment();
    }

    public FeedListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbAdapter = DatabaseAdapter.getInstance(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        if (UpdateTaskManager.getInstance(getActivity()).isUpdatingFeed()) {
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

        Log.d("Time", "onActivityCreated");
        refreshList();
        numOfAllFeeds = dbAdapter.getNumOfFeeds();

        // Set ListView
        rssFeedListAdapter = new RssFeedListAdapter(feeds, getActivity());
        feedsListView.setAdapter(rssFeedListAdapter);
        if (UpdateTaskManager.getInstance(getActivity()).isUpdatingFeed()) {
            feedsListView.setRefreshing(true);
            updateProgress();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void refreshList() {
        if(allFeeds.size() == hideList.size() || hideList.size() == 0) {
            mListener.setShowAllFeedsGone();
        }else {
            mListener.setShowAllFeedsVisible();
        }

        rssFeedListAdapter = new RssFeedListAdapter(feeds, getActivity());
        feedsListView.setAdapter(rssFeedListAdapter);
        rssFeedListAdapter.notifyDataSetChanged();
    }

    public void setAllFeeds(ArrayList<Feed> allFeeds) {
        this.allFeeds = allFeeds;
        generateHidedFeedList();
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
            int numOfUnreadArticles = feed.getUnreadAriticlesCount();
            if (numOfUnreadArticles == 0) {
                hideList.add(feed);
            }
        }
        if(allFeeds.size() != hideList.size() && hideList.size() != 0) {
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
        feeds.add(newFeed);
        rssFeedListAdapter.notifyDataSetChanged();
    }

    public void removeFeedAtPosition(int position) {
        dbAdapter.deleteFeed(feeds.get(position).getId());
        feeds.remove(position);
        rssFeedListAdapter.notifyDataSetChanged();
    }

    public void updateProgress() {
        UpdateTaskManager updateTaskManager = UpdateTaskManager.getInstance(getActivity());
        int updatedFeed = numOfAllFeeds - updateTaskManager.getFeedRequestCountInQueue();
        if ((updatedFeed == numOfAllFeeds) && !updateTaskManager.isUpdatingFeed()) {
            updatedFeed = 0;
        }
        feedsListView.getLoadingLayoutProxy()
                .setRefreshingLabel(getString(R.string.loading) + "(" + updatedFeed + "/" + numOfAllFeeds + ")");
    }

    public int getFeedIdAtPosition (int position) {
        if (position < 0 || feeds == null || position > feeds.size()-1) {
            return -1;
        }
        return feeds.get(position).getId();
    }

    public String getFeedTitleAtPosition (int position) {
        if (position < 0 || feeds == null || position > feeds.size()-1) {
            return null;
        }
        return feeds.get(position).getTitle();
    }


    public String getFeedUrlAtPosition (int position) {
        if (position < 0 || feeds == null || position > feeds.size()-1) {
            return null;
        }
        return feeds.get(position).getUrl();
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

    public interface OnFeedListFragmentListener {
        // TODO: Update argument type and name
        public void onListClicked(int position);
        public void onRefreshList();
        public void setShowAllFeedsGone();
        public void setShowAllFeedsVisible();
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
                holder.feedIcon = (ImageView)row.findViewById(R.id.feedIcon);
                holder.feedTitle = (TextView) row.findViewById(R.id.feedTitle);
                holder.feedCount = (TextView) row.findViewById(R.id.feedCount);
                row.setTag(holder);
            }else {
                holder = (ViewHolder)row.getTag();
            }

            Feed feed = this.getItem(position);

            String iconPath = feed.getIconPath();
            if(iconPath == null || iconPath.equals(Feed.DEDAULT_ICON_PATH)) {
                holder.feedIcon.setImageResource(R.drawable.no_icon);
            }else {
                File file = new File(iconPath);
                if (file.exists()) {
                    Bitmap bmp = BitmapFactory.decodeFile(file.getPath());
                    holder.feedIcon.setImageBitmap(bmp);
                }
            }

            // set RSS Feed title
            holder.feedTitle.setText(feed.getTitle());

            // set RSS Feed unread article count
            holder.feedCount.setText(String.valueOf(feed.getUnreadAriticlesCount()));

            return (row);
        }

        private class ViewHolder {
            ImageView feedIcon;
            TextView feedTitle;
            TextView feedCount;
        }
    }

}
