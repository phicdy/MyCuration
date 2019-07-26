package com.phicdy.mycuration.data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.CurationSelection
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.Filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber

class ArticleRepository(val db: SQLiteDatabase) {

    /**
     * Update method from "to read" to "read" for all of the articles.
     */
    suspend fun saveAllStatusToReadFromToRead() = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(Article.STATUS, Article.READ)
                val condition = Article.STATUS + " = '" + Article.TOREAD + "'"
                db.update(Article.TABLE_NAME, values, condition, null)
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                db.endTransaction()
            }
        }
    }

    suspend fun getAllArticlesInRss(rssId: Int, isNewestArticleTop: Boolean): ArrayList<Article> = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            val articles = ArrayList<Article>()
            var cursor: Cursor? = null
            try {
                db.beginTransaction()
                // Get unread articles
                val sql = ("select " + Article.ID + ", " + Article.TITLE + ", " + Article.URL + ", " + Article.STATUS + "" +
                        ", " + Article.POINT + ", " + Article.DATE + " from " + Article.TABLE_NAME + " where " + Article.FEEDID + " = "
                        + rssId + " order by " + Article.DATE) + if (isNewestArticleTop) " desc" else " asc"
                cursor = db.rawQuery(sql, null)
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(0)
                    val title = cursor.getString(1)
                    val url = cursor.getString(2)
                    val status = cursor.getString(3)
                    val point = cursor.getString(4)
                    val dateLong = cursor.getLong(5)
                    val article = Article(id, title, url, status, point,
                            dateLong, rssId, "", "")
                    articles.add(article)
                }
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                return@withContext articles
            } finally {
                db.endTransaction()
                cursor?.close()
            }

            return@withContext articles
        }
    }

    suspend fun applyFiltersOfRss(filterList: ArrayList<Filter>, rssId: Int): Int = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
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

                    db.beginTransaction()
                    // Initialize condition
                    var condition = Article.FEEDID + " = $rssId and " + Article.STATUS + " = '" + Article.UNREAD + "'"
                    if (keyword.isNotBlank()) {
                        condition = "$condition and title like '%$keyword%'"
                    }
                    if (url.isNotBlank()) {
                        condition = "$condition and url like '%$url%'"
                    }
                    updatedCount += db.update(Article.TABLE_NAME, value, condition, null)
                    db.setTransactionSuccessful()
                } catch (e: Exception) {
                    Timber.e("Apply Filtering, article can't be updated.Feed ID = $rssId")
                    Timber.e(e)
                } finally {
                    db.endTransaction()
                }
            }
            return@withContext updatedCount
        }
    }

    /**
     * Save method for new articles
     *
     * @param articles Article array to save
     * @param feedId Feed ID of the articles
     */
    suspend fun saveNewArticles(articles: List<Article>, feedId: Int): List<Article> = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            if (articles.isEmpty()) {
                return@withContext emptyList<Article>()
            }
            val insertArticleSt = db.compileStatement(
                    "insert into articles(title,url,status,point,date,feedId) values (?,?,?,?,?,?);")
            val result = arrayListOf<Article>()
            try {
                db.beginTransaction()
                articles.forEach { article ->
                    insertArticleSt.bindString(1, article.title)
                    insertArticleSt.bindString(2, article.url)
                    insertArticleSt.bindString(3, article.status)
                    insertArticleSt.bindString(4, article.point)
                    insertArticleSt.bindLong(5, article.postedDate)
                    insertArticleSt.bindString(6, feedId.toString())
                    val id = insertArticleSt.executeInsert().toInt()
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
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                db.endTransaction()
            }
            return@withContext result
        }
    }

    /**
     * Check method of article existence of specified RSS ID.
     *
     * @param rssId RSS ID to check
     * @return `true` if exists.
     */
    suspend fun isExistArticleOf(rssId: Int? = null): Boolean = withContext(Dispatchers.IO) {
        var isExist = false
        db.beginTransaction()
        var cursor: Cursor? = null
        try {
            val selection = if (rssId == null) null else Article.FEEDID + " = " + rssId
            cursor = db.query(Article.TABLE_NAME, arrayOf(Article.ID), selection, null, null, null, null, "1")
            isExist = cursor.count > 0
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.endTransaction()
        }
        return@withContext isExist
    }

    /**
     * Get article URLs that were stored in database from argument articles
     *
     */
    suspend fun getStoredUrlListIn(articles: List<Article>): List<String> = withContext(Dispatchers.IO) {
        val urls = mutableListOf<String>()
        db.beginTransaction()
        var cursor: Cursor? = null
        try {
            val selection = StringBuffer().apply {
                append(Article.URL)
                append(" in (")
                articles.map { append("'" + it.url + "', ") }
                delete(length - 2, length)
                append(")")
            }.toString()
            cursor = db.query(true, Article.TABLE_NAME, arrayOf(Article.URL), selection, null, null, null, null, null)
            while (cursor.moveToNext()) {
                urls.add(cursor.getString(0))
            }
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.endTransaction()
        }
        return@withContext urls
    }

    /**
     * Check method of article existence.
     *
     * @return `true` if there is an article or more.
     */
    suspend fun isExistArticle(): Boolean = coroutineScope {
        return@coroutineScope isExistArticleOf(null)
    }

    /**
     * Update method for all of the articles of RSS ID to read status.
     *
     * @param rssId RSS ID for articles to change status to read
     */
    suspend fun saveStatusToRead(rssId: Int) = withContext(Dispatchers.IO) {
        try {
            db.beginTransaction()
            val values = ContentValues().apply {
                put(Article.STATUS, Article.READ)
            }
            val whereClause = Article.FEEDID + " = " + rssId
            db.update(Article.TABLE_NAME, values, whereClause, null)
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Update method for all of the articles to read status.
     */
    suspend fun saveAllStatusToRead() = withContext(Dispatchers.IO) {
        try {
            db.beginTransaction()
            val values = ContentValues().apply {
                put(Article.STATUS, Article.READ)
            }
            db.update(Article.TABLE_NAME, values, null, null)
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Update method for article read/unread status.
     *
     * @param articleId Artilce ID to change status
     * @param status New status
     */
    suspend fun saveStatus(articleId: Int, status: String) = withContext(Dispatchers.IO) {
        try {
            db.beginTransaction()
            val values = ContentValues().apply {
                put(Article.STATUS, status)
            }
            db.update(Article.TABLE_NAME, values, Article.ID + " = " + articleId, null)
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Update method for hatena point of the article.
     *
     * @param url Article URL to update
     * @param point New hatena point
     */
    suspend fun saveHatenaPoint(url: String, point: String) = withContext(Dispatchers.IO) {
        try {
            db.beginTransaction()
            val values = ContentValues().apply {
                put(Article.POINT, point)
            }
            db.update(Article.TABLE_NAME, values, Article.URL + " = '" + url + "'", null)
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    suspend fun getAllUnreadArticles(isNewestArticleTop: Boolean): ArrayList<Article> = withContext(Dispatchers.IO) {
        val articles = arrayListOf<Article>()
        var cursor: Cursor? = null
        try {
            // Get unread articles
            var sql = "select articles._id,articles.title,articles.url,articles.point,articles.date,articles.feedId,feeds.title,feeds.iconPath " +
                    "from articles inner join feeds " +
                    "where articles.status = \"unread\" and articles.feedId = feeds._id " +
                    "order by date "
            sql += if (isNewestArticleTop) {
                "desc"
            } else {
                "asc"
            }
            db.beginTransaction()
            cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val title = cursor.getString(1)
                val url = cursor.getString(2)
                val status = Article.UNREAD
                val point = cursor.getString(3)
                val dateLong = cursor.getLong(4)
                val feedId = cursor.getInt(5)
                val feedTitle = cursor.getString(6)
                val feedIconPath = cursor.getString(7)
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

    suspend fun getTop300Articles(isNewestArticleTop: Boolean): ArrayList<Article> = withContext(Dispatchers.IO) {
        val articles = arrayListOf<Article>()
        var cursor: Cursor? = null
        try {
            // Get unread articles
            var sql = "select articles._id,articles.title,articles.url,articles.status,articles.point,articles.date,articles.feedId,feeds.title,feeds.iconPath " +
                    "from articles inner join feeds " +
                    "where articles.feedId = feeds._id " +
                    "order by articles._id "
            sql += if (isNewestArticleTop) {
                "desc"
            } else {
                "asc"
            }
            sql += " limit 300"
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

    suspend fun searchArticles(keyword: String, isNewestArticleTop: Boolean): ArrayList<Article> = withContext(Dispatchers.IO) {
        var searchKeyWord = keyword
        val articles = arrayListOf<Article>()
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
        var sql = "select " + columns +
                " from " + Article.TABLE_NAME + " inner join " + Feed.TABLE_NAME +
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
                val article = Article(id, title, url, status, point,
                        dateLong, 0, feedTitle, feedIconPath)
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

    suspend fun getAllArticlesOfRss(rssId: Int, isNewestArticleTop: Boolean): ArrayList<Article> {
        return getArticlesOfRss(rssId, null, isNewestArticleTop)
    }

    suspend fun getUnreadArticlesOfRss(rssId: Int, isNewestArticleTop: Boolean): ArrayList<Article> {
        return getArticlesOfRss(rssId, Article.UNREAD, isNewestArticleTop)
    }

    suspend fun getUnreadArticleCount(rssId: Int): Int = withContext(Dispatchers.IO) {
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

    private suspend fun getArticlesOfRss(rssId: Int, searchStatus: String?, isNewestArticleTop: Boolean): ArrayList<Article> = withContext(Dispatchers.IO) {
        val articles = arrayListOf<Article>()
        var cursor: Cursor? = null
        val columns = arrayOf(
                Article.ID,
                Article.TITLE,
                Article.URL,
                Article.STATUS,
                Article.POINT,
                Article.DATE
        )
        val selection = Article.FEEDID + " = " + rssId +
                if (searchStatus == null) "" else " and " + Article.STATUS + " = '" + searchStatus + "'"
        val orderBy = Article.DATE + if (isNewestArticleTop) " desc" else " asc"
        try {
            db.beginTransaction()
            cursor = db.query(Article.TABLE_NAME, columns, selection, null, null, null, orderBy)
            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val title = cursor.getString(1)
                val url = cursor.getString(2)
                val status = cursor.getString(3)
                val point = cursor.getString(4)
                val dateLong = cursor.getLong(5)
                val article = Article(id, title, url, status, point,
                        dateLong, rssId, "", "")
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

    suspend fun getAllUnreadArticlesOfCuration(curationId: Int, isNewestArticleTop: Boolean): ArrayList<Article> = withContext(Dispatchers.IO) {
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

    suspend fun getAllArticlesOfCuration(curationId: Int, isNewestArticleTop: Boolean): ArrayList<Article> = withContext(Dispatchers.IO) {
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
}