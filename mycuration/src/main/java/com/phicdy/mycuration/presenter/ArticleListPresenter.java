package com.phicdy.mycuration.presenter;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Article;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.phicdy.mycuration.util.PreferenceHelper;
import com.phicdy.mycuration.view.ArticleListView;

import java.util.ArrayList;
import java.util.Random;

public class ArticleListPresenter implements Presenter {

    private ArticleListView view;
    private final int feedId;
    private final int curationId;
    private final DatabaseAdapter adapter;
    private final UnreadCountManager unreadCountManager;
    private final boolean isOpenInternal;
    private final boolean isAllReadBack;
    private final boolean isNewArticleTop;
    private final int swipeDirection;
    private AsyncTask<Long, Void, Void> mTask;

    private ArrayList<Article> allArticles;
    private boolean isSwipeRightToLeft = false;
    private boolean isSwipeLeftToRight = false;
    private static final int SWIPE_MIN_WIDTH = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    public static final int DEFAULT_CURATION_ID = -1;
    private static final int LOAD_COUNT = 100;

    public ArticleListPresenter(int feedId, int curationId, DatabaseAdapter adapter,
                                UnreadCountManager unreadCountManager,
                                boolean isOpenInternal, boolean isAllReadBack,
                                boolean isNewArticleTop,int swipeDirection) {
        this.feedId = feedId;
        this.curationId = curationId;
        this.adapter = adapter;
        this.unreadCountManager = unreadCountManager;
        this.isOpenInternal = isOpenInternal;
        this.isAllReadBack = isAllReadBack;
        this.isNewArticleTop = isNewArticleTop;
        this.swipeDirection = swipeDirection;
    }

    public void setView(ArticleListView view) {
        this.view = view;
    }

    @Override
    public void create() {
    }

    public void createView() {
        allArticles = loadAllArticles();
        loadArticle(LOAD_COUNT, allArticles);
        view.notifyListView();
    }

    private @NonNull ArrayList<Article> loadAllArticles() {
        ArrayList<Article> allArticles;
        if (curationId != DEFAULT_CURATION_ID) {
            allArticles = adapter.getAllUnreadArticlesOfCuration(curationId, isNewArticleTop);
            if (allArticles.size() == 0) {
                allArticles = adapter.getAllArticlesOfCuration(curationId, isNewArticleTop);
            }
        }else if(feedId == Feed.ALL_FEED_ID) {
            allArticles = adapter.getAllUnreadArticles(isNewArticleTop);
            if(allArticles.size() == 0 && adapter.isExistArticle()) {
                allArticles = adapter.getTop300Articles(isNewArticleTop);
            }
        }else {
            allArticles = adapter.getUnreadArticlesInAFeed(feedId, isNewArticleTop);
            if(allArticles.size() == 0 && adapter.isExistArticle(feedId)) {
                allArticles = adapter.getAllArticlesInAFeed(feedId, isNewArticleTop);
            }
        }
        return allArticles;
    }

    private void loadArticle(int num, ArrayList<Article> articles) {
        int currentSize = view.size();
        for (int i = currentSize; i < currentSize+num; i++) {
            if (i >= articles.size()) {
                break;
            }
            view.addArticle(articles.get(i));
        }
    }

    @Override
    public void resume() {
        view.removeFooter();
    }

    @Override
    public void pause() {
    }

    public void onListItemClicked(Article article) {
        if(!isSwipeLeftToRight && !isSwipeRightToLeft) {
            setReadStatusToTouchedView(article, Article.TOREAD, false);
            if(isOpenInternal) {
                view.openInternalWebView(article.getUrl());
            }else {
                view.openExternalWebView(article.getUrl());
            }
        }
        isSwipeRightToLeft = false;
        isSwipeLeftToRight = false;
    }

    public void loadArticles() {
        if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
            return;
        }
        if (view.size() == allArticles.size()) {
            // All articles are loaded
            view.removeFooter();
            return;
        }

        view.showFooter();
        mTask = new AsyncTask<Long, Void, Void>() {

            @Override
            protected Void doInBackground(Long[] params) {
                try {
                    Thread.sleep(params[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                loadArticle(LOAD_COUNT, allArticles);
                view.invalidateView();
                view.removeFooter();
            }
        }.execute(Math.abs(new Random(System.currentTimeMillis()).nextLong() % 1000));
    }

    private void setReadStatusToTouchedView(Article article, final String status, boolean isAllReadBack) {
        String oldStatus = article.getStatus();
        if (oldStatus.equals(status) || (oldStatus.equals(Article.READ) && status.equals(Article.TOREAD))) {
            return;
        }
        adapter.saveStatus(article.getId(), status);
        if (status.equals(Article.TOREAD)) {
            unreadCountManager.countDownUnreadCount(article.getFeedId());
        }else if (status.equals(Article.UNREAD)) {
            unreadCountManager.conutUpUnreadCount(article.getFeedId());
        }
        article.setStatus(status);

        article.setStatus(status);
        if(isAllReadBack) {
            if(isAllRead()) {
                view.finish();
            }
        }
        view.notifyListView();
    }

    private boolean isAllRead() {
        boolean isAllRead = true;
        for (int i = 0; i < view.size(); i++) {
            Article article = view.getItem(i);
            if(article.getStatus().equals(Article.UNREAD)) {
                isAllRead = false;
                break;
            }
        }
        return  isAllRead;
    }

    public void onFlying(int touchedPosition, MotionEvent event1, MotionEvent event2, float velocityX) {
        if (touchedPosition < 0 || touchedPosition > view.size()-1) return;
        isSwipeLeftToRight = false;
        isSwipeRightToLeft = false;
        try {
            if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH) {
                return;
            }
            // event1 is first motion event and event2 is second motion event.
            // So, if the distance from event1'x to event2'x is longer than a certain value, it is swipe
            // And if event1'x is bigger than event2'x, it is swipe from right
            // And if event1'x is smaller than event2'x, it is swipe from left
            Article touchedArticle = view.getItem(touchedPosition);
            if (event1.getX() - event2.getX() > SWIPE_MIN_WIDTH
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                // Right to Left
                isSwipeRightToLeft = true;
                switch (swipeDirection) {
                    case PreferenceHelper.SWIPE_RIGHT_TO_LEFT:
                        setReadStatusToTouchedView(touchedArticle, Article.TOREAD, isAllReadBack);
                        break;
                    case PreferenceHelper.SWIPE_LEFT_TO_RIGHT:
                        setReadStatusToTouchedView(touchedArticle, Article.UNREAD, isAllReadBack);
                        break;
                    default:
                        break;
                }
            } else if (event2.getX() - event1.getX() > SWIPE_MIN_WIDTH
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                // Left to Right
                isSwipeLeftToRight = true;
                switch (swipeDirection) {
                    case PreferenceHelper.SWIPE_RIGHT_TO_LEFT:
                        setReadStatusToTouchedView(touchedArticle, Article.UNREAD, isAllReadBack);
                        break;
                    case PreferenceHelper.SWIPE_LEFT_TO_RIGHT:
                        setReadStatusToTouchedView(touchedArticle, Article.TOREAD, isAllReadBack);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            // nothing
        }
    }

    public void onListItemLongClicked(@NonNull Article item) {
        view.showShareUi(item.getUrl());
    }

    public void onFabButtonClicked() {
        if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
            return;
        }
        int firstPosition = view.getFirstVisiblePosition();
        int lastPosition = view.getLastVisiblePosition();
        for (int i = firstPosition; i <= lastPosition; i++) {
            if (i > view.size()-1) break;
            Article targetArticle = view.getItem(i);
            if (targetArticle == null) break;
            if (targetArticle.getStatus().equals(Article.UNREAD)) {
                targetArticle.setStatus(Article.TOREAD);
                unreadCountManager.countDownUnreadCount(targetArticle.getFeedId());
                adapter.saveStatus(targetArticle.getId(), Article.TOREAD);
            }
        }
        view.notifyListView();
        // Row in last visible position is hidden by buttons, so scroll to it
        final int PIXEL_FROM_TOP_AFTER_SCROLL = 4;
        view.scroll(lastPosition, PIXEL_FROM_TOP_AFTER_SCROLL);
        if (isAllReadBack && view.isBottomVisible()) {
            if (isAllRead()) {
                view.finish();
            }
        }
    }

    public void handleAllRead() {
        if (feedId == Feed.ALL_FEED_ID) {
            adapter.saveAllStatusToRead();
            unreadCountManager.readAll();
        } else {
            adapter.saveStatusToRead(feedId);
            unreadCountManager.readAll(feedId);
        }
        if (isAllReadBack) {
            view.finish();
        } else {
            for (int i = 0; i < view.size(); i++) {
                view.getItem(i).setStatus(Article.READ);
            }
            view.notifyListView();
        }
    }
}
