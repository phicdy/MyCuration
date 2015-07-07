package com.phicdy.filfeed.rss;

import android.content.Context;
import android.util.Log;

import com.phicdy.filfeed.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UnreadCountManager {
    private int total = 0;
    private Map<Integer, Integer> unreadCountMap = new HashMap<>();
    private ArrayList<Feed> allFeeds;
    private DatabaseAdapter adapter;
    private static UnreadCountManager mgr;

    private static final String LOG_TAG = "FilFeed.Unread";

    private UnreadCountManager(Context context) {
        this.adapter = DatabaseAdapter.getInstance(context);
        init();
    }

    public static UnreadCountManager getInstance(Context context) {
        if (mgr == null) {
            synchronized (UnreadCountManager.class) {
                if (mgr == null) {
                    mgr = new UnreadCountManager(context);
                }
            }
        }
        return mgr;
    }

    public void init() {
        total = 0;
        allFeeds = adapter.getAllFeedsWithNumOfUnreadArticles();
        synchronized (unreadCountMap) {
            unreadCountMap.clear();
            for (Feed feed : allFeeds) {
                unreadCountMap.put(feed.getId(), feed.getUnreadAriticlesCount());
                total += feed.getUnreadAriticlesCount();
            }
        }
    }

    public void addFeed(Feed feed) {
        if (feed == null) {
            return;
        }
        unreadCountMap.put(feed.getId(), feed.getUnreadAriticlesCount());
        total += feed.getUnreadAriticlesCount();
    }

    public void deleteFeed(int feedId) {
        if (!unreadCountMap.containsKey(feedId)) {
            return;
        }
        total -= unreadCountMap.get(feedId);
        unreadCountMap.remove(feedId);
    }

    public void conutUpUnreadCount(int feedId) {
        synchronized (unreadCountMap) {
            if (!unreadCountMap.containsKey(feedId)) {
                return;
            }
            int count = unreadCountMap.get(feedId) + 1;
            unreadCountMap.put(feedId, count);
        }
        total++;
        updateDatbase(feedId);
    }

    public void countDownUnreadCount(int feedId) {
        synchronized (unreadCountMap) {
            if (!unreadCountMap.containsKey(feedId)) {
                return;
            }
            int count = unreadCountMap.get(feedId);
            if (count == 0) {
                return;
            }
            unreadCountMap.put(feedId, --count);
        }
        total--;
        updateDatbase(feedId);
    }

    public int getUnreadCount(int feedId) {
        return unreadCountMap.containsKey(feedId) ? unreadCountMap.get(feedId) : -1;
    }

    public int getTotal() {
        return total;
    }

    private void updateDatbase(final int feedId) {
        if (!unreadCountMap.containsKey(feedId)) {
            return;
        }

        synchronized (unreadCountMap) {
            final int count = unreadCountMap.get(feedId);
            new Thread() {
                @Override
                public void run() {
                    adapter.updateUnreadArticleCount(feedId, count);
                }
            }.start();
        }
    }

    public void readAll(int feedId) {
        synchronized (unreadCountMap) {
            if (!unreadCountMap.containsKey(feedId)) {
                return;
            }
            int count = unreadCountMap.get(feedId);
            total -= count;
            unreadCountMap.put(feedId, 0);
        }
        updateDatbase(feedId);
    }

    public void readAll() {
        synchronized (unreadCountMap) {
            total = 0;
            for (Feed feed : allFeeds) {
                unreadCountMap.put(feed.getId(), 0);
                updateDatbase(feed.getId());
            }
        }
    }

    public void refreshConut(int feedId) {
        synchronized (unreadCountMap) {
            // Decrease original unread count
            int oldCount = unreadCountMap.get(feedId);
            total -= oldCount;

            // Calc unread count from database
            int count = adapter.getNumOfUnreadArtilces(feedId);
            adapter.updateUnreadArticleCount(feedId, count);
            unreadCountMap.put(feedId, count);
            total += count;
        }
    }
}
