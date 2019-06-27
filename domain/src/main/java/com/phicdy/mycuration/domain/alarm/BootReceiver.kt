package com.phicdy.mycuration.domain.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.phicdy.mycuration.data.preference.PreferenceHelper

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null && intent.action == Intent.ACTION_BOOT_COMPLETED) {
            AlarmManagerTaskManager(context).run {
                setNewAlarm(PreferenceHelper.autoUpdateIntervalSecond)
                setFixUnreadCountAlarm()
            }
        }
    }
}
