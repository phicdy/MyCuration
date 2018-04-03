package com.phicdy.mycuration.presentation.view;

import android.support.annotation.NonNull;

public interface ArticleListView {
    void openInternalWebView(@NonNull String url, @NonNull String rssTitle);
    void openExternalWebView(@NonNull String url);
    void notifyListView();
    void finish();
    int getFirstVisiblePosition();
    int getLastVisiblePosition();
    void showShareUi(@NonNull String url);
    void scrollTo(int position);
    boolean isBottomVisible();
    void showEmptyView();
}
