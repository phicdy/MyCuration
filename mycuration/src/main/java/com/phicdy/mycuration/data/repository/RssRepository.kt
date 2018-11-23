package com.phicdy.mycuration.data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.data.filter.Filter
import com.phicdy.mycuration.data.filter.FilterFeedRegistration
import com.phicdy.mycuration.data.rss.Article
import com.phicdy.mycuration.data.rss.CurationSelection
import com.phicdy.mycuration.data.rss.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.ArrayList

class RssRepository(private val db: SQLiteDatabase,
                    private val articleRepository: ArticleRepository,
                    private val filterRepository: FilterRepository) {

    suspend fun getNumOfRss(): Int = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            var num: Int
            var cursor: Cursor? = null
            try {
                db.beginTransaction()
                cursor = db.query(Feed.TABLE_NAME, arrayOf(Feed.ID), "", emptyArray(), "", "", "")
                num = cursor.count
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                num = -1
            } finally {
                db.endTransaction()
                cursor?.close()
            }

            return@withContext num
        }
    }

    /**
     * Update method for rss title.
     *
     * @param rssId RSS ID to update
     * @param newTitle New rss title
     * @return Num of updated rss
     */
    suspend fun saveNewTitle(rssId: Int, newTitle: String): Int = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            var numOfUpdated = 0
            db.beginTransaction()
            try {
                val values = ContentValues().apply {
                    put(Feed.TITLE, newTitle)
                }
                numOfUpdated = db.update(Feed.TABLE_NAME, values, Feed.ID + " = " + rssId, null)
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                db.endTransaction()
            }
            return@withContext numOfUpdated
        }
    }

    /**
     * Delete method for rss and related data.
     *
     * @param rssId Feed ID to delete
     * @return result of delete
     */
    suspend fun deleteRss(rssId: Int): Boolean = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            var numOfDeleted = 0
            try {
                db.beginTransaction()

                val allArticlesInRss = articleRepository.getAllArticlesInRss(rssId, true)
                for (article in allArticlesInRss) {
                    db.delete(CurationSelection.TABLE_NAME, CurationSelection.ARTICLE_ID + " = " + article.id, null)
                }
                db.delete(Article.TABLE_NAME, Article.FEEDID + " = " + rssId, null)

                // Delete related filter
                db.delete(FilterFeedRegistration.TABLE_NAME, FilterFeedRegistration.FEED_ID + " = " + rssId, null)
                val filters = filterRepository.getAllFilters()
                for (filter in filters) {
                    val rsss = filter.feeds
                    if (rsss.size == 1 && rsss.get(0).id == rssId) {
                        // This filter had relation with this rss only
                        db.delete(Filter.TABLE_NAME, Filter.ID + " = " + filter.id, null)
                    }
                }

                numOfDeleted = db.delete(Feed.TABLE_NAME, Feed.ID + " = " + rssId, null)
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                db.endTransaction()
            }
            return@withContext numOfDeleted == 1
        }
    }

    /**
     * Get method to feed array with unread count of articles.
     *
     * @return Feed array with unread count of articles
     */
    suspend fun getAllFeedsWithNumOfUnreadArticles(): ArrayList<Feed> = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            var feedList = ArrayList<Feed>()
            db.beginTransaction()
            var cursor: Cursor? = null
            try {
                val columns = arrayOf(Feed.ID, Feed.TITLE, Feed.URL, Feed.ICON_PATH, Feed.SITE_URL, Feed.UNREAD_ARTICLE)
                val orderBy = Feed.TITLE
                cursor = db.query(Feed.TABLE_NAME, columns, null, null, null, null, orderBy)
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val id = cursor.getInt(0)
                        val title = cursor.getString(1)
                        val url = cursor.getString(2)
                        val iconPath = cursor.getString(3)
                        val siteUrl = cursor.getString(4)
                        val unreadAriticlesCount = cursor.getInt(5)
                        feedList.add(Feed(id, title, url, iconPath, "", unreadAriticlesCount, siteUrl))
                    }
                }
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                cursor?.close()
                db.endTransaction()
            }

            if (feedList.size == 0) {
                feedList = getAllFeedsWithoutNumOfUnreadArticles()
            }
            return@withContext feedList
        }
    }

    /**
     * Get method to feed array without unread count of articles.
     *
     * @return Feed array without unread count of articles
     */
    suspend fun getAllFeedsWithoutNumOfUnreadArticles(): ArrayList<Feed> = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            val feedList = ArrayList<Feed>()
            val columns = arrayOf(Feed.ID, Feed.TITLE, Feed.URL, Feed.ICON_PATH, Feed.SITE_URL)
            val orderBy = Feed.TITLE
            var cursor: Cursor? = null
            try {
                db.beginTransaction()
                cursor = db.query(Feed.TABLE_NAME, columns, null, null, null, null, orderBy)
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val id = cursor.getInt(0)
                        val title = cursor.getString(1)
                        val url = cursor.getString(2)
                        val iconPath = cursor.getString(3)
                        val siteUrl = cursor.getString(4)
                        feedList.add(Feed(id, title, url, iconPath, "", 0, siteUrl))
                    }
                }
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                cursor?.close()
                db.endTransaction()
            }

            return@withContext feedList
        }
    }

    /**
     * Update method for unread article count of the feed.
     *
     * @param feedId Feed ID to change
     * @param unreadCount New article unread count
     */
    suspend fun updateUnreadArticleCount(feedId: Int, unreadCount: Int) = withContext(Dispatchers.IO) {
        try {
            db.beginTransaction()
            val values = ContentValues().apply {
                put(Feed.UNREAD_ARTICLE, unreadCount)
            }
            db.update(Feed.TABLE_NAME, values, Feed.ID + " = $feedId", null)
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    fun resetIconPath() {
        try {
            db.beginTransaction()
            val values = ContentValues().apply {
                put(Feed.ICON_PATH, Feed.DEDAULT_ICON_PATH)
            }
            db.update(Feed.TABLE_NAME, values, null, null)
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    @VisibleForTesting
    fun getFeedWithUnreadCountBy(rssId: Int): Feed? {
        var feed: Feed? = null
        db.beginTransaction()
        var cur: Cursor? = null
        try {
            val culumn = arrayOf(Feed.TITLE, Feed.URL, Feed.ICON_PATH, Feed.SITE_URL, Feed.UNREAD_ARTICLE)
            val selection = Feed.ID + " = " + rssId
            cur = db.query(Feed.TABLE_NAME, culumn, selection, null, null, null, null)
            if (cur.count != 0) {
                cur.moveToNext()
                val feedTitle = cur.getString(0)
                val feedUrl = cur.getString(1)
                val iconPath = cur.getString(2)
                val siteUrl = cur.getString(3)
                val count = cur.getInt(4)
                feed = Feed(rssId, feedTitle, feedUrl, iconPath, "", count, siteUrl)
            }
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            cur?.close()
            db.endTransaction()
        }
        return feed
    }
}