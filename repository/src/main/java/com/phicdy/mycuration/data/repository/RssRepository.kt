package com.phicdy.mycuration.data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.core.CoroutineDispatcherProvider
import com.phicdy.mycuration.di.common.ApplicationCoroutineScope
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.repository.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RssRepository @Inject constructor(
        private val db: SQLiteDatabase,
        private val database: Database,
        private val articleRepository: ArticleRepository,
        private val filterRepository: FilterRepository,
        @ApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
        private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) {

    fun getNumOfRss(): Long =
            database.feedQueries.getNumOfRss()
                    .executeAsOne()

    /**
     * Update method for rss title.
     *
     * @param rssId RSS ID to update
     * @param newTitle New rss title
     * @return Num of updated rss
     */
    suspend fun saveNewTitle(rssId: Int, newTitle: String): Int {
        return withContext(coroutineDispatcherProvider.io()) {
            withContext(applicationCoroutineScope.coroutineContext) {
                database.transactionWithResult {
                    database.feedQueries.updateTitle(newTitle, rssId.toLong())
                    database.feedQueries.selectChanges().executeAsOne().toInt()
                }
            }
        }
    }

    /**
     * Delete method for rss and related data.
     *
     * @param rssId Feed ID to delete
     * @return result of delete
     */
    suspend fun deleteRss(rssId: Int): Boolean {
        return withContext(coroutineDispatcherProvider.io()) {
            withContext(applicationCoroutineScope.coroutineContext) {
                val allArticlesInRss = articleRepository.getAllArticlesInRss(rssId, true)
                val filters = filterRepository.getAllFilters()
                database.transactionWithResult {
                    for (article in allArticlesInRss) {
                        database.curationSelectionQueries.delete(article.id.toLong())
                        database.favoriteArticleQueries.delete(article.id.toLong())
                    }
                    database.articleQueries.deleteByFeedId(rssId.toLong())

                    // Delete related filter
                    database.filterFeedRegistrationQueries.deleteByFeedId(rssId.toLong())
                    for (filter in filters) {
                        val rsss = filter.feeds
                        if (rsss.size == 1 && rsss[0].id == rssId) {
                            // This filter had relation with this rss only
                            database.filtersQueries.delete(filter.id.toLong())
                        }
                    }

                    database.feedQueries.delete(rssId.toLong())
                    database.feedQueries.selectChanges().executeAsOne().toInt() == 1
                }
            }
        }
    }

    /**
     * Get method to all of feeds
     *
     * @return Feed list
     */
    suspend fun getAllFeeds(): List<Feed> = withContext(coroutineDispatcherProvider.io()) {
        return@withContext database.transactionWithResult<List<Feed>> {
            database.feedQueries.getAllFeeds()
                    .executeAsList()
                    .map {
                        Feed(
                                id = it._id.toInt(),
                                title = it.title,
                                url = it.url,
                                iconPath = it.iconPath,
                                format = it.format,
                                unreadAriticlesCount = it.unreadArticle.toInt(),
                                siteUrl = it.siteUrl
                        )
                    }
        }
    }

    /**
     * Update method for unread article count of the feed.
     *
     * @param feedId Feed ID to change
     * @param unreadCount New article unread count
     */
    suspend fun updateUnreadArticleCount(feedId: Int, unreadCount: Int) = withContext(coroutineDispatcherProvider.io()) {
        withContext(applicationCoroutineScope.coroutineContext) {
            try {
                db.beginTransaction()
                val values = ContentValues().apply {
                    put(Feed.UNREAD_ARTICLE, unreadCount)
                }
                db.update(Feed.TABLE_NAME, values, Feed.ID + " = $feedId", null)
                db.setTransactionSuccessful()
                Timber.d("Finished to update unread article count to $unreadCount in DB. RSS ID is $feedId")
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                db.endTransaction()
            }
        }
    }

    /**
     * Update method for feed icon path.
     *
     * @param siteUrl Site URL of the feed to change
     * @param iconPath New icon path
     */
    suspend fun saveIconPath(siteUrl: String, iconPath: String) = withContext(coroutineDispatcherProvider.io()) {
        withContext(applicationCoroutineScope.coroutineContext) {
            try {
                db.beginTransaction()
                val values = ContentValues().apply {
                    put(Feed.ICON_PATH, iconPath)
                }
                db.update(Feed.TABLE_NAME, values, Feed.SITE_URL + " = '" + siteUrl + "'", null)
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                db.endTransaction()
            }
        }
    }

    /**
     * @return Stored RSS or null if failed or same RSS exist
     */
    suspend fun store(feedTitle: String, feedUrl: String, format: String, siteUrl: String): Feed? = withContext(coroutineDispatcherProvider.io()) {
        withContext(applicationCoroutineScope.coroutineContext) {
            var rss: Feed? = null
            var isBeginTransaction = false
            try {
                // Get same feeds from DB
                val selection = Feed.TITLE + "=\"$feedTitle\" and " + Feed.URL + "=\"$feedUrl\" and " +
                        Feed.FORMAT + "=\"$format\""
                val stored = query(arrayOf(Feed.ID), selection)
                if (stored == null) {
                    // If there aren't same feeds in DB,Insert into DB
                    db.beginTransaction()
                    isBeginTransaction = true
                    val values = ContentValues()
                    values.put(Feed.TITLE, feedTitle)
                    values.put(Feed.URL, feedUrl)
                    values.put(Feed.FORMAT, format)
                    values.put(Feed.ICON_PATH, Feed.DEDAULT_ICON_PATH)
                    values.put(Feed.SITE_URL, siteUrl)
                    values.put(Feed.UNREAD_ARTICLE, 0)
                    val id = db.insert(Feed.TABLE_NAME, null, values)
                    rss = Feed(
                            id = id.toInt(),
                            title = feedTitle,
                            url = feedUrl,
                            format = format,
                            siteUrl = siteUrl
                    )
                    db.setTransactionSuccessful()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                if (isBeginTransaction) db.endTransaction()
            }

            rss
        }
    }

    suspend fun getFeedByUrl(feedUrl: String): Feed? = withContext(coroutineDispatcherProvider.io()) {
        val columns = arrayOf(Feed.ID, Feed.TITLE, Feed.URL, Feed.ICON_PATH, Feed.SITE_URL, Feed.UNREAD_ARTICLE)
        val selection = Feed.URL + " = \"" + feedUrl + "\""
        return@withContext query(columns, selection)
    }

    suspend fun getFeedById(feedId: Int): Feed? = withContext(coroutineDispatcherProvider.io()) {
        val columns = arrayOf(Feed.ID, Feed.TITLE, Feed.URL, Feed.ICON_PATH, Feed.SITE_URL, Feed.UNREAD_ARTICLE)
        val selection = Feed.ID + " = " + feedId
        return@withContext query(columns, selection)
    }

    private suspend fun query(columns: Array<String>, selection: String? = null): Feed? = coroutineScope {
        var feed: Feed? = null
        var cur: Cursor? = null
        try {
            db.beginTransaction()
            cur = db.query(Feed.TABLE_NAME, columns, selection, null, null, null, null)
            if (cur.count != 0) {
                cur.moveToNext()
                var id = 0
                var title = ""
                var url = ""
                var iconPath = ""
                var siteUrl = ""
                var count = 0
                for ((i, column) in columns.withIndex()) {
                    when (column) {
                        Feed.ID -> id = cur.getInt(i)
                        Feed.TITLE -> title = cur.getString(i)
                        Feed.URL -> url = cur.getString(i)
                        Feed.ICON_PATH -> iconPath = cur.getString(i)
                        Feed.SITE_URL -> siteUrl = cur.getString(i)
                        Feed.UNREAD_ARTICLE -> count = cur.getInt(i)
                    }
                }
                feed = Feed(id, title, url, iconPath, "", count, siteUrl)
            }
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            cur?.close()
            db.endTransaction()
        }
        return@coroutineScope feed
    }
}