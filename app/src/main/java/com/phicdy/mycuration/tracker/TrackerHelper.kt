package com.phicdy.mycuration.tracker

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.phicdy.mycuration.BuildConfig

object TrackerHelper {

    private lateinit var tracker: FirebaseAnalytics

    fun setTracker(newTracker: FirebaseAnalytics) {
        tracker = newTracker
    }

    fun sendButtonEvent(itemId: String) {
        sendEvent(itemId, "button", FirebaseAnalytics.Event.SELECT_CONTENT)
    }

    fun sendSettingEvent(setting: String, value: String) {
        sendEvent(setting, "setting", FirebaseAnalytics.Event.SELECT_CONTENT, value)
    }

    fun sendUiEvent(itemId: String) {
        sendEvent(itemId, "ui", FirebaseAnalytics.Event.SELECT_CONTENT)
    }

    fun sendFailedParseUrl(event: String, url: String) {
        val params = Bundle().apply {
            putString("failed_url", url)
        }
        tracker.logEvent(event, params)
    }

    private fun sendEvent(itemId: String, contentType: String, event: String, value: String = "") {
        if (BuildConfig.DEBUG) return
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            putString(FirebaseAnalytics.Param.VALUE, value)
        }
        tracker.logEvent(event, params)
    }

}
