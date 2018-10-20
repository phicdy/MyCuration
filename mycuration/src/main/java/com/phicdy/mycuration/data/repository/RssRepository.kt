package com.phicdy.mycuration.data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.data.rss.Feed
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.coroutineScope
import kotlinx.coroutines.experimental.withContext

class RssRepository(private val db: SQLiteDatabase) {

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
}