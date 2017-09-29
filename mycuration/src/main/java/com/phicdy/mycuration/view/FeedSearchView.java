package com.phicdy.mycuration.view;

import android.support.annotation.NonNull;

public interface FeedSearchView {
    void startFeedUrlHookActivity(@NonNull String url);
    void showProgressDialog();
    void dismissProgressDialog();
    void registerFinishReceiver();
    void unregisterFinishReceiver();
    void load(@NonNull String url);
    void showInvalidUrlErrorToast();
    void showGenericErrorToast();
    void showAddFeedSuccessToast();
    void finishView();
    void setSearchViewTextFrom(@NonNull String url);
}
