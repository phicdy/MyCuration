package com.phicdy.mycuration.presentation.presenter;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Article;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.phicdy.mycuration.util.PreferenceHelper;
import com.phicdy.mycuration.util.TextUtil;
import com.phicdy.mycuration.presentation.view.ArticleListView;
import com.phicdy.mycuration.presentation.view.fragment.ArticlesListFragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static android.support.v7.widget.helper.ItemTouchHelper.RIGHT;

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

    public static final int DEFAULT_CURATION_ID = -1;
    private static final int LOAD_COUNT = 100;
    private int loadedPosition = -1;

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
        loadArticle(LOAD_COUNT);
        if (allArticles.size() == 0) {
            view.showEmptyView();
        } else {
            view.notifyListView();
        }
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

    private void loadArticle(int num) {
        if (loadedPosition >= allArticles.size() || num < 0) return;
        loadedPosition += num;
        if (loadedPosition >= allArticles.size()-1) loadedPosition = allArticles.size()-1;
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    public void onListItemClicked(int position) {
        if (position < 0) return;
        Article article = allArticles.get(position);
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

    public void onScrolled(int lastItemPosition) {
        if (loadedPosition == allArticles.size()-1) {
            // All articles are loaded
            return;
        }
        if (lastItemPosition < loadedPosition+1) return;
        if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
            return;
        }

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
                loadArticle(LOAD_COUNT);
                view.notifyListView();
            }
        }.execute(Math.abs(new Random(System.currentTimeMillis()).nextLong() % 1000));
    }

    private void setReadStatusToTouchedView(Article article, final String status, boolean isAllReadBack) {
        String oldStatus = article.getStatus();
        if (oldStatus.equals(status) || (oldStatus.equals(Article.READ) && status.equals(Article.TOREAD))) {
            view.notifyListView();
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
        for (int i = 0; i <= loadedPosition; i++) {
            Article article = allArticles.get(i);
            if(article.getStatus().equals(Article.UNREAD)) {
                isAllRead = false;
                break;
            }
        }
        return  isAllRead;
    }

    public void onListItemLongClicked(int position) {
        if (position < 0) return;
        Article article = allArticles.get(position);
        view.showShareUi(article.getUrl());
    }

    public void onFabButtonClicked() {
        if (allArticles.size() == 0 || (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING)) {
            return;
        }
        int firstPosition = view.getFirstVisiblePosition();
        int lastPosition = view.getLastVisiblePosition();
        for (int i = firstPosition; i <= lastPosition; i++) {
            if (i > loadedPosition) break;
            Article targetArticle = allArticles.get(i);
            if (targetArticle == null) break;
            if (targetArticle.getStatus().equals(Article.UNREAD)) {
                targetArticle.setStatus(Article.TOREAD);
                unreadCountManager.countDownUnreadCount(targetArticle.getFeedId());
                adapter.saveStatus(targetArticle.getId(), Article.TOREAD);
            }
        }
        view.notifyListView();
        final int visibleNum = lastPosition - firstPosition;
        int positionAfterScroll = lastPosition + visibleNum;
        if (positionAfterScroll >= loadedPosition) positionAfterScroll = loadedPosition;
        view.scrollTo(positionAfterScroll);
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
            for (int i = 0; i < allArticles.size(); i++) {
                allArticles.get(i).setStatus(Article.READ);
            }
            view.notifyListView();
        }
    }

    public void onBindViewHolder(ArticlesListFragment.SimpleItemRecyclerViewAdapter.ArticleViewHolder holder, int position) {
        Article article = allArticles.get(position);
        if (article != null) {
            holder.setArticleTitle(article.getTitle());
            holder.setArticleUrl(article.getUrl());

            // Set article posted date
            SimpleDateFormat format = new SimpleDateFormat(
                    "yyyy/MM/dd HH:mm:ss", Locale.US);
            String dateString = format.format(new Date(article
                    .getPostedDate()));
            holder.setArticlePostedTime(dateString);

            // Set RSS Feed unread article count
            String hatenaPoint = article.getPoint();
            if(hatenaPoint.equals(Article.DEDAULT_HATENA_POINT)) {
                holder.setNotGetPoint();
            }else {
                holder.setArticlePoint(hatenaPoint);
            }

            String feedTitle = article.getFeedTitle();
            if(feedTitle == null) {
                holder.hideRssInfo();
            }else {
                holder.setRssTitle(article.getFeedTitle());

                String iconPath = article.getFeedIconPath();
                if (!TextUtil.INSTANCE.isEmpty(iconPath) && new File(iconPath).exists()) {
                    holder.setRssIcon(article.getFeedIconPath());
                }else {
                    holder.setDefaultRssIcon();
                }
            }

            // Change color if already be read
            if (article.getStatus().equals(Article.TOREAD) || article.getStatus().equals(Article.READ)) {
                holder.changeColorToRead();
            } else {
                holder.changeColorToUnread();
            }
        }

    }

    public int articleSize() {
        if (loadedPosition == allArticles.size()-1) return allArticles.size();
        // Index starts with 0 and add +1 for footer, so add 2
        return loadedPosition+2;
    }

    public int onGetItemViewType(int position) {
        if (position == loadedPosition+1) return ArticlesListFragment.SimpleItemRecyclerViewAdapter.VIEW_TYPE_FOOTER;
        return ArticlesListFragment.SimpleItemRecyclerViewAdapter.VIEW_TYPE_ARTICLE;
    }

    boolean isAllUnreadArticle() {
        int index = 0;
        for (Article article : allArticles) {
            if (!article.getStatus().equals(Article.UNREAD)) return false;
            index++;
            if (index == loadedPosition) break;
        }
        return true;
    }

    public void onSwiped(int direction, int touchedPosition) {
        Article touchedArticle = allArticles.get(touchedPosition);
        switch (direction) {
            case LEFT:
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
                isSwipeRightToLeft = false;
                break;
            case RIGHT:
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
                isSwipeLeftToRight = false;
                break;
        }
    }
}
