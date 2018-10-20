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
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.coroutineScope
import kotlinx.coroutines.experimental.withContext

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

}