package com.phicdy.mycuration.rss;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.util.UrlUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RssParseExecutor {
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private RssParser parser;

    public void setParser(@NonNull RssParser parser) {
        this.parser = parser;
    }

    public void start(@NonNull final String url) {
        if (parser == null) return;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                parser.parseRssXml(UrlUtil.removeUrlParameter(url), true);
            }
        });

    }
}
