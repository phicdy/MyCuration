package com.phicdy.mycuration.presentation.view;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.rss.Article;

import java.util.ArrayList;

public interface ArticleSearchResultView {
    void refreshList(@NonNull ArrayList<Article> articles);
    void startInternalWebView(@NonNull String url);
    void startExternalWebView(@NonNull String url);
    void startShareUrl(@NonNull String url);
}
