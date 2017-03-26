package com.phicdy.mycuration.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Article;
import com.phicdy.mycuration.view.ArticleSearchResultView;

import java.util.ArrayList;

public class ArticleSearchResultPresenter implements Presenter {

    private ArticleSearchResultView view;
    private boolean isNewTop = false;
    private boolean isOpenInternal = false;
    private final DatabaseAdapter dbAdapter;

    public ArticleSearchResultPresenter(boolean isNewTop, boolean isOpenInternal, DatabaseAdapter dbAdapter) {
        this.isNewTop = isNewTop;
        this.isOpenInternal = isOpenInternal;
        this.dbAdapter = dbAdapter;
    }

    public void setView(ArticleSearchResultView view) {
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
    }

    public void handleIntent(@NonNull String intentAction, @NonNull String query) {
        if (Intent.ACTION_SEARCH.equals(intentAction)) {
            ArrayList<Article> articles = dbAdapter.searchArticles(query, isNewTop);
            view.refreshList(articles);
        }
    }

    public void onListViewItemClicked(@NonNull String url) {
        if (isOpenInternal) {
            view.startInternalWebView(url);
        } else {
            view.startExternalWebView(url);
        }
    }

    public void onListViewItemLongClick(@NonNull String url) {
        view.startShareUrl(url);
    }
}
