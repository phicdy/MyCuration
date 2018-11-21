package com.phicdy.mycuration

import android.app.Application
import android.content.Context
import com.facebook.stetho.Stetho
import com.google.firebase.analytics.FirebaseAnalytics
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.di.appModule
import com.phicdy.mycuration.tracker.TrackerHelper
import com.phicdy.mycuration.util.FileUtil
import com.phicdy.mycuration.util.PreferenceHelper
import org.koin.android.ext.android.startKoin
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import java.io.File

class MyApplication : Application() {

    /**
     * Gets the default [Tracker] for this [Application].
     * @return tracker
     */
    // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
    companion object {
        fun setUp(context: Context): FirebaseAnalytics {
            return FirebaseAnalytics.getInstance(context)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(appModule))
        PreferenceHelper.setUp(this)
        DatabaseAdapter.setUp(DatabaseHelper(this))
        TrackerHelper.setTracker(setUp(this))
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/GenShinGothic-P-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )
        if (BuildConfig.DEBUG) {
            Stetho.initialize(
                    Stetho.newInitializerBuilder(this)
                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                            .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                            .build()
            )
        }

        // For old version under 1.6.0
        FileUtil.setUpIconSaveFolder(this)
        File(FileUtil.iconSaveFolder()).let { dir ->
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles().forEach { icon ->
                    icon.delete()
                }
                dir.delete()
            }
        }
    }

}
