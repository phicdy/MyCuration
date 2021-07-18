package com.phicdy.mycuration.data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.core.CoroutineDispatcherProvider
import com.phicdy.mycuration.data.Articles
import com.phicdy.mycuration.di.common.ApplicationCoroutineScope
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.CurationSelection
import com.phicdy.mycuration.entity.FavoritableArticle
import com.phicdy.mycuration.entity.FavoriteArticle
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.Filter
import com.phicdy.mycuration.repository.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
        val db: SQLiteDatabase,
        private val database: Database,
        private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
        @ApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
) {

    suspend fun getAllArticlesInRss(rssId: Int, isNewestArticleTop: Boolean): List<Article> {
        return withContext(coroutineDispatcherProvider.io()) {
            database.transactionWithResult {
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

    suspend fun applyFiltersOfRss(filterList: ArrayList<Filter>, rssId: Int): Int {
        return withContext(applicationCoroutineScope.coroutineContext) {
            // If articles are hit in condition, Set articles status to "read"
            val value = ContentValues().apply {
                put(Article.STATUS, Article.READ)
            }
            var updatedCount = 0
            for ((id, _, keyword, url) in filterList) {
                try {
                    // If keyword or url exists, add condition
                    if (keyword.isBlank() && url.isBlank()) {
                        Timber.w("Set filtering conditon, keyword and url don't exist fileter ID =$id")
                        continue
                    }

                    database.transaction {
                        if (keyword.isNotBlank()) {
                            if (url.isNotBlank()) {
                                database.articleQueries.updateReadStatusByTitleAndUrl(rssId.toLong(), Article.UNREAD, "%$keyword%", "%$url%")
                            } else {
                                database.articleQueries.updateReadStatusByTitle(rssId.toLong(), Article.UNREAD, "%$keyword%")
                            }
                        } else {
                            if (url.isNotBlank()) {
                                database.articleQueries.updateReadStatusByUrl(rssId.toLong(), Article.UNREAD, "%$url%")
                            }
                        }
                        updatedCount = database.articleQueries.selectChanges().executeAsOne().toInt()
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
     * @param feedId Feed ID of the articles
     */
    suspend fun saveNewArticles(articles: List<Article>, feedId: Int): List<Article> {
        return withContext(applicationCoroutineScope.coroutineContext) {
            val result = arrayListOf<Article>()
            try {
                database.transaction {
                    articles.forEach { article ->
                        database.articleQueries.insert(article.title, article.url, article.status, article.point, article.postedDate, feedId.toLong())
                        val id = database.articleQueries.selectLastInsertRowId().executeAsOne().toInt()
                        result.add(Article(
                                id = id,
                                title = article.title,
                                url = article.url,
                                status = article.status,
                                point = article.point,
                                feedIconPath = article.feedIconPath,
                                postedDate = article.postedDate,
                                feedId = article.feedId,
                                feedTitle = article.feedTitle)
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
    suspend fun isExistArticleOf(rssId: Int): Boolean = withContext(coroutineDispatcherProvider.io()) {
        var isExist = false
        try {
            val count = database.transactionWithResult<Long> {
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
    suspend fun getStoredUrlListIn(articles: List<Article>): List<String> = withContext(coroutineDispatcherProvider.io()) {
        try {
            return@withContext database.transactionWithResult<List<String>> {
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
    suspend fun saveStatusToRead(rssId: Int) = withContext(applicationCoroutineScope.coroutineContext) {
        try {
            database.transaction {
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
            database.transaction {
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
    suspend fun saveStatus(articleId: Int, status: String) = withContext(applicationCoroutineScope.coroutineContext) {
        try {
            database.transaction {
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
    suspend fun saveHatenaPoint(url: String, point: String) = withContext(applicationCoroutineScope.coroutineContext) {
        try {
            database.transaction {
                database.articleQueries.updatePointByUrl(point, url)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    suspend fun getAllUnreadArticles(isNewestArticleTop: Boolean): List<FavoritableArticle> = withContext(coroutineDispatcherProvider.io()) {
        try {
            return@withContext database.transactionWithResult<List<FavoritableArticle>> {
                if (isNewestArticleTop) {
                    database.articleQueries.getAllUnreadArticlesOrderByDateDesc().executeAsList().map {
                        val isFavorite = if (it._id__ == null) false else it._id__ > 0
                        FavoritableArticle(it._id.toInt(), it.title, it.url, it.status, it.point,
                                it.date, it.feedId.toInt(), it.title_, it.iconPath, isFavorite)
                    }
                } else {
                    database.articleQueries.getAllUnreadArticlesOrderByDateAsc().executeAsList().map {
                        val isFavorite = if (it._id__ == null) false else it._id__ > 0
                        FavoritableArticle(it._id.toInt(), it.title, it.url, it.status, it.point,
                                it.date, it.feedId.toInt(), it.title_, it.iconPath, isFavorite)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        return@withContext emptyList()
    }

    suspend fun getTop300Articles(isNewestArticleTop: Boolean): List<FavoritableArticle> = withContext(coroutineDispatcherProvider.io()) {
        val articles = mutableListOf<FavoritableArticle>()
        var cursor: Cursor? = null
        try {
            // Get unread articles
            val sql = StringBuilder().apply {
                append("select ")
                append("${Article.TABLE_NAME}.${Article.ID},")
                append("${Article.TABLE_NAME}.${Article.TITLE},")
                append("${Article.TABLE_NAME}.${Article.URL},")
                append("${Article.TABLE_NAME}.${Article.STATUS},")
                append("${Article.TABLE_NAME}.${Article.POINT},")
                append("${Article.TABLE_NAME}.${Article.DATE},")
                append("${Article.TABLE_NAME}.${Article.FEEDID},")
                append("${Feed.TABLE_NAME}.${Feed.TITLE},")
                append("${Feed.TABLE_NAME}.${Feed.ICON_PATH},")
                append("${FavoriteArticle.TABLE_NAME}.${FavoriteArticle.ID} ")
                append("from ")
                append("${Article.TABLE_NAME} ")
                append("inner join ${Feed.TABLE_NAME} ")
                append("left outer join ${FavoriteArticle.TABLE_NAME} ")
                append("on ")
                append("(${Article.TABLE_NAME}.${Article.ID} = ${FavoriteArticle.TABLE_NAME}.${FavoriteArticle.ARTICLE_ID}) ")
                append("where ")
                append("${Article.TABLE_NAME}.${Article.FEEDID} = ${Feed.TABLE_NAME}.${Feed.ID} ")
                append("order by ${Article.DATE} ")
                append(if (isNewestArticleTop) "desc " else "asc ")
                append("limit 300")
            }.toString()
            db.beginTransaction()
            cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val title = cursor.getString(1)
                val url = cursor.getString(2)
                val status = cursor.getString(3)
                val point = cursor.getString(4)
                val dateLong = cursor.getLong(5)
                val feedId = cursor.getInt(6)
                val feedTitle = cursor.getString(7)
                val feedIconPath = cursor.getString(8)
                val favoriteId = cursor.getInt(9)
                val article = FavoritableArticle(id, title, url, status, point,
                        dateLong, feedId, feedTitle, feedIconPath, favoriteId > 0)
                articles.add(article)
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            cursor?.close()
            db.endTransaction()
        }
        return@withContext articles
    }

    suspend fun searchArticles(keyword: String, isNewestArticleTop: Boolean): List<FavoritableArticle> = withContext(coroutineDispatcherProvider.io()) {
        var searchKeyWord = keyword
        val articles = mutableListOf<FavoritableArticle>()
        if (searchKeyWord.contains("%")) {
            searchKeyWord = searchKeyWord.replace("%", "$%")
        }
        if (searchKeyWord.contains("_")) {
            searchKeyWord = searchKeyWord.replace("_", "$" + "_")
        }
        var columns = arrayOf(
                Article.ID, Article.TITLE, Article.URL, Article.STATUS, Article.POINT, Article.DATE
        ).joinToString(postfix = ", ") {
            Article.TABLE_NAME + "." + it
        }
        columns += arrayOf(Feed.TITLE, Feed.ICON_PATH).joinToString { Feed.TABLE_NAME + "." + it }
        var sql = "select " + columns + ", ${FavoriteArticle.TABLE_NAME}.${FavoriteArticle.ID}" +
                " from " + Article.TABLE_NAME + " inner join " + Feed.TABLE_NAME +
                " left outer join ${FavoriteArticle.TABLE_NAME}" +
                " on " +
                "(${Article.TABLE_NAME}.${Article.ID} = ${FavoriteArticle.TABLE_NAME}.${FavoriteArticle.ARTICLE_ID}) " +
                " where " + Article.TABLE_NAME + "." + Article.TITLE + " like '%" + searchKeyWord + "%' escape '$' and " +
                Article.TABLE_NAME + "." + Article.FEEDID + " = " + Feed.TABLE_NAME + "." + Feed.ID +
                " order by " + Article.DATE
        sql += if (isNewestArticleTop) {
            " desc"
        } else {
            " asc"
        }

        var cursor: Cursor? = null
        try {
            db.beginTransaction()
            cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val title = cursor.getString(1)
                val url = cursor.getString(2)
                val status = cursor.getString(3)
                val point = cursor.getString(4)
                val dateLong = cursor.getLong(5)
                val feedTitle = cursor.getString(6)
                val feedIconPath = cursor.getString(7)
                val favoriteId = cursor.getInt(8)
                val article = FavoritableArticle(id, title, url, status, point,
                        dateLong, 0, feedTitle, feedIconPath, favoriteId > 0)
                articles.add(article)
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.endTransaction()
        }

        return@withContext articles
    }

    suspend fun getAllArticlesOfRss(rssId: Int, isNewestArticleTop: Boolean): List<FavoritableArticle> {
        return getArticlesOfRss(rssId, null, isNewestArticleTop)
    }

    suspend fun getUnreadArticlesOfRss(rssId: Int, isNewestArticleTop: Boolean): List<FavoritableArticle> {
        return getArticlesOfRss(rssId, Article.UNREAD, isNewestArticleTop)
    }

    suspend fun getUnreadArticleCount(rssId: Int): Int = withContext(coroutineDispatcherProvider.io()) {
        var cursor: Cursor? = null
        var count = -1
        try {
            val selection = "${Article.FEEDID} = $rssId and ${Article.STATUS} = '${Article.UNREAD}'"
            db.beginTransaction()
            cursor = db.query(Article.TABLE_NAME, arrayOf(Article.ID), selection, null, null, null, null)
            count = cursor.count
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.endTransaction()
        }
        return@withContext count
    }

    private suspend fun getArticlesOfRss(rssId: Int, searchStatus: String?, isNewestArticleTop: Boolean): List<FavoritableArticle> = withContext(coroutineDispatcherProvider.io()) {
        val articles = mutableListOf<FavoritableArticle>()
        val sql = StringBuilder().apply {
            append("select ")
            append("${Article.TABLE_NAME}.${Article.ID},")
            append("${Article.TABLE_NAME}.${Article.TITLE},")
            append("${Article.TABLE_NAME}.${Article.URL},")
            append("${Article.TABLE_NAME}.${Article.STATUS},")
            append("${Article.TABLE_NAME}.${Article.POINT},")
            append("${Article.TABLE_NAME}.${Article.DATE},")
            append("${FavoriteArticle.TABLE_NAME}.${FavoriteArticle.ID} ")
            append("from ")
            append("${Article.TABLE_NAME} ")
            append("left outer join ${FavoriteArticle.TABLE_NAME} ")
            append("on ")
            append("(${Article.TABLE_NAME}.${Article.ID} = ${FavoriteArticle.TABLE_NAME}.${FavoriteArticle.ARTICLE_ID}) ")
            append("where ")
            if (!searchStatus.isNullOrBlank()) append("${Article.TABLE_NAME}.${Article.STATUS} = \"$searchStatus\" and ")
            append("${Article.TABLE_NAME}.${Article.FEEDID} = $rssId ")
            append("order by ${Article.DATE} ")
            append(if (isNewestArticleTop) "desc" else "asc")
        }.toString()
        var cursor: Cursor? = null
        try {
            db.beginTransaction()
            cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val title = cursor.getString(1)
                val url = cursor.getString(2)
                val status = cursor.getString(3)
                val point = cursor.getString(4)
                val dateLong = cursor.getLong(5)
                val favoriteId = cursor.getInt(6)
                val article = FavoritableArticle(id, title, url, status, point,
                        dateLong, rssId, "", "", favoriteId > 0)
                articles.add(article)
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            cursor?.close()
            db.endTransaction()
        }

        return@withContext articles
    }

    suspend fun getAllUnreadArticlesOfCuration(curationId: Int, isNewestArticleTop: Boolean): ArrayList<Article> = withContext(coroutineDispatcherProvider.io()) {
        val articles = arrayListOf<Article>()
        var sql = "select " + Article.TABLE_NAME + "." + Article.ID + "," +
                Article.TABLE_NAME + "." + Article.TITLE + "," +
                Article.TABLE_NAME + "." + Article.URL + "," +
                Article.TABLE_NAME + "." + Article.STATUS + "," +
                Article.TABLE_NAME + "." + Article.POINT + "," +
                Article.TABLE_NAME + "." + Article.DATE + "," +
                Article.TABLE_NAME + "." + Article.FEEDID + "," +
                Feed.TABLE_NAME + "." + Feed.TITLE + "," +
                Feed.TABLE_NAME + "." + Feed.ICON_PATH +
                " from (" + Article.TABLE_NAME + " inner join " + CurationSelection.TABLE_NAME +
                " on " + CurationSelection.CURATION_ID + " = " + curationId + " and " +
                Article.TABLE_NAME + "." + Article.STATUS + " = '" + Article.UNREAD + "' and " +
                Article.TABLE_NAME + "." + Article.ID + " = " + CurationSelection.TABLE_NAME + "." + CurationSelection.ARTICLE_ID + ")" +
                " inner join " + Feed.TABLE_NAME +
                " on " + Article.TABLE_NAME + "." + Article.FEEDID + " = " + Feed.TABLE_NAME + "." + Feed.ID +
                " order by " + Article.DATE
        sql += if (isNewestArticleTop) {
            " desc"
        } else {
            " asc"
        }
        var cursor: Cursor? = null
        try {
            db.beginTransaction()
            cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val title = cursor.getString(1)
                val url = cursor.getString(2)
                val status = cursor.getString(3)
                val point = cursor.getString(4)
                val dateLong = cursor.getLong(5)
                val feedId = cursor.getInt(6)
                val feedTitle = cursor.getString(7)
                val feedIconPath = cursor.getString(8)
                val article = Article(id, title, url, status, point,
                        dateLong, feedId, feedTitle, feedIconPath)
                articles.add(article)
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            cursor?.close()
            db.endTransaction()
        }

        return@withContext articles
    }

    suspend fun getAllArticlesOfCuration(curationId: Int, isNewestArticleTop: Boolean): ArrayList<Article> = withContext(coroutineDispatcherProvider.io()) {
        val articles = arrayListOf<Article>()
        var sql = "select " + Article.TABLE_NAME + "." + Article.ID + "," +
                Article.TABLE_NAME + "." + Article.TITLE + "," +
                Article.TABLE_NAME + "." + Article.URL + "," +
                Article.TABLE_NAME + "." + Article.STATUS + "," +
                Article.TABLE_NAME + "." + Article.POINT + "," +
                Article.TABLE_NAME + "." + Article.DATE + "," +
                Article.TABLE_NAME + "." + Article.FEEDID + "," +
                Feed.TABLE_NAME + "." + Feed.TITLE + "," +
                Feed.TABLE_NAME + "." + Feed.ICON_PATH +
                " from (" + Article.TABLE_NAME + " inner join " + CurationSelection.TABLE_NAME +
                " on " + CurationSelection.CURATION_ID + " = " + curationId + " and " +
                Article.TABLE_NAME + "." + Article.ID + " = " + CurationSelection.TABLE_NAME + "." + CurationSelection.ARTICLE_ID + ")" +
                " inner join " + Feed.TABLE_NAME +
                " on " + Article.TABLE_NAME + "." + Article.FEEDID + " = " + Feed.TABLE_NAME + "." + Feed.ID +
                " order by " + Article.DATE
        sql += if (isNewestArticleTop) {
            " desc"
        } else {
            " asc"
        }
        var cursor: Cursor? = null
        try {
            db.beginTransaction()
            cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val title = cursor.getString(1)
                val url = cursor.getString(2)
                val status = cursor.getString(3)
                val point = cursor.getString(4)
                val dateLong = cursor.getLong(5)
                val feedId = cursor.getInt(6)
                val feedTitle = cursor.getString(7)
                val feedIconPath = cursor.getString(8)
                val article = Article(id, title, url, status, point,
                        dateLong, feedId, feedTitle, feedIconPath)
                articles.add(article)
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            cursor?.close()
            db.endTransaction()
        }

        return@withContext articles
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