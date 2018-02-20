package com.phicdy.mycuration

import android.app.Application
import android.content.Context

import com.facebook.stetho.Stetho
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import com.phicdy.mycuration.db.DatabaseAdapter
import com.phicdy.mycuration.db.DatabaseHelper
import com.phicdy.mycuration.tracker.GATrackerHelper
import com.phicdy.mycuration.util.PreferenceHelper

import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class MyApplication : Application() {

    /**
     * Gets the default [Tracker] for this [Application].
     * @return tracker
     */
    // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
    companion object DefaultTracker {
        fun setUp(context: Context): Tracker {
            val analytics = GoogleAnalytics.getInstance(context)
            return analytics.newTracker(R.xml.global_tracker)
        }
    }
    override fun onCreate() {
        super.onCreate()
        PreferenceHelper.setUp(this)
        DatabaseAdapter.setUp(DatabaseHelper(this))
        GATrackerHelper.setTracker(DefaultTracker.setUp(this))
        GATrackerHelper.setCategoryAction(getString(R.string.action))
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
    }

}
