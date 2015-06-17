package com.phicdy.filfeed.rss;

import android.content.Context;

import com.phicdy.filfeed.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UnreadCountManager {
    private int total = 0;
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
        total = 0;
        ArrayList<Feed> feeds = adapter.getAllFeedsWithNumOfUnreadArticles();
        synchronized (unreadCountMap) {
            unreadCountMap.clear();
            for (Feed feed : feeds) {
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

    public synchronized void addUnreadCount(int feedId, int addCount) {
        synchronized (unreadCountMap) {
            if (!unreadCountMap.containsKey(feedId)) {
                return;
            }
            int count = unreadCountMap.get(feedId);
            unreadCountMap.put(feedId, count + addCount);
        }
        total += addCount;
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
        total++;
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

        new Thread() {
            @Override
            public void run() {
                int count = adapter.getNumOfUnreadArtilces(feedId);
                synchronized (unreadCountMap) {
                    total = total - unreadCountMap.get(feedId) + count;
                    unreadCountMap.put(feedId, count);
                }
                adapter.updateUnreadArticleCount(feedId, count);
            }
        }.start();

    }

}
