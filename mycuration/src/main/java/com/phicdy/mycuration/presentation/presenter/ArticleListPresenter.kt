package com.phicdy.mycuration.presentation.presenter

import android.content.Intent

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.rss.Article
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.domain.rss.UnreadCountManager
import com.phicdy.mycuration.util.PreferenceHelper
import com.phicdy.mycuration.util.TextUtil
import com.phicdy.mycuration.presentation.view.ArticleListView
import com.phicdy.mycuration.presentation.view.fragment.ArticlesListFragment

import java.io.File
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import java.util.Random

import android.support.v7.widget.helper.ItemTouchHelper.LEFT
import android.support.v7.widget.helper.ItemTouchHelper.RIGHT
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ArticleListPresenter(private val feedId: Int, private val curationId: Int, private val adapter: DatabaseAdapter,
                           private val unreadCountManager: UnreadCountManager,
                           private val isOpenInternal: Boolean, private val isAllReadBack: Boolean,
                           private val isNewArticleTop: Boolean, private val swipeDirection: Int,
                           private val query: String, private val action: String) : Presenter {

    companion object {
        const val DEFAULT_CURATION_ID = -1
        private const val LOAD_COUNT = 100
    }
    private lateinit var view: ArticleListView

    private var allArticles: ArrayList<Article> = arrayListOf()
    private var isSwipeRightToLeft = false
    private var isSwipeLeftToRight = false
    private var loadedPosition = -1

    private val isAllRead: Boolean
        get() {
            var isAllRead = true
            for (i in 0..loadedPosition) {
                val article = allArticles[i]
                if (article.status == Article.UNREAD) {
                    isAllRead = false
                    break
                }
            }
            return isAllRead
        }

    private val isSearchAction: Boolean
        get() {
            return Intent.ACTION_SEARCH == action
        }

    internal val isAllUnreadArticle: Boolean
        get() {
            var index = 0
            for (article in allArticles) {
                if (article.status != Article.UNREAD) return false
                index++
                if (index == loadedPosition) break
            }
            return true
        }

    fun setView(view: ArticleListView) {
        this.view = view
    }

    override fun create() {}

    fun createView() {
        allArticles = loadAllArticles()
        loadArticle(LOAD_COUNT)
        if (allArticles.size == 0) {
            if (isSearchAction) {
                view.showNoSearchResult()
            } else {
                view.showEmptyView()
            }
        } else {
            view.notifyListView()
        }
    }

    private fun loadAllArticles(): ArrayList<Article> {
        var allArticles: ArrayList<Article>
        if (isSearchAction) {
            allArticles = adapter.searchArticles(query, isNewArticleTop)
        } else if (curationId != DEFAULT_CURATION_ID) {
            allArticles = adapter.getAllUnreadArticlesOfCuration(curationId, isNewArticleTop)
            if (allArticles.size == 0) {
                allArticles = adapter.getAllArticlesOfCuration(curationId, isNewArticleTop)
            }
        } else if (feedId == Feed.ALL_FEED_ID) {
            allArticles = adapter.getAllUnreadArticles(isNewArticleTop)
            if (allArticles.size == 0 && adapter.isExistArticle) {
                allArticles = adapter.getTop300Articles(isNewArticleTop)
            }
        } else {
            allArticles = adapter.getUnreadArticlesInAFeed(feedId, isNewArticleTop)
            if (allArticles.size == 0 && adapter.isExistArticle(feedId)) {
                allArticles = adapter.getAllArticlesInAFeed(feedId, isNewArticleTop)
            }
        }
        return allArticles
    }

    private fun loadArticle(num: Int) {
        if (loadedPosition >= allArticles.size || num < 0) return
        loadedPosition += num
        if (loadedPosition >= allArticles.size - 1) loadedPosition = allArticles.size - 1
    }

    override fun resume() {}

    override fun pause() {}

    fun onListItemClicked(position: Int) {
        if (position < 0) return
        val article = allArticles[position]
        if (!isSwipeLeftToRight && !isSwipeRightToLeft) {
            setReadStatusToTouchedView(article, Article.TOREAD, false)
            if (isOpenInternal) {
                val feedTitle = if (feedId == Feed.ALL_FEED_ID) {
                    article.feedTitle
                } else {
                    val feed = adapter.getFeedById(feedId)
                    feed.title
                }
                view.openInternalWebView(article.url, feedTitle)
            } else {
                view.openExternalWebView(article.url)
            }
        }
        isSwipeRightToLeft = false
        isSwipeLeftToRight = false
    }

    fun onScrolled(lastItemPosition: Int) {
        if (loadedPosition == allArticles.size - 1) {
            // All articles are loaded
            return
        }
        if (lastItemPosition < loadedPosition + 1) return
        Flowable.just(Math.abs(Random(System.currentTimeMillis()).nextLong() % 1000))
                .subscribeOn(Schedulers.io())
                .map { Thread.sleep(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    loadArticle(LOAD_COUNT)
                    view.notifyListView()
                }
    }

    private fun setReadStatusToTouchedView(article: Article, status: String, isAllReadBack: Boolean) {
        val oldStatus = article.status
        if (oldStatus == status || oldStatus == Article.READ && status == Article.TOREAD) {
            view.notifyListView()
            return
        }
        adapter.saveStatus(article.id, status)
        if (status == Article.TOREAD) {
            unreadCountManager.countDownUnreadCount(article.feedId)
        } else if (status == Article.UNREAD) {
            unreadCountManager.conutUpUnreadCount(article.feedId)
        }
        article.status = status

        article.status = status
        if (isAllReadBack) {
            if (isAllRead) {
                view.finish()
            }
        }
        view.notifyListView()
    }

    fun onListItemLongClicked(position: Int) {
        if (position < 0) return
        val article = allArticles[position]
        view.showShareUi(article.url)
    }

    fun onFabButtonClicked() {
        if (allArticles.size == 0) return
        val firstPosition = view.firstVisiblePosition
        val lastPosition = view.lastVisiblePosition
        for (i in firstPosition..lastPosition) {
            if (i > loadedPosition) break
            val targetArticle = allArticles[i]
            if (targetArticle.status == Article.UNREAD) {
                targetArticle.status = Article.TOREAD
                unreadCountManager.countDownUnreadCount(targetArticle.feedId)
                adapter.saveStatus(targetArticle.id, Article.TOREAD)
            }
        }
        view.notifyListView()
        val visibleNum = lastPosition - firstPosition
        var positionAfterScroll = lastPosition + visibleNum
        if (positionAfterScroll >= loadedPosition) positionAfterScroll = loadedPosition
        view.scrollTo(positionAfterScroll)
        if (isAllReadBack && view.isBottomVisible) {
            if (isAllRead) {
                view.finish()
            }
        }
    }

    fun handleAllRead() {
        if (feedId == Feed.ALL_FEED_ID) {
            adapter.saveAllStatusToRead()
            unreadCountManager.readAll()
        } else {
            adapter.saveStatusToRead(feedId)
            unreadCountManager.readAll(feedId)
        }
        if (isAllReadBack) {
            view.finish()
        } else {
            for (i in allArticles.indices) {
                allArticles[i].status = Article.READ
            }
            view.notifyListView()
        }
    }

    fun onBindViewHolder(holder: ArticlesListFragment.SimpleItemRecyclerViewAdapter.ArticleViewHolder, position: Int) {
        val article = allArticles[position]
        holder.setArticleTitle(article.title)
        holder.setArticleUrl(article.url)

        // Set article posted date
        val format = SimpleDateFormat(
                "yyyy/MM/dd HH:mm:ss", Locale.US)
        val dateString = format.format(Date(article.postedDate))
        holder.setArticlePostedTime(dateString)

        // Set RSS Feed unread article count
        val hatenaPoint = article.point
        if (hatenaPoint == Article.DEDAULT_HATENA_POINT) {
            holder.setNotGetPoint()
        } else {
            holder.setArticlePoint(hatenaPoint)
        }

        val feedTitle = article.feedTitle
        if (feedTitle == "") {
            holder.hideRssInfo()
        } else {
            holder.setRssTitle(article.feedTitle)

            val iconPath = article.feedIconPath
            if (!TextUtil.isEmpty(iconPath) && File(iconPath).exists()) {
                holder.setRssIcon(article.feedIconPath)
            } else {
                holder.setDefaultRssIcon()
            }
        }

        // Change color if already be read
        if (article.status == Article.TOREAD || article.status == Article.READ) {
            holder.changeColorToRead()
        } else {
            holder.changeColorToUnread()
        }

    }

    fun articleSize(): Int {
        return if (loadedPosition == allArticles.size - 1) allArticles.size else loadedPosition + 2
        // Index starts with 0 and add +1 for footer, so add 2
    }

    fun onGetItemViewType(position: Int): Int {
        return if (position == loadedPosition + 1) ArticlesListFragment.VIEW_TYPE_FOOTER else ArticlesListFragment.VIEW_TYPE_ARTICLE
    }

    fun onSwiped(direction: Int, touchedPosition: Int) {
        val touchedArticle = allArticles[touchedPosition]
        when (direction) {
            LEFT -> {
                isSwipeRightToLeft = true
                when (swipeDirection) {
                    PreferenceHelper.SWIPE_RIGHT_TO_LEFT -> setReadStatusToTouchedView(touchedArticle, Article.TOREAD, isAllReadBack)
                    PreferenceHelper.SWIPE_LEFT_TO_RIGHT -> setReadStatusToTouchedView(touchedArticle, Article.UNREAD, isAllReadBack)
                    else -> {
                    }
                }
                isSwipeRightToLeft = false
            }
            RIGHT -> {
                isSwipeLeftToRight = true
                when (swipeDirection) {
                    PreferenceHelper.SWIPE_RIGHT_TO_LEFT -> setReadStatusToTouchedView(touchedArticle, Article.UNREAD, isAllReadBack)
                    PreferenceHelper.SWIPE_LEFT_TO_RIGHT -> setReadStatusToTouchedView(touchedArticle, Article.TOREAD, isAllReadBack)
                    else -> {
                    }
                }
                isSwipeLeftToRight = false
            }
        }
    }

}
