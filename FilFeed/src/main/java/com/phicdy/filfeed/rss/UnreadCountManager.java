package com.phicdy.filfeed.rss;

import android.content.Context;

import com.phicdy.filfeed.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UnreadCountManager {
    private Map<Integer, Integer> unreadCountMap = new HashMap<>();
    private DatabaseAdapter adapter;
    private static UnreadCountManager mgr;

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
        synchronized (unreadCountMap) {
            unreadCountMap.clear();
        }
        ArrayList<Feed> feeds = adapter.getAllFeedsWithNumOfUnreadArticles();
        synchronized (unreadCountMap) {
            for (Feed feed : feeds) {
                unreadCountMap.put(feed.getId(), feed.getUnreadAriticlesCount());
            }
        }
    }

    public void addFeed(Feed feed) {
        if (feed == null) {
            return;
        }
        unreadCountMap.put(feed.getId(), feed.getUnreadAriticlesCount());
    }

    public void deleteFeed(int feedId) {
        if (!unreadCountMap.containsKey(feedId)) {
            return;
        }
        unreadCountMap.remove(feedId);
    }

    public synchronized void addUnreadCount(int feedId, int addCount) {
        synchronized (unreadCountMap) {
            if (!unreadCountMap.containsKey(feedId)) {
                return;
            }
            int count = unreadCountMap.get(feedId);
            unreadCountMap.put(feedId, count + addCount);
        }
        updateDatbase(feedId);
    }

    public void conutUpUnreadCount(int feedId) {
        synchronized (unreadCountMap) {
            if (!unreadCountMap.containsKey(feedId)) {
                return;
            }
            int count = unreadCountMap.get(feedId);
            unreadCountMap.put(feedId, ++count);
        }
        updateDatbase(feedId);
    }

    public void conutDownUnreadCount(int feedId) {
        synchronized (unreadCountMap) {
            if (!unreadCountMap.containsKey(feedId)) {
                return;
            }
            int count = unreadCountMap.get(feedId);
            unreadCountMap.put(feedId, --count);
        }
        updateDatbase(feedId);
    }

    public int getUnreadCount(int feedId) {
        return unreadCountMap.containsKey(feedId) ? unreadCountMap.get(feedId) : -1;
    }

    private void updateDatbase(final int feedId) {
        if (!unreadCountMap.containsKey(feedId)) {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                adapter.updateUnreadArticleCount(feedId, unreadCountMap.get(feedId));
            }
        }.start();

    }

}
