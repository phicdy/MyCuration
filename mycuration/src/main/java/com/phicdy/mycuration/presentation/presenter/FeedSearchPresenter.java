package com.phicdy.mycuration.presentation.presenter;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.data.rss.Feed;
import com.phicdy.mycuration.domain.rss.RssParseExecutor;
import com.phicdy.mycuration.domain.rss.RssParseResult;
import com.phicdy.mycuration.domain.rss.RssParser;
import com.phicdy.mycuration.domain.rss.UnreadCountManager;
import com.phicdy.mycuration.domain.task.NetworkTaskManager;
import com.phicdy.mycuration.presentation.view.FeedSearchView;
import com.phicdy.mycuration.util.UrlUtil;

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
        public void succeeded(@NonNull String rssUrl) {
            onFinishAddFeed(rssUrl, RssParseResult.FailedReason.NOT_FAILED);
        }

        @Override
        public void failed(@NonNull RssParseResult.FailedReason reason, @NonNull String url) {
            onFinishAddFeed(url, reason);
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

    void onFinishAddFeed(@NonNull String url, RssParseResult.FailedReason reason) {
        Feed newFeed = adapter.getFeedByUrl(url);
        if (reason == RssParseResult.FailedReason.NOT_FAILED && newFeed != null) {
            unreadManager.addFeed(newFeed);
            networkTaskManager.updateFeed(newFeed);
            view.showAddFeedSuccessToast();
        } else {
            if (reason == RssParseResult.FailedReason.INVALID_URL) {
                view.showInvalidUrlErrorToast();
            } else {
                view.showGenericErrorToast();
            }
            view.trackFailedUrl(url);
        }
        view.dismissProgressBar();
        view.finishView();
    }
}
