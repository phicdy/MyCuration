package com.phicdy.mycuration.uitest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.uiautomator.UiDevice;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.db.DatabaseHelper;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.squareup.spoon.Spoon;

import java.io.File;
import java.util.ArrayList;

import static android.support.test.InstrumentationRegistry.getTargetContext;

abstract class UiTest {

    void setup() {
        deleteAllData();
    }

    void tearDown() {
        deleteAllData();
    }

    private void deleteAllData() {
        DatabaseAdapter.setUp(new DatabaseHelper(getTargetContext()));
        DatabaseAdapter adapter = DatabaseAdapter.getInstance();
        ArrayList<Feed> feeds = adapter.getAllFeedsWithoutNumOfUnreadArticles();
        UnreadCountManager manager = UnreadCountManager.getInstance();
        for (Feed feed : feeds) {
            manager.deleteFeed(feed.getId());
        }
        manager.readAll();
        adapter.deleteAll();
    }

    void takeScreenshot(UiDevice device) {
        Context context = getTargetContext();
        final File file = new File(context.getExternalCacheDir(), System.currentTimeMillis() + ".png");
        device.takeScreenshot(file);
        Spoon.save(context, file);
    }

    void takeScreenshot(UiDevice device, @NonNull String fileName) {
        if (fileName.equals("")) {
            takeScreenshot(device);
            return;
        }
        Context context = getTargetContext();
        final File file = new File(context.getExternalCacheDir(), fileName + ".png");
        device.takeScreenshot(file);
        Spoon.save(context, file);
    }
}
