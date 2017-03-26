package com.phicdy.mycuration.presenter;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.phicdy.mycuration.task.NetworkTaskManager;
import com.phicdy.mycuration.view.fragment.FeedListFragment;
import com.phicdy.mycuration.view.FeedListView;

import java.util.ArrayList;

public class FeedListPresenter implements Presenter {

    private FeedListView view;
    private final DatabaseAdapter dbAdapter;
    private final NetworkTaskManager networkTaskManager;
    private final UnreadCountManager unreadCountManager;

    private ArrayList<Feed> feeds = new ArrayList<>();
    private ArrayList<Feed> allFeeds = new ArrayList<>();

    // Manage hide feed status
    private boolean isHided = true;
    private int numOfAllFeeds = 0;

    public FeedListPresenter(DatabaseAdapter dbAdapter, NetworkTaskManager networkTaskManager,
                             UnreadCountManager unreadCountManager) {
        this.dbAdapter = dbAdapter;
        this.networkTaskManager = networkTaskManager;
        this.unreadCountManager = unreadCountManager;
    }

    public void setView(FeedListView view) {
        this.view = view;
    }

    @Override
    public void create() {
    }

    @Override
    public void resume() {
        allFeeds = dbAdapter.getAllFeedsWithNumOfUnreadArticles();
        // For show/hide
        if (allFeeds.size() != 0) {
            addShowHideLine(allFeeds);
        }
        generateHidedFeedList();
        refreshList();
        if (networkTaskManager.isUpdatingFeed()) {
            view.setRefreshing(true);
            updateProgress();
        }
    }

    private void addShowHideLine(ArrayList<Feed> feeds) {
        feeds.add(new Feed());
    }

    private void generateHidedFeedList() {
        if (allFeeds.isEmpty()) {
            return;
        }
        feeds = (ArrayList<Feed>)allFeeds.clone();
        ArrayList<Feed> hideList = new ArrayList<>();
        for (Feed feed : allFeeds) {
            int numOfUnreadArticles = unreadCountManager.getUnreadCount(feed.getId());
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

    private void refreshList() {
        generateHidedFeedList();
        if (isHided) {
            view.init(feeds);
        }else {
            view.init(allFeeds);
        }
        updateAllUnreadArticlesCount();
    }

    private void updateAllUnreadArticlesCount() {
        view.setTotalUnreadCount(unreadCountManager.getTotal());
    }

    private void updateProgress() {
        int updatedFeed = numOfAllFeeds - networkTaskManager.getFeedRequestCountInQueue();
        if ((updatedFeed == numOfAllFeeds) && !networkTaskManager.isUpdatingFeed()) {
            updatedFeed = 0;
        }
        view.setProgress(updatedFeed, numOfAllFeeds);
    }

    @Override
    public void pause() {
        if (networkTaskManager.isUpdatingFeed()) {
            view.onRefreshCompleted();
        }
    }

    public void onDeleteFeedMenuClicked(int position) {
        view.showDeleteFeedAlertDialog(position);
    }

    public void onEditFeedMenuClicked(int position) {
        view.showEditTitleDialog(position, getFeedTitleAtPosition(position));
    }

    @NonNull
    private String getFeedTitleAtPosition(int position) {
        if (position < 0 || feeds == null || position > feeds.size()-1) {
            return "";
        }
        if (isHided) {
            feeds.get(position).getTitle();
        }
        String title = allFeeds.get(position).getTitle();
        if (title == null) title = "";
        return title;
    }

    public void onEditFeedOkButtonClicked(String newTitle, int position) {
        if(newTitle.equals("")) {
            view.showEditFeedTitleEmptyErrorToast();
        }else {
            int updatedFeedId = getFeedIdAtPosition(position);
            int numOfUpdate = dbAdapter.saveNewTitle(updatedFeedId, newTitle);
            if(numOfUpdate == 1) {
                view.showEditFeedSuccessToast();
                updateFeedTitle(updatedFeedId, newTitle);
            }else {
                view.showEditFeedFailToast();
            }
        }
    }

    private void updateFeedTitle(int feedId, String newTitle) {
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
        view.notifyDataSetChanged();
    }

    public void onDeleteOkButtonClicked(int position) {
        if(dbAdapter.deleteFeed(getFeedIdAtPosition(position))) {
            removeFeedAtPosition(position);
            view.showDeleteSuccessToast();
        }else {
            view.showDeleteFailToast();
        }
    }

    private int getFeedIdAtPosition(int position) {
        if (position < 0) {
            return -1;
        }

        if (feeds == null && allFeeds == null) {
            generateHidedFeedList();
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

    private void removeFeedAtPosition(int position) {
        if (isHided) {
            Feed deletedFeed = feeds.get(position);
            dbAdapter.deleteFeed(deletedFeed.getId());
            unreadCountManager.deleteFeed(deletedFeed.getId());
            feeds.remove(position);
            for (int i = 0;i < allFeeds.size();i++) {
                if (allFeeds.get(i).getId() == deletedFeed.getId()) {
                    allFeeds.remove(i);
                }
            }
        }else {
            Feed deletedFeed = allFeeds.get(position);
            dbAdapter.deleteFeed(deletedFeed.getId());
            unreadCountManager.deleteFeed(deletedFeed.getId());
            allFeeds.remove(position);
            for (int i = 0;i < feeds.size();i++) {
                if (feeds.get(i).getId() == deletedFeed.getId()) {
                    feeds.remove(i);
                }
            }
        }
        refreshList();
    }

    public void onFeedListClicked(int position, FeedListFragment.OnFeedListFragmentListener mListener) {
        int feedId = getFeedIdAtPosition(position);
        if (feedId == Feed.DEFAULT_FEED_ID) {
            changeHideStatus();
            return;
        }
        mListener.onListClicked(feedId);
    }

    private void changeHideStatus() {
        if (isHided) {
            isHided = false;
            view.init(allFeeds);
        }else {
            isHided = true;
            view.init(feeds);
        }
    }

    public void onRefresh() {
        if (allFeeds == null || allFeeds.isEmpty()) {
            onRefreshComplete();
            return;
        }

        networkTaskManager.updateAllFeeds(allFeeds);
    }

    private void onRefreshComplete() {
        view.onRefreshCompleted();
        updateProgress();
    }

    public void onFinishUpdate() {
        if (networkTaskManager.isUpdatingFeed()) {
            updateProgress();
        }else {
            onRefreshComplete();
            refreshList();
        }
    }

    public void onFinishAddFeed(@NonNull String feedUrl, int errorReason) {
        Feed newFeed = dbAdapter.getFeedByUrl(feedUrl);
        if (errorReason != 1 || newFeed == null) {
            if (errorReason == NetworkTaskManager.ERROR_INVALID_URL) {
                view.showInvalidUrlAddFeedErrorToast();
            } else {
                view.showGenericAddFeedErrorToast();
            }
        } else {
            view.showAddFeedSuccessToast();
            addFeed(newFeed);
            unreadCountManager.addFeed(newFeed);
            networkTaskManager.updateFeed(newFeed);
            refreshList();
        }
    }

    private void addFeed(Feed newFeed) {
        deleteShowHideLineIfNeeded();
        if (newFeed.getUnreadAriticlesCount() > 0) {
            feeds.add(newFeed);
            addShowHideLine(feeds);
        }
        allFeeds.add(newFeed);
        addShowHideLine(allFeeds);
        unreadCountManager.addFeed(newFeed);
        view.notifyDataSetChanged();
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

    public void activityCreated() {
        refreshList();
        numOfAllFeeds = dbAdapter.getNumOfFeeds();

        if (numOfAllFeeds == 0) {
            view.hideAllUnreadView();
        }else {
            updateAllUnreadArticlesCount();
        }
        view.init(feeds);
    }

    public boolean isAllRssShowView(int position) {
        return isHided && (position) == feeds.size();
    }

    public boolean isHideReadRssView(int position) {
        return !isHided && (position) == allFeeds.size();
    }

    public ArrayList<Feed> getFeeds() {
        return feeds;
    }

    public ArrayList<Feed> getAllFeeds() {
        return allFeeds;
    }
}
