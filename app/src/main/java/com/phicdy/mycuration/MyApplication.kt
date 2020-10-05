package com.phicdy.mycuration

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.facebook.stetho.Stetho
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.phicdy.mycuration.advertisement.AdProvider
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.di.appModule
import com.phicdy.mycuration.domain.alarm.AlarmManagerTaskManager
import com.phicdy.mycuration.rss.IconFetchWorker
import com.phicdy.mycuration.tracker.TrackerHelper
import com.phicdy.mycuration.util.FileUtil
import com.phicdy.mycuration.util.log.TimberTree
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit


class MyApplication : Application() {

    // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
    companion object {
        fun setUp(context: Context): FirebaseAnalytics {
            return FirebaseAnalytics.getInstance(context)
        }
    }

    private val adProvider by inject<AdProvider>()

    private val rssRepository by inject<RssRepository>()

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

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        PreferenceHelper.setUp(this)

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
        WorkManager.initialize(this, Configuration.Builder()
                .setWorkerFactory(DefaultWorkerFactory(rssRepository))
                .build())
        val saveRequest =
                PeriodicWorkRequestBuilder<IconFetchWorker>(1, TimeUnit.DAYS)
                        .build()
        WorkManager.getInstance(this).apply {
            cancelAllWork()
            enqueue(saveRequest)
        }

        adProvider.init(this)
    }
}
