package com.phicdy.mycuration.tracker

import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.phicdy.mycuration.BuildConfig

object GATrackerHelper {

    private lateinit var tracker: Tracker
    private lateinit var categoryAction: String

    fun setTracker(newTracker: Tracker) {
        tracker = newTracker
    }

    fun setCategoryAction(action: String) {
        categoryAction = action
    }

    fun sendScreen(screenName: String) {
        if (BuildConfig.DEBUG) return
        tracker.setScreenName(screenName)
        tracker.send(HitBuilders.ScreenViewBuilder().build())
    }

    fun sendEvent(action: String) {
        if (BuildConfig.DEBUG) return
        tracker.send(HitBuilders.EventBuilder()
                .setCategory(categoryAction)
                .setAction(action)
                .build())
    }

    fun sendEvent(action: String, label: String) {
        if (BuildConfig.DEBUG) return
        tracker.send(HitBuilders.EventBuilder()
                .setCategory(categoryAction)
                .setAction(action)
                .setLabel(label)
                .build())
    }
}
