package com.phicdy.mycuration.presenter;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.rss.RssParseExecutor;
import com.phicdy.mycuration.rss.RssParseResult;
import com.phicdy.mycuration.rss.RssParser;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.phicdy.mycuration.task.NetworkTaskManager;
import com.phicdy.mycuration.util.UrlUtil;
import com.phicdy.mycuration.view.FeedSearchView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class FeedSearchPresenter implements Presenter {

    private final NetworkTaskManager networkTaskManager;
    private final DatabaseAdapter adapter;
    private final UnreadCountManager unreadManager;
    private final RssParser parser;
    private FeedSearchView view;
    RssParseExecutor.RssParseCallback callback = new RssParseExecutor.RssParseCallback() {
        @Override
        public void succeeded(@NonNull String url) {
            onFinishAddFeed(url, RssParseResult.NOT_FAILED);
        }

        @Override
        public void failed(@RssParseResult.FailedReason int reason) {
            onFinishAddFeed("", reason);
        }
    };

    public FeedSearchPresenter(@NonNull NetworkTaskManager networkTaskManager,
                               @NonNull DatabaseAdapter adapter,
                               @NonNull UnreadCountManager unreadManager,
                               @NonNull RssParser parser) {
        this.networkTaskManager = networkTaskManager;
        this.adapter = adapter;
        this.unreadManager = unreadManager;
        this.parser = parser;
    }

    public void setView(FeedSearchView view) {
        this.view = view;
    }

    @Override
    public void create() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    public void onFabClicked(@NonNull String url) {
        if ((url.equals(""))) return;
        view.startFeedUrlHookActivity(url);
    }

    public void handle(@NonNull String query) {
        if (UrlUtil.INSTANCE.isCorrectUrl(query)) {
            view.showProgressBar();
            RssParseExecutor executor = new RssParseExecutor(parser, adapter);
            executor.start(query, callback);
            return;
        }
        try {
            String encodedQuery = URLEncoder.encode(query, "utf-8");
            String url = "https://www.google.co.jp/search?q=" + encodedQuery;
            view.load(url);
            view.setSearchViewTextFrom(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    void onFinishAddFeed(@NonNull String url, int reason) {
        Feed newFeed = adapter.getFeedByUrl(url);
        if (reason == RssParseResult.NOT_FAILED && newFeed != null) {
            unreadManager.addFeed(newFeed);
            networkTaskManager.updateFeed(newFeed);
            view.showAddFeedSuccessToast();
        } else {
            if (reason == NetworkTaskManager.ERROR_INVALID_URL) {
                view.showInvalidUrlErrorToast();
            } else {
                view.showGenericErrorToast();
            }
        }
        view.dismissProgressBar();
        view.finishView();
    }
}
