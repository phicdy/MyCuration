package com.phicdy.mycuration.articlelist

import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Feed
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.coroutineScope
import java.util.ArrayList
import java.util.Random

class ArticleListPresenter(private val feedId: Int,
                           private val preferenceHelper: PreferenceHelper,
                           private val articleRepository: ArticleRepository,
                           private val unreadCountRepository: UnreadCountRepository) {

    companion object {
        const val DEFAULT_CURATION_ID = -1
        private const val LOAD_COUNT = 100

        const val VIEW_TYPE_ARTICLE = 0
        const val VIEW_TYPE_FOOTER = 1
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

    private fun loadArticle(num: Int) {
        if (loadedPosition >= allArticles.size || num < 0) return
        loadedPosition += num
        if (loadedPosition >= allArticles.size - 1) loadedPosition = allArticles.size - 1
    }

    fun resume() {}

    fun pause() {
        disposable?.dispose()
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
        articleRepository.saveStatus(article.id, status)
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

    fun articleSize(): Int {
        return if (loadedPosition == allArticles.size - 1) allArticles.size else loadedPosition + 2
        // Index starts with 0 and add +1 for footer, so add 2
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
