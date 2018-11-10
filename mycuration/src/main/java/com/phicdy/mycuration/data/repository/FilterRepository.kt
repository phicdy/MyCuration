package com.phicdy.mycuration.data.repository

import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.data.filter.Filter
import com.phicdy.mycuration.data.filter.FilterFeedRegistration
import com.phicdy.mycuration.data.rss.Feed
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.coroutineScope
import kotlinx.coroutines.experimental.withContext

class FilterRepository(private val db: SQLiteDatabase) {

    /**
     * Helper method to retrieve all of the filters.
     *
     * @return all of the filters in the database
     */
    suspend fun getAllFilters(): ArrayList<Filter> = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            val filters = ArrayList<Filter>()
            val columns = arrayOf(
                    Filter.TABLE_NAME + "." + Filter.ID,
                    Filter.TABLE_NAME + "." + Filter.TITLE,
                    Filter.TABLE_NAME + "." + Filter.KEYWORD,
                    Filter.TABLE_NAME + "." + Filter.URL,
                    Filter.TABLE_NAME + "." + Filter.ENABLED,
                    Feed.TABLE_NAME + "." + Feed.ID,
                    Feed.TABLE_NAME + "." + Feed.TITLE
            )
            val selection = Filter.TABLE_NAME + "." + Filter.ID + "=" +
                    FilterFeedRegistration.TABLE_NAME + "." + FilterFeedRegistration.FILTER_ID + " and " +
                    FilterFeedRegistration.TABLE_NAME + "." + FilterFeedRegistration.FEED_ID + "=" +
                    Feed.TABLE_NAME + "." + Feed.ID
            val table = Filter.TABLE_NAME + " inner join " +
                    FilterFeedRegistration.TABLE_NAME + " inner join " + Feed.TABLE_NAME
            var cursor: Cursor? = null
            try {
                db.beginTransaction()
                cursor = db.query(table, columns, selection, null, null, null, null)
                if (cursor != null && cursor.count > 0) {
                    cursor.moveToFirst()
                    var filter: Filter
                    var rssList = ArrayList<Feed>()
                    var filterId = cursor.getInt(0)
                    var title = cursor.getString(1)
                    var keyword = cursor.getString(2)
                    var url = cursor.getString(3)
                    var enabled = cursor.getInt(4)
                    var rssId = cursor.getInt(5)
                    var rssTitle = cursor.getString(6)
                    rssList.add(Feed(rssId, rssTitle, "", Feed.DEDAULT_ICON_PATH, "", 0, ""))
                    while (cursor.moveToNext()) {
                        val cursorFilterId = cursor.getInt(0)
                        if (filterId != cursorFilterId) {
                            // Next filter starts, add to filter list and init RSS list for next filter
                            filter = Filter(filterId, title, keyword, url, rssList, -1, enabled)
                            filters.add(filter)
                            filterId = cursorFilterId
                            rssList = ArrayList()
                        }
                        title = cursor.getString(1)
                        keyword = cursor.getString(2)
                        url = cursor.getString(3)
                        enabled = cursor.getInt(4)
                        rssId = cursor.getInt(5)
                        rssTitle = cursor.getString(6)
                        rssList.add(Feed(rssId, rssTitle, "", Feed.DEDAULT_ICON_PATH, "", 0, ""))
                    }
                    filter = Filter(filterId, title, keyword, url, rssList, -1, enabled)
                    filters.add(filter)
                    cursor.close()
                }
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                db.endTransaction()
                cursor?.close()
            }
            return@withContext filters
        }
    }
}
