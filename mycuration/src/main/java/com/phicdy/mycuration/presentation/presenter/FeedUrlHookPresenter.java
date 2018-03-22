package com.phicdy.mycuration.presentation.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.data.rss.Feed;
import com.phicdy.mycuration.data.rss.RssParseExecutor;
import com.phicdy.mycuration.data.rss.RssParseResult;
import com.phicdy.mycuration.data.rss.RssParser;
import com.phicdy.mycuration.data.rss.UnreadCountManager;
import com.phicdy.mycuration.domain.task.NetworkTaskManager;
import com.phicdy.mycuration.util.UrlUtil;
import com.phicdy.mycuration.presentation.view.FeedUrlHookView;

public class FeedUrlHookPresenter implements Presenter {
    private FeedUrlHookView view;
    private final DatabaseAdapter dbAdapter;
    private final UnreadCountManager unreadCountManager;
    private final NetworkTaskManager networkTaskManager;
    private final RssParser parser;
    RssParseExecutor.RssParseCallback callback = new RssParseExecutor.RssParseCallback() {
        @Override
        public void succeeded(@NonNull String url) {
            Feed newFeed = dbAdapter.getFeedByUrl(url);
            unreadCountManager.addFeed(newFeed);
            networkTaskManager.updateFeed(newFeed);
            view.showSuccessToast();
            view.finishView();
        }

        @Override
        public void failed(@RssParseResult.FailedReason int reason) {
            if (reason == RssParseResult.INVALID_URL) {
                view.showInvalidUrlErrorToast();
            } else {
                view.showGenericErrorToast();
            }
            view.finishView();
        }
    };

    public FeedUrlHookPresenter(@NonNull DatabaseAdapter dbAdapter,
                                @NonNull UnreadCountManager unreadCountManager,
                                @NonNull NetworkTaskManager networkTaskManager,
                                @NonNull RssParser parser) {
        this.dbAdapter = dbAdapter;
        this.unreadCountManager = unreadCountManager;
        this.networkTaskManager = networkTaskManager;
        this.parser = parser;
    }
    public void setView(@NonNull FeedUrlHookView view) {
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

    public void handle(@NonNull String action, @NonNull String url) {
        if (action.equals(Intent.ACTION_VIEW) || action.equals(Intent.ACTION_SEND)) {
            if (UrlUtil.INSTANCE.isCorrectUrl(url)) {
                RssParseExecutor executor = new RssParseExecutor(parser, dbAdapter);
                executor.start(url, callback);
            }else {
                view.showInvalidUrlErrorToast();
            }
        }else {
            view.finishView();
        }
    }
}
