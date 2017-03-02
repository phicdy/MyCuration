package com.phicdy.mycuration.view;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.rss.Article;

public interface ArticleListView {
    void invalidateView();
    void showFooter();
    void removeFooter();
    void addArticle(Article article);
    void openInternalWebView(@NonNull String url);
    void openExternalWebView(@NonNull String url);
    void notifyListView();
    int size();
    void hideFabButton();
    void finish();
    Article getItem(int position);
    int getFirstVisiblePosition();
    int getLastVisiblePosition();
    void showShareUi(@NonNull String url);
    void scroll(int positionToScroll, int pixelFromTopAfterScroll);
}
