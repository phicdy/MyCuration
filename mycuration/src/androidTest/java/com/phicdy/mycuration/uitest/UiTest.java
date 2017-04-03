package com.phicdy.mycuration.uitest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.squareup.spoon.Spoon;

import java.io.File;
import java.util.ArrayList;

abstract class UiTest {

    void setup() {
        deleteAllData();
    }

    void tearDown() {
        deleteAllData();
    }

    private void deleteAllData() {
        Context context = InstrumentationRegistry.getTargetContext();
        DatabaseAdapter adapter = DatabaseAdapter.getInstance(context);
        ArrayList<Feed> feeds = adapter.getAllFeedsWithoutNumOfUnreadArticles();
        UnreadCountManager manager = UnreadCountManager.getInstance(context);
        for (Feed feed : feeds) {
            manager.deleteFeed(feed.getId());
        }
        adapter.deleteAll();
    }

    void takeScreenshot(UiDevice device) {
        Context context = InstrumentationRegistry.getTargetContext();
        final File file = new File(context.getExternalCacheDir(), System.currentTimeMillis() + ".png");
        device.takeScreenshot(file);
        Spoon.save(context, file);
    }

    void takeScreenshot(UiDevice device, @NonNull String fileName) {
        if (fileName.equals("")) {
            takeScreenshot(device);
            return;
        }
        Context context = InstrumentationRegistry.getTargetContext();
        final File file = new File(context.getExternalCacheDir(), fileName + ".png");
        device.takeScreenshot(file);
        Spoon.save(context, file);
    }
}
