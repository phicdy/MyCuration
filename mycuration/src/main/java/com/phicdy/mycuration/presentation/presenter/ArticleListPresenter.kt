package com.phicdy.mycuration.presentation.presenter

import android.content.Intent
import android.support.v7.widget.helper.ItemTouchHelper.LEFT
import android.support.v7.widget.helper.ItemTouchHelper.RIGHT
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.data.rss.Article
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.presentation.view.ArticleListView
import com.phicdy.mycuration.presentation.view.fragment.ArticlesListFragment
import com.phicdy.mycuration.util.PreferenceHelper
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import java.util.Random

class ArticleListPresenter(private val feedId: Int, private val curationId: Int, private val adapter: DatabaseAdapter,
                           private val preferenceHelper: PreferenceHelper,
                           private val articleRepository: ArticleRepository,
                           private val unreadCountRepository: UnreadCountRepository,
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
    private var disposable: Disposable? = null

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

    suspend fun createView() = coroutineScope {
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

    private suspend fun loadAllArticles(): ArrayList<Article> = coroutineScope {
        var allArticles: ArrayList<Article>
        if (isSearchAction) {
            allArticles = adapter.searchArticles(query, preferenceHelper.sortNewArticleTop)
        } else if (curationId != DEFAULT_CURATION_ID) {
            allArticles = adapter.getAllUnreadArticlesOfCuration(curationId, preferenceHelper.sortNewArticleTop)
            if (allArticles.size == 0) {
                allArticles = adapter.getAllArticlesOfCuration(curationId, preferenceHelper.sortNewArticleTop)
            }
        } else if (feedId == Feed.ALL_FEED_ID) {
            allArticles = adapter.getAllUnreadArticles(preferenceHelper.sortNewArticleTop)
            if (allArticles.size == 0 && articleRepository.isExistArticle()) {
                allArticles = adapter.getTop300Articles(preferenceHelper.sortNewArticleTop)
            }
        } else {
            allArticles = adapter.getUnreadArticlesInAFeed(feedId, preferenceHelper.sortNewArticleTop)
            if (allArticles.size == 0 && articleRepository.isExistArticleOf(feedId)) {
                allArticles = adapter.getAllArticlesInAFeed(feedId, preferenceHelper.sortNewArticleTop)
            }
        }
        return@coroutineScope allArticles
    }

    private fun loadArticle(num: Int) {
        if (loadedPosition >= allArticles.size || num < 0) return
        loadedPosition += num
        if (loadedPosition >= allArticles.size - 1) loadedPosition = allArticles.size - 1
    }

    override fun resume() {}

    override fun pause() { disposable?.dispose() }

    suspend fun onListItemClicked(position: Int) = coroutineScope {
        if (position < 0) return@coroutineScope
        val article = allArticles[position]
        if (!isSwipeLeftToRight && !isSwipeRightToLeft) {
            setReadStatusToTouchedView(article, Article.TOREAD, false)
            if (preferenceHelper.isOpenInternal) {
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
        disposable = Flowable.just(Math.abs(Random(System.currentTimeMillis()).nextLong() % 1000))
                .subscribeOn(Schedulers.io())
                .map { Thread.sleep(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    loadArticle(LOAD_COUNT)
                    view.notifyListView()
                }
    }

    private suspend fun setReadStatusToTouchedView(article: Article, status: String, isAllReadBack: Boolean) = coroutineScope {
        val oldStatus = article.status
        if (oldStatus == status || oldStatus == Article.READ && status == Article.TOREAD) {
            view.notifyListView()
            return@coroutineScope
        }
        adapter.saveStatus(article.id, status)
        if (status == Article.TOREAD) {
            unreadCountRepository.countDownUnreadCount(article.feedId)
        } else if (status == Article.UNREAD) {
            unreadCountRepository.conutUpUnreadCount(article.feedId)
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

    suspend fun onFabButtonClicked() = coroutineScope {
        if (allArticles.size == 0) return@coroutineScope
        val firstPosition = view.firstVisiblePosition
        val lastPosition = view.lastVisiblePosition
        for (i in firstPosition..lastPosition) {
            if (i > loadedPosition) break
            val targetArticle = allArticles[i]
            if (targetArticle.status == Article.UNREAD) {
                targetArticle.status = Article.TOREAD
                unreadCountRepository.countDownUnreadCount(targetArticle.feedId)
                adapter.saveStatus(targetArticle.id, Article.TOREAD)
            }
        }
        view.notifyListView()
        val visibleNum = lastPosition - firstPosition
        var positionAfterScroll = lastPosition + visibleNum
        if (positionAfterScroll >= loadedPosition) positionAfterScroll = loadedPosition
        view.scrollTo(positionAfterScroll)
        if (preferenceHelper.allReadBack && view.isBottomVisible) {
            if (isAllRead) {
                view.finish()
            }
        }
    }

    suspend fun handleAllRead() = coroutineScope {
        if (feedId == Feed.ALL_FEED_ID) {
            articleRepository.saveAllStatusToRead()
            unreadCountRepository.readAll()
        } else {
            articleRepository.saveStatusToRead(feedId)
            unreadCountRepository.readAll(feedId)
        }
        if (preferenceHelper.allReadBack) {
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
            if (iconPath.isNotBlank() && iconPath != Feed.DEDAULT_ICON_PATH) {
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

    suspend fun onSwiped(direction: Int, touchedPosition: Int) = coroutineScope {
        val touchedArticle = allArticles[touchedPosition]
        when (direction) {
            LEFT -> {
                isSwipeRightToLeft = true
                when (preferenceHelper.swipeDirection) {
                    PreferenceHelper.SWIPE_RIGHT_TO_LEFT -> setReadStatusToTouchedView(touchedArticle, Article.TOREAD, preferenceHelper.allReadBack)
                    PreferenceHelper.SWIPE_LEFT_TO_RIGHT -> setReadStatusToTouchedView(touchedArticle, Article.UNREAD, preferenceHelper.allReadBack)
                    else -> {
                    }
                }
                isSwipeRightToLeft = false
            }
            RIGHT -> {
                isSwipeLeftToRight = true
                when (preferenceHelper.swipeDirection) {
                    PreferenceHelper.SWIPE_RIGHT_TO_LEFT -> setReadStatusToTouchedView(touchedArticle, Article.UNREAD, preferenceHelper.allReadBack)
                    PreferenceHelper.SWIPE_LEFT_TO_RIGHT -> setReadStatusToTouchedView(touchedArticle, Article.TOREAD, preferenceHelper.allReadBack)
                    else -> {
                    }
                }
                isSwipeLeftToRight = false
            }
        }
    }

}
