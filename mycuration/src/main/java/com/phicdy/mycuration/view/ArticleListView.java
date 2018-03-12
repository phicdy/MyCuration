package com.phicdy.mycuration.view;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.rss.Article;

public interface ArticleListView {
    void openInternalWebView(@NonNull String url);
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
