package com.phicdy.mycuration.uitest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;

import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.data.db.DatabaseHelper;
import com.phicdy.mycuration.data.rss.Feed;
import com.phicdy.mycuration.domain.rss.UnreadCountManager;
import com.squareup.spoon.Spoon;

import java.io.File;
import java.util.ArrayList;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertNotNull;

public abstract class UiTest {


    void setup(Activity activity) {
        grantWriteExternalStoragePermission(activity);
        deleteAllData();
    }

    void tearDown() {
        deleteAllData();
    }

    private void grantWriteExternalStoragePermission(Activity activity) {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (PermissionChecker.checkSelfPermission(getTargetContext(), permission) ==
                PackageManager.PERMISSION_GRANTED) return;
        ActivityCompat.requestPermissions(activity,
                new String[]{permission}, 1);
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject2 allowButton = device.wait(Until.findObject(By.text("許可")), 5000);
        assertNotNull(allowButton);
        allowButton.click();
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
