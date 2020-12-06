package com.phicdy.mycuration.domain.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

class AlarmManagerTaskManager @Inject constructor(@ApplicationContext private val context: Context) {

    fun setNewAlarm(intervalSec: Int) {
        if (intervalSec == 0) {
            cancelAlarm(context, AutoUpdateBroadcastReciever.AUTO_UPDATE_ACTION)
            return
        }
        setAlarm(context, AutoUpdateBroadcastReciever.AUTO_UPDATE_ACTION, intervalSec)
    }

    fun setNewHatenaUpdateAlarmAfterFeedUpdate(context: Context) {
        setAlarm(context, AutoUpdateBroadcastReciever.AUTO_UPDATE_HATENA_ACTION, HATENA_UPDATE_INTERVAL_AFTER_FEED_UPDATE_SECOND)
    }

    fun setFixUnreadCountAlarm(intervalSec: Int = FIX_UNREAD_COUNT_INTERVAL) {
        setAlarm(context, AutoUpdateBroadcastReciever.FIX_UNREAD_COUNT_ACTION, intervalSec)
    }

    private fun setAlarm(context: Context, action: String, intervalSec: Int) {
        val i = Intent(context, AutoUpdateBroadcastReciever::class.java).apply {
            this.action = action
        }

        val pi = PendingIntent.getBroadcast(context, 0, i, 0)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.SECOND, intervalSec)
        }
        Timber.d("Set alarm : %s", calendar.time.toString())

        val alm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alm.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
    }

    private fun cancelAlarm(context: Context, action: String) {
        val i = Intent(context, AutoUpdateBroadcastReciever::class.java)
        i.action = action

        val pi = PendingIntent.getBroadcast(context, 0, i, 0)
        val alm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alm.cancel(pi)
    }

    companion object {
        private const val HATENA_UPDATE_INTERVAL_AFTER_FEED_UPDATE_SECOND = 10
        private const val FIX_UNREAD_COUNT_INTERVAL = 12 * 60 * 60 // 12hours
    }
}
