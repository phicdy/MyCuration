package com.phicdy.mycuration.presentation.view;

import android.support.annotation.NonNull;

public interface FeedSearchView {
    void startFeedUrlHookActivity(@NonNull String url);
    void showProgressBar();
    void dismissProgressBar();
    void load(@NonNull String url);
    void showInvalidUrlErrorToast();
    void showGenericErrorToast();
    void showAddFeedSuccessToast();
    void finishView();
    void setSearchViewTextFrom(@NonNull String url);
}
