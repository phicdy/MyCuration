package com.phicdy.mycuration.presenter;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Article;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.phicdy.mycuration.util.PreferenceHelper;
import com.phicdy.mycuration.view.ArticleListView;

import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertFalse;

public class ArticleListPresenterTest {

    @Test
    public void testOnCreate() {
        // For coverage
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);
        ArticleListPresenter presenter = new ArticleListPresenter(
                1, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
    }

    @Test
    public void footerIsInvisibleOnResume() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);
        ArticleListPresenter presenter = new ArticleListPresenter(
                1, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.resume();
        assertFalse(view.isFooterVisible);
    }

    @Test
    public void testOnPause() {
        // For coverage
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);
        ArticleListPresenter presenter = new ArticleListPresenter(
                1, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.pause();
    }

    private class MockView implements ArticleListView {

        private boolean isFooterVisible = false;

        @Override
        public void invalidateView() {

        }

        @Override
        public void showFooter() {
            isFooterVisible = true;
        }

        @Override
        public void removeFooter() {
            isFooterVisible = false;
        }

        @Override
        public void addArticle(Article article) {

        }

        @Override
        public void openInternalWebView(@NonNull String url) {

        }

        @Override
        public void openExternalWebView(@NonNull String url) {

        }

        @Override
        public void notifyListView() {

        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void hideFabButton() {

        }

        @Override
        public void finish() {

        }

        @Override
        public Article getItem(int position) {
            return null;
        }

        @Override
        public int getFirstVisiblePosition() {
            return 0;
        }

        @Override
        public int getLastVisiblePosition() {
            return 0;
        }

        @Override
        public void showShareUi(@NonNull String url) {

        }

        @Override
        public void scroll(int positionToScroll, int pixelFromTopAfterScroll) {

        }
    }
}
