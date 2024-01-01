package com.phicdy.mycuration.data.repository

import android.database.SQLException
import com.phicdy.mycuration.core.CoroutineDispatcherProvider
import com.phicdy.mycuration.data.Articles
import com.phicdy.mycuration.di.common.ApplicationCoroutineScope
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.FavoritableArticle
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.Filter
import com.phicdy.mycuration.repository.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val database: Database,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
    @ApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
) {

    // RSS ID to latest articles
    private val latestArticlesCache: ConcurrentHashMap<Int, List<Article>> = ConcurrentHashMap()

    suspend fun getAllArticlesInRss(rssId: Int, isNewestArticleTop: Boolean): List<Article> {
        return withContext(coroutineDispatcherProvider.io()) {
            database.articleQueries.transactionWithResult {
                if (isNewestArticleTop) {
                    database.articleQueries.getAllInRssOrderByDateDesc(rssId.toLong())
                        .executeAsList()
                        .map { it.toArticle() }
                } else {
                    database.articleQueries.getAllInRssOrderByDateAsc(rssId.toLong())
                        .executeAsList()
                        .map { it.toArticle() }
                }
            }
        }
    }

    suspend fun applyFiltersOfRss(filterList: List<Filter>, rssId: Int): Int {
        return withContext(applicationCoroutineScope.coroutineContext) {
            // If articles are hit in condition, Set articles status to "read"
            var updatedCount = 0
            for ((id, _, keyword, url) in filterList) {
                try {
                    // If keyword or url exists, add condition
                    if (keyword.isBlank() && url.isBlank()) {
                        Timber.w("Set filtering conditon, keyword and url don't exist fileter ID =$id")
                        continue
                    }

                    database.articleQueries.transaction {
                        if (keyword.isNotBlank()) {
                            if (url.isNotBlank()) {
                                database.articleQueries.updateReadStatusByTitleAndUrl(
                                    rssId.toLong(),
                                    Article.UNREAD,
                                    "%$keyword%",
                                    "%$url%"
                                )
                            } else {
                                database.articleQueries.updateReadStatusByTitle(
                                    rssId.toLong(),
                                    Article.UNREAD,
                                    "%$keyword%"
                                )
                            }
                        } else {
                            if (url.isNotBlank()) {
                                database.articleQueries.updateReadStatusByUrl(
                                    rssId.toLong(),
                                    Article.UNREAD,
                                    "%$url%"
                                )
                            }
                        }
                        updatedCount =
                            database.articleQueries.selectChanges().executeAsOne().toInt()
                    }
                } catch (e: Exception) {
                    Timber.e("Apply Filtering, article can't be updated.Feed ID = $rssId")
                    Timber.e(e)
                }
            }
            updatedCount
        }
    }

    /**
     * Save method for new articles
     *
     * @param articles Article array to save
     */
    suspend fun saveNewArticles(articles: List<Article>): List<Article> {
        return withContext(applicationCoroutineScope.coroutineContext) {
            val result = arrayListOf<Article>()
            try {
                database.articleQueries.transaction {
                    articles.forEach { article ->
                        database.articleQueries.insert(
                            article.title,
                            article.url,
                            article.status,
                            article.point,
                            article.postedDate,
                            article.feedId.toLong()
                        )
                        val id =
                            database.articleQueries.selectLastInsertRowId().executeAsOne().toInt()
                        result.add(
                            Article(
                                id = id,
                                title = article.title,
                                url = article.url,
                                status = article.status,
                                point = article.point,
                                feedIconPath = article.feedIconPath,
                                postedDate = article.postedDate,
                                feedId = article.feedId,
                                feedTitle = article.feedTitle
                            )
                        )
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            result
        }
    }

    /**
     * Check method of article existence of specified RSS ID.
     *
     * @param rssId RSS ID to check
     * @return `true` if exists.
     */
    suspend fun isExistArticleOf(rssId: Int): Boolean =
        withContext(coroutineDispatcherProvider.io()) {
            var isExist = false
            try {
                val count = database.articleQueries.transactionWithResult {
                    database.articleQueries.getCount(rssId.toLong()).executeAsOne()
                }
                isExist = count > 0
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return@withContext isExist
        }

    /**
     * Get article URLs that were stored in database from argument articles
     *
     */
    suspend fun getStoredUrlListIn(articles: List<Article>): List<String> =
        withContext(coroutineDispatcherProvider.io()) {
            try {
                return@withContext database.articleQueries.transactionWithResult<List<String>> {
                    database.articleQueries.getAllInUrl(articles.map { it.url }).executeAsList()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return@withContext emptyList()
        }

    /**
     * Update method for all of the articles of RSS ID to read status.
     *
     * @param rssId RSS ID for articles to change status to read
     */
    suspend fun saveStatusToRead(rssId: Int) =
        withContext(applicationCoroutineScope.coroutineContext) {
            try {
                database.articleQueries.transaction {
                    database.articleQueries.updateReadStatusByFeedId(rssId.toLong())
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

    /**
     * Update method for all of the articles to read status.
     */
    suspend fun saveAllStatusToRead() = withContext(applicationCoroutineScope.coroutineContext) {
        try {
            database.articleQueries.transaction {
                database.articleQueries.updateAllReadStatus()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    /**
     * Update method for article read/unread status.
     *
     * @param articleId Artilce ID to change status
     * @param status New status
     */
    suspend fun saveStatus(articleId: Int, status: String) =
        withContext(applicationCoroutineScope.coroutineContext) {
            try {
                database.articleQueries.transaction {
                    database.articleQueries.updateReadStatusById(status, articleId.toLong())
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

    /**
     * Update method for hatena point of the article.
     *
     * @param url Article URL to update
     * @param point New hatena point
     */
    suspend fun saveHatenaPoint(url: String, point: String) =
        withContext(applicationCoroutineScope.coroutineContext) {
            try {
                database.articleQueries.transaction {
                    database.articleQueries.updatePointByUrl(point, url)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

    suspend fun getAllUnreadArticles(isNewestArticleTop: Boolean): List<FavoritableArticle> =
        withContext(coroutineDispatcherProvider.io()) {
            try {
                return@withContext database.articleQueries.transactionWithResult<List<FavoritableArticle>> {
                    if (isNewestArticleTop) {
                        database.articleQueries.getAllUnreadArticlesOrderByDateDesc()
                            .executeAsList().map {
                                val isFavorite = if (it._id__ == null) false else it._id__ > 0
                                FavoritableArticle(
                                    it._id.toInt(), it.title, it.url, it.status, it.point,
                                    it.date, it.feedId.toInt(), it.title_, it.iconPath, isFavorite
                                )
                            }
                    } else {
                        database.articleQueries.getAllUnreadArticlesOrderByDateAsc().executeAsList()
                            .map {
                                val isFavorite = if (it._id__ == null) false else it._id__ > 0
                                FavoritableArticle(
                                    it._id.toInt(), it.title, it.url, it.status, it.point,
                                    it.date, it.feedId.toInt(), it.title_, it.iconPath, isFavorite
                                )
                            }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }

            return@withContext emptyList()
        }

    suspend fun getTop300Articles(isNewestArticleTop: Boolean): List<FavoritableArticle> =
        withContext(coroutineDispatcherProvider.io()) {
            try {
                return@withContext database.articleQueries.transactionWithResult<List<FavoritableArticle>> {
                    if (isNewestArticleTop) {
                        database.articleQueries.getTop300OrderByDateDesc().executeAsList().map {
                            val isFavorite = if (it._id__ == null) false else it._id__ > 0
                            FavoritableArticle(
                                it._id.toInt(), it.title, it.url, it.status, it.point,
                                it.date, it.feedId.toInt(), it.title_, it.iconPath, isFavorite
                            )
                        }
                    } else {
                        database.articleQueries.getTop300OrderByDateAsc().executeAsList().map {
                            val isFavorite = if (it._id__ == null) false else it._id__ > 0
                            FavoritableArticle(
                                it._id.toInt(), it.title, it.url, it.status, it.point,
                                it.date, it.feedId.toInt(), it.title_, it.iconPath, isFavorite
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
            return@withContext emptyList()
        }

    suspend fun searchArticles(
        keyword: String,
        isNewestArticleTop: Boolean
    ): List<FavoritableArticle> = withContext(coroutineDispatcherProvider.io()) {
        var searchKeyWord = keyword
        if (searchKeyWord.contains("%")) {
            searchKeyWord = searchKeyWord.replace("%", "$%")
        }
        if (searchKeyWord.contains("_")) {
            searchKeyWord = searchKeyWord.replace("_", "$" + "_")
        }
        try {
            return@withContext database.articleQueries.transactionWithResult<List<FavoritableArticle>> {
                if (isNewestArticleTop) {
                    database.articleQueries.searchArticleOrderByDateDesc("%$searchKeyWord%")
                        .executeAsList().map {
                            val isFavorite = if (it._id__ == null) false else it._id__ > 0
                            FavoritableArticle(
                                it._id.toInt(), it.title, it.url, it.status, it.point,
                                it.date, it.feedId.toInt(), it.title_, it.iconPath, isFavorite
                            )
                        }
                } else {
                    database.articleQueries.searchArticleOrderByDateAsc("%$searchKeyWord%")
                        .executeAsList().map {
                            val isFavorite = if (it._id__ == null) false else it._id__ > 0
                            FavoritableArticle(
                                it._id.toInt(), it.title, it.url, it.status, it.point,
                                it.date, it.feedId.toInt(), it.title_, it.iconPath, isFavorite
                            )
                        }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext emptyList()
    }

    suspend fun getAllArticlesOfRss(
        rssId: Int,
        isNewestArticleTop: Boolean
    ): List<FavoritableArticle> = withContext(coroutineDispatcherProvider.io()) {
        return@withContext database.articleQueries.transactionWithResult<List<FavoritableArticle>> {
            if (isNewestArticleTop) {
                database.articleQueries.getArticlesOfFeedsDesc(rssId.toLong()).executeAsList().map {
                    val isFavorite = if (it._id_ == null) false else it._id_ > 0
                    FavoritableArticle(
                        it._id.toInt(), it.title, it.url, it.status, it.point,
                        it.date, it.feedId.toInt(), "", "", isFavorite
                    )
                }
            } else {
                database.articleQueries.getArticlesOfFeedsAsc(rssId.toLong()).executeAsList().map {
                    val isFavorite = if (it._id_ == null) false else it._id_ > 0
                    FavoritableArticle(
                        it._id.toInt(), it.title, it.url, it.status, it.point,
                        it.date, it.feedId.toInt(), "", "", isFavorite
                    )
                }
            }
        }
    }

    suspend fun getUnreadArticlesOfRss(
        rssId: Int,
        isNewestArticleTop: Boolean
    ): List<FavoritableArticle> = withContext(coroutineDispatcherProvider.io()) {
        return@withContext database.articleQueries.transactionWithResult<List<FavoritableArticle>> {
            if (isNewestArticleTop) {
                database.articleQueries.getUnreadArticlesOfFeedsDesc(rssId.toLong()).executeAsList()
                    .map {
                        val isFavorite = if (it._id_ == null) false else it._id_ > 0
                        FavoritableArticle(
                            it._id.toInt(), it.title, it.url, it.status, it.point,
                            it.date, it.feedId.toInt(), "", "", isFavorite
                        )
                    }
            } else {
                database.articleQueries.getUnreadArticlesOfFeedsAsc(rssId.toLong()).executeAsList()
                    .map {
                        val isFavorite = if (it._id_ == null) false else it._id_ > 0
                        FavoritableArticle(
                            it._id.toInt(), it.title, it.url, it.status, it.point,
                            it.date, it.feedId.toInt(), "", "", isFavorite
                        )
                    }
            }
        }
    }

    suspend fun getUnreadArticleCount(rssId: Int): Int =
        withContext(coroutineDispatcherProvider.io()) {
            try {
                return@withContext database.articleQueries.transactionWithResult<Int> {
                    database.articleQueries.getUnreadCount(rssId.toLong()).executeAsOne().toInt()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return@withContext -1
        }

    suspend fun getAllUnreadArticlesOfCuration(
        curationId: Int,
        isNewestArticleTop: Boolean
    ): List<Article> = withContext(coroutineDispatcherProvider.io()) {
        try {
            return@withContext if (isNewestArticleTop) {
                database.articleQueries.getUnreadArticlesOfCurationDesc(curationId.toLong())
                    .executeAsList().map {
                        Article(
                            it._id.toInt(), it.title, it.url, it.status, it.point,
                            it.date, it.feedId.toInt(), it.title_, it.iconPath
                        )
                    }
            } else {
                database.articleQueries.getUnreadArticlesOfCurationAsc(curationId.toLong())
                    .executeAsList().map {
                        Article(
                            it._id.toInt(), it.title, it.url, it.status, it.point,
                            it.date, it.feedId.toInt(), it.title_, it.iconPath
                        )
                    }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        return@withContext emptyList()
    }

    suspend fun getAllArticlesOfCuration(
        curationId: Int,
        isNewestArticleTop: Boolean
    ): List<Article> = withContext(coroutineDispatcherProvider.io()) {
        try {
            return@withContext if (isNewestArticleTop) {
                database.articleQueries.getArticlesOfCurationDesc(curationId.toLong())
                    .executeAsList().map {
                        Article(
                            it._id.toInt(), it.title, it.url, it.status, it.point,
                            it.date, it.feedId.toInt(), it.title_, it.iconPath
                        )
                    }
            } else {
                database.articleQueries.getArticlesOfCurationAsc(curationId.toLong())
                    .executeAsList().map {
                        Article(
                            it._id.toInt(), it.title, it.url, it.status, it.point,
                            it.date, it.feedId.toInt(), it.title_, it.iconPath
                        )
                    }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        return@withContext emptyList()
    }

    suspend fun deleteAll() {
        withContext(coroutineDispatcherProvider.io()) {
            database.articleQueries.transaction {
                database.articleQueries.deleteAll()
            }
        }
    }

    suspend fun getLatestArticlesCache(feed: Feed): List<Article> =
        withContext(coroutineDispatcherProvider.io()) {
            return@withContext latestArticlesCache[feed.id] ?: emptyList()
        }

    suspend fun updateLatestArticlesCache(feed: Feed, articles: List<Article>) {
        withContext(coroutineDispatcherProvider.io()) {
            latestArticlesCache[feed.id] = articles
        }
    }

    private fun Articles.toArticle(): Article = Article(
        id = _id.toInt(),
        title = title,
        url = url,
        status = status,
        point = point,
        postedDate = date,
        feedId = feedId.toInt(),
        feedTitle = "",
        feedIconPath = ""
    )
}