package com.phicdy.mycuration.uitest

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.deleteAll
import com.phicdy.mycuration.repository.Database
import com.squareup.spoon.Spoon
import com.squareup.sqldelight.android.AndroidSqliteDriver
import org.junit.Assert.assertNotNull
import java.io.File

abstract class UiTest {

    internal fun setup(activity: Activity) {
        PreferenceHelper.setUp(activity)
        PreferenceHelper.setReviewed()
        grantWriteExternalStoragePermission(activity)
        deleteAllData()
    }

    internal open fun tearDown() {
        deleteAllData()
    }

    private fun grantWriteExternalStoragePermission(activity: Activity) {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (PermissionChecker.checkSelfPermission(ApplicationProvider.getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED) return
        ActivityCompat.requestPermissions(activity, arrayOf(permission), 1)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val allowButton = device.wait(Until.findObject(By.text("許可")), 5000)
        assertNotNull(allowButton)
        allowButton.click()
    }

    private fun deleteAllData() {
        val db = Database(
                AndroidSqliteDriver(
                        schema = Database.Schema,
                        context = ApplicationProvider.getApplicationContext(),
                        name = "rss_manage"
                )
        )
        deleteAll(db)
    }

    internal fun takeScreenshot(device: UiDevice) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File(context.externalCacheDir, System.currentTimeMillis().toString() + ".png")
        device.takeScreenshot(file)
        Spoon.save(context, file)
    }

    internal fun takeScreenshot(device: UiDevice, fileName: String) {
        if (fileName.isBlank()) {
            takeScreenshot(device)
            return
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File(context.externalCacheDir, "$fileName.png")
        device.takeScreenshot(file)
        Spoon.save(context, file)
    }
}
