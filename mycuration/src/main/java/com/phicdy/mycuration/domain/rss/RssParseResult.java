package com.phicdy.mycuration.domain.rss;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.phicdy.mycuration.data.rss.Feed;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RssParseResult {
    public final Feed feed;
    final int failedReason;
    public static final int NOT_FAILED = 0;
    public static final int INVALID_URL = 1;
    public static final int NON_RSS_HTML = 2;
    public static final int NOT_FOUND = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INVALID_URL, NON_RSS_HTML, NOT_FOUND})
    public @interface FailedReason {
    }

    RssParseResult(@NonNull Feed feed) {
        this.feed = feed;
        failedReason = NOT_FAILED;
    }

    RssParseResult(@FailedReason int failedReason) {
        feed = null;
        this.failedReason = failedReason;
    }
}
