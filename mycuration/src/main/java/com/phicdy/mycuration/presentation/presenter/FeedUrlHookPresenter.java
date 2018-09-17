package com.phicdy.mycuration.presentation.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.data.rss.Feed;
import com.phicdy.mycuration.domain.rss.RssParseExecutor;
import com.phicdy.mycuration.domain.rss.RssParseResult;
import com.phicdy.mycuration.domain.rss.RssParser;
import com.phicdy.mycuration.domain.rss.UnreadCountManager;
import com.phicdy.mycuration.domain.task.NetworkTaskManager;
import com.phicdy.mycuration.presentation.view.FeedUrlHookView;
import com.phicdy.mycuration.util.UrlUtil;

public class FeedUrlHookPresenter implements Presenter {
    private FeedUrlHookView view;
    private final DatabaseAdapter dbAdapter;
    private final UnreadCountManager unreadCountManager;
    private final NetworkTaskManager networkTaskManager;
    private final RssParser parser;
    private final String action;
    private final String dataString;
    private final CharSequence extrasText;

    RssParseExecutor.RssParseCallback callback = new RssParseExecutor.RssParseCallback() {
        @Override
        public void succeeded(@NonNull String rssUrl) {
            Feed newFeed = dbAdapter.getFeedByUrl(rssUrl);
            unreadCountManager.addFeed(newFeed);
            networkTaskManager.updateFeed(newFeed);
            view.showSuccessToast();
            view.finishView();
        }

        @Override
        public void failed(@NonNull RssParseResult.FailedReason reason, @NonNull String url) {
            if (reason == RssParseResult.FailedReason.INVALID_URL) {
                view.showInvalidUrlErrorToast();
            } else {
                view.showGenericErrorToast();
            }
            view.trackFailedUrl(url);
            view.finishView();
        }
    };

    public FeedUrlHookPresenter(@NonNull String action,
                                @NonNull String dataString,
                                @NonNull CharSequence extrasText,
                                @NonNull DatabaseAdapter dbAdapter,
                                @NonNull UnreadCountManager unreadCountManager,
                                @NonNull NetworkTaskManager networkTaskManager,
                                @NonNull RssParser parser) {
        this.action = action;
        this.dataString = dataString;
        this.extrasText = extrasText;
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
        if (!action.equals(Intent.ACTION_VIEW) && !action.equals(Intent.ACTION_SEND)) {
            view.finishView();
            return;
        }
        String url = null;
        if (action.equals(Intent.ACTION_VIEW)) {
            url = dataString;
        } else if (action.equals(Intent.ACTION_SEND)) {
            // For Chrome
            url = extrasText.toString();
        }
        if (url != null) {
            handle(action, url);
        }
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    private void handle(@NonNull String action, @NonNull String url) {
        if (action.equals(Intent.ACTION_VIEW) || action.equals(Intent.ACTION_SEND)) {
            if (UrlUtil.INSTANCE.isCorrectUrl(url)) {
                RssParseExecutor executor = new RssParseExecutor(parser, dbAdapter);
                executor.start(url, callback);
            }else {
                view.showInvalidUrlErrorToast();
                view.trackFailedUrl(url);
            }
        } else {
            view.finishView();
        }
    }
}
