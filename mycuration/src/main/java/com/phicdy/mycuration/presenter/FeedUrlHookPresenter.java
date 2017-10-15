package com.phicdy.mycuration.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.rss.RssParseExecutor;
import com.phicdy.mycuration.rss.RssParseResult;
import com.phicdy.mycuration.rss.RssParser;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.phicdy.mycuration.task.NetworkTaskManager;
import com.phicdy.mycuration.util.UrlUtil;
import com.phicdy.mycuration.view.FeedUrlHookView;

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
        }

        @Override
        public void failed(@RssParseResult.FailedReason int reason) {
            if (reason == RssParseResult.INVALID_URL) {
                view.showInvalidUrlErrorToast();
            } else {
                view.showGenericErrorToast();
            }
            view.dismissProgressDialog();
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
        view.unregisterFinishAddReceiver();
    }

    public void handle(@NonNull String action, @NonNull String url) {
        if (action.equals(Intent.ACTION_VIEW) || action.equals(Intent.ACTION_SEND)) {
            if (UrlUtil.isCorrectUrl(url)) {
                view.registerFinishAddReceiver();
                view.showProgressDialog();
                RssParseExecutor executor = new RssParseExecutor(parser, dbAdapter);
                executor.start(url, callback);
            }else {
                view.showInvalidUrlErrorToast();
            }
        }else {
            view.finishView();
        }
    }

    public void handleFinish(@NonNull String action, @Nullable String feedUrl,
                      int errorReason) {
        if (action.equals(NetworkTaskManager.FINISH_UPDATE_ACTION)) {
            view.dismissProgressDialog();
            view.showSuccessToast();
            view.finishView();
        }
    }
}
