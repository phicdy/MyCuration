package com.phicdy.mycuration

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.facebook.stetho.Stetho
import com.google.firebase.analytics.FirebaseAnalytics
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.di.appModule
import com.phicdy.mycuration.domain.alarm.AlarmManagerTaskManager
import com.phicdy.mycuration.tracker.TrackerHelper
import com.phicdy.mycuration.util.FileUtil
import com.phicdy.mycuration.util.log.TimberTree
import io.fabric.sdk.android.Fabric
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import java.io.File


class MyApplication : Application() {

    // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
    companion object {
        fun setUp(context: Context): FirebaseAnalytics {
            return FirebaseAnalytics.getInstance(context)
        }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            if (BuildConfig.DEBUG) androidLogger()
            androidContext(this@MyApplication)
            modules(listOf(appModule))
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(get<TimberTree>())
            Stetho.initialize(
                    Stetho.newInitializerBuilder(this)
                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                            .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                            .build()
            )
        }

        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()
        Fabric.with(this, crashlyticsKit)

        PreferenceHelper.setUp(this)
        try {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            if (PreferenceHelper.getPreviousVersion() != versionName) {
                PreferenceHelper.resetReviewCount()
                PreferenceHelper.resetReviewed()
                PreferenceHelper.setPreviousVersion(versionName)
            }
        } catch (e: PackageManager.NameNotFoundException) {
        }

        TrackerHelper.setTracker(setUp(this))

        // Font
        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/GenShinGothic-P-Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build())

        // For old version under 1.6.0
        FileUtil.setUpAppPath(this)
        File(FileUtil.iconSaveFolder()).let { dir ->
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles().forEach { icon ->
                    icon.delete()
                }
                dir.delete()
            }
        }

        AlarmManagerTaskManager(this).setFixUnreadCountAlarm()
    }
}
