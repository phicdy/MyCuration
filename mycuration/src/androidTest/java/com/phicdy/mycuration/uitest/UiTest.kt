package com.phicdy.mycuration.uitest

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.test.InstrumentationRegistry
import android.support.test.InstrumentationRegistry.getTargetContext
import android.support.test.uiautomator.By
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.Until
import android.support.v4.app.ActivityCompat
import android.support.v4.content.PermissionChecker
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.domain.rss.UnreadCountManager
import com.squareup.spoon.Spoon
import junit.framework.Assert.assertNotNull
import java.io.File

abstract class UiTest {

    internal fun setup(activity: Activity) {
        grantWriteExternalStoragePermission(activity)
        deleteAllData()
    }

    internal open fun tearDown() {
        deleteAllData()
    }

    private fun grantWriteExternalStoragePermission(activity: Activity) {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (PermissionChecker.checkSelfPermission(getTargetContext(), permission) == PackageManager.PERMISSION_GRANTED) return
        ActivityCompat.requestPermissions(activity, arrayOf(permission), 1)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val allowButton = device.wait(Until.findObject(By.text("許可")), 5000)
        assertNotNull(allowButton)
        allowButton.click()
    }

    private fun deleteAllData() {
        DatabaseAdapter.setUp(DatabaseHelper(getTargetContext()))
        val adapter = DatabaseAdapter.getInstance()
        val feeds = adapter.allFeedsWithoutNumOfUnreadArticles
        val manager = UnreadCountManager
        for (feed in feeds) {
            manager.deleteFeed(feed.id)
        }
        manager.readAll()
        adapter.deleteAll()
    }

    internal fun takeScreenshot(device: UiDevice) {
        val context = getTargetContext()
        val file = File(context.externalCacheDir, System.currentTimeMillis().toString() + ".png")
        device.takeScreenshot(file)
        Spoon.save(context, file)
    }

    internal fun takeScreenshot(device: UiDevice, fileName: String) {
        if (fileName.isBlank()) {
            takeScreenshot(device)
            return
        }
        val context = getTargetContext()
        val file = File(context.externalCacheDir, "$fileName.png")
        device.takeScreenshot(file)
        Spoon.save(context, file)
    }
}
