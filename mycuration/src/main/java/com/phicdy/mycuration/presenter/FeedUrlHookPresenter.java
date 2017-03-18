package com.phicdy.mycuration.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.phicdy.mycuration.task.NetworkTaskManager;
import com.phicdy.mycuration.util.UrlUtil;
import com.phicdy.mycuration.view.FeedUrlHookView;

public class FeedUrlHookPresenter implements Presenter {
    private FeedUrlHookView view;
    private final DatabaseAdapter dbAdapter;
    private final UnreadCountManager unreadCountManager;
    private final NetworkTaskManager networkTaskManager;

    public FeedUrlHookPresenter(@NonNull DatabaseAdapter dbAdapter,
                                @NonNull UnreadCountManager unreadCountManager,
                                @NonNull NetworkTaskManager networkTaskManager) {
        this.dbAdapter = dbAdapter;
        this.unreadCountManager = unreadCountManager;
        this.networkTaskManager = networkTaskManager;
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
                networkTaskManager.addNewFeed(url);
            }else {
                view.showInvalidUrlErrorToast();
            }
        }else {
            view.finishView();
        }
    }

    public void handleFinish(@NonNull String action, @Nullable String feedUrl,
                             int errorReason) {
        if (action.equals(NetworkTaskManager.FINISH_ADD_FEED)) {
            Feed newFeed = dbAdapter.getFeedByUrl(feedUrl);
            if (errorReason != NetworkTaskManager.REASON_NOT_FOUND || newFeed == null) {
                if (errorReason == NetworkTaskManager.ERROR_INVALID_URL) {
                    view.showInvalidUrlErrorToast();
                } else {
                    view.showGenericErrorToast();
                }
            } else {
                unreadCountManager.addFeed(newFeed);
                networkTaskManager.updateFeed(newFeed);
                view.showSuccessToast();
            }
            view.dismissProgressDialog();
            view.finishView();
        }
    }
}
