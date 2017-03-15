package com.phicdy.mycuration.presenter;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.phicdy.mycuration.task.NetworkTaskManager;
import com.phicdy.mycuration.util.UrlUtil;
import com.phicdy.mycuration.view.FeedSearchView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class FeedSearchPresenter implements Presenter {

    private NetworkTaskManager networkTaskManager;
    private DatabaseAdapter adapter;
    private UnreadCountManager unreadManager;
    private FeedSearchView view;

    public FeedSearchPresenter(@NonNull NetworkTaskManager networkTaskManager,
                               @NonNull DatabaseAdapter adapter,
                               @NonNull UnreadCountManager unreadManager) {
        this.networkTaskManager = networkTaskManager;
        this.adapter = adapter;
        this.unreadManager = unreadManager;
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
        view.unregisterFinishReceiver();
    }

    public void onFabClicked(@NonNull String url) {
        if ((url.equals(""))) return;
        view.startFeedUrlHookActivity(url);
    }

    public void handle(@NonNull String query) {
        if (UrlUtil.isCorrectUrl(query)) {
            view.registerFinishReceiver();
            view.showProgressDialog();
            networkTaskManager.addNewFeed(query);
            return;
        }
        try {
            String encodedQuery = URLEncoder.encode(query, "utf-8");
            String url = "https://www.google.co.jp/search?q=" + encodedQuery;
            view.load(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void onFinishAddFeed(@NonNull String url, int reason) {
        Feed newFeed = adapter.getFeedByUrl(url);
        if (reason != NetworkTaskManager.REASON_NOT_FOUND || newFeed == null) {
            if (reason == NetworkTaskManager.ERROR_INVALID_URL) {
                view.showInvalidUrlErrorToast();
            } else {
                view.showGenericErrorToast();
            }
        } else {
            unreadManager.addFeed(newFeed);
            networkTaskManager.updateFeed(newFeed);
            view.showAddFeedSuccessToast();
        }
        view.dismissProgressDialog();
        view.finishView();
    }
}
