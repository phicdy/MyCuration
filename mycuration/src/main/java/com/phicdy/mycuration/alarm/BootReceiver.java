package com.phicdy.mycuration.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.phicdy.mycuration.util.PreferenceHelper;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            AlarmManagerTaskManager manager = new AlarmManagerTaskManager(context);
            PreferenceHelper helper = PreferenceHelper.INSTANCE;
            int intervalSec = helper.getAutoUpdateIntervalSecond();
            manager.setNewAlarm(intervalSec);
        }
    }
}
