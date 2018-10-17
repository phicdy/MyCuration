package com.phicdy.mycuration.data.repository

import android.database.Cursor
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
}