package com.phicdy.mycuration.tracker;

import android.app.Activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.phicdy.mycuration.BuildConfig;
import com.phicdy.mycuration.MyApplication;
import com.phicdy.mycuration.R;

public class GATrackerHelper {

    private static GATrackerHelper gaTrackerHelper;
    private MyApplication application;
    private Tracker tracker;
    private String categoryAction;

    private GATrackerHelper(Activity activity) {
        application = (MyApplication)activity.getApplication();
        tracker = application.getDefaultTracker();
        categoryAction = activity.getString(R.string.action);
    }

    public static GATrackerHelper getInstance(Activity activity) {
        if (gaTrackerHelper == null) {
            gaTrackerHelper = new GATrackerHelper(activity);
        }
        return gaTrackerHelper;
    }

    public void sendScreen(String screenName) {
        if (BuildConfig.DEBUG) return;
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void sendEvent(String action) {
        if (BuildConfig.DEBUG) return;
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(categoryAction)
                .setAction(action)
                .build());
    }
}
