package com.phicdy.mycuration.tracker;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.phicdy.mycuration.BuildConfig;

public class GATrackerHelper {

    private static Tracker tracker;
    private static String categoryAction;

    public static void setTracker(Tracker newTracker) {
        tracker = newTracker;
    }

    public static void setCategoryAction(String action) {
        categoryAction = action;
    }

    public static void sendScreen(String screenName) {
        if (BuildConfig.DEBUG) return;
        if (tracker == null) throw new IllegalStateException();
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void sendEvent(String action) {
        if (BuildConfig.DEBUG) return;
        if (tracker == null) throw new IllegalStateException();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(categoryAction)
                .setAction(action)
                .build());
    }

    public static void sendEvent(String action, long value) {
        if (BuildConfig.DEBUG) return;
        if (tracker == null) throw new IllegalStateException();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(categoryAction)
                .setAction(action)
                .setValue(value)
                .build());
    }
}
