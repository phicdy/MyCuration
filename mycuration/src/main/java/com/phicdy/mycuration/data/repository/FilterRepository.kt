package com.phicdy.mycuration.data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.data.filter.Filter
import com.phicdy.mycuration.data.filter.FilterFeedRegistration
import com.phicdy.mycuration.data.rss.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber

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

    suspend fun getEnabledFiltersOfFeed(feedId: Int): ArrayList<Filter> = withContext(Dispatchers.IO) {
        val filterList = arrayListOf<Filter>()
        var cur: Cursor? = null
        try {
            // Get all filters which feed ID is "feedId"
            val columns = arrayOf(
                    Filter.TABLE_NAME + "." + Filter.ID,
                    Filter.TABLE_NAME + "." + Filter.TITLE,
                    Filter.TABLE_NAME + "." + Filter.KEYWORD,
                    Filter.TABLE_NAME + "." + Filter.URL,
                    Filter.TABLE_NAME + "." + Filter.ENABLED
            )
            val condition = FilterFeedRegistration.TABLE_NAME + "." + FilterFeedRegistration.FEED_ID + " = " + feedId + " and " +
                    FilterFeedRegistration.TABLE_NAME + "." + FilterFeedRegistration.FILTER_ID + " = " + Filter.TABLE_NAME + "." + Filter.ID + " and " +
                    Filter.TABLE_NAME + "." + Filter.ENABLED + " = " + Filter.TRUE
            db.beginTransaction()
            cur = db.query(Filter.TABLE_NAME + " inner join " + FilterFeedRegistration.TABLE_NAME, columns, condition, null, null, null, null)
            // Change to ArrayList
            while (cur.moveToNext()) {
                val id = cur.getInt(0)
                val title = cur.getString(1)
                val keyword = cur.getString(2)
                val url = cur.getString(3)
                val enabled = cur.getInt(4)
                filterList.add(Filter(id, title, keyword, url, ArrayList(), -1, enabled))
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            cur?.close()
            db.endTransaction()
        }

        return@withContext filterList
    }

    suspend fun getFilterById(filterId: Int): Filter? = withContext(Dispatchers.IO) {
        var filter: Filter? = null
        val columns = arrayOf(
                Filter.TABLE_NAME + "." + Filter.ID,
                Filter.TABLE_NAME + "." + Filter.KEYWORD,
                Filter.TABLE_NAME + "." + Filter.URL,
                Filter.TABLE_NAME + "." + Filter.TITLE,
                Filter.TABLE_NAME + "." + Filter.ENABLED,
                Feed.TABLE_NAME + "." + Feed.ID,
                Feed.TABLE_NAME + "." + Feed.TITLE
        )
        val condition = Filter.TABLE_NAME + "." + Filter.ID + " = " + filterId + " and " +
                FilterFeedRegistration.TABLE_NAME + "." + FilterFeedRegistration.FILTER_ID + " = " + filterId + " and " +
                FilterFeedRegistration.TABLE_NAME + "." + FilterFeedRegistration.FEED_ID + " = " + Feed.TABLE_NAME + "." + Feed.ID
        val table = Filter.TABLE_NAME + " inner join " +
                FilterFeedRegistration.TABLE_NAME + " inner join " + Feed.TABLE_NAME
        var cursor: Cursor? = null
        try {
            db.beginTransaction()
            cursor = db.query(table, columns, condition, null, null, null, null)
            if (cursor == null || cursor.count < 1) return@withContext null

            val feeds = arrayListOf<Feed>()
            var id = 0
            var keyword = ""
            var url = ""
            var title = ""
            var enabled = 0
            while (cursor.moveToNext()) {
                id = cursor.getInt(0)
                keyword = cursor.getString(1)
                url = cursor.getString(2)
                title = cursor.getString(3)
                enabled = cursor.getInt(4)
                val feedId = cursor.getInt(5)
                val feedTitle = cursor.getString(6)
                val feed = Feed(feedId, feedTitle, "", Feed.DEDAULT_ICON_PATH, "", 0, "")
                feeds.add(feed)
            }
            db.setTransactionSuccessful()
            filter = Filter(id, title, keyword, url, feeds, enabled)
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            cursor?.close()
            db.endTransaction()
        }

        return@withContext filter
    }

    /**
     * Delete method for specified filter
     *
     * @param filterId Filter ID to delete
     */
    suspend fun deleteFilter(filterId: Int) = withContext(Dispatchers.IO) {
        try {
            db.beginTransaction()
            val relationWhere = FilterFeedRegistration.FILTER_ID + " = " + filterId
            db.delete(FilterFeedRegistration.TABLE_NAME, relationWhere, null)
            val filterWhere = Filter.ID + " = " + filterId
            db.delete(Filter.TABLE_NAME, filterWhere, null)
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            Timber.e(e)
        } finally {
            db.endTransaction()
        }
    }

    /**
     *
     * Save method for new filter.
     *
     * @param title Filter title
     * @param selectedFeeds Feed set to register the filter
     * @param keyword Filter keyword
     * @param filterUrl Filter URL
     * @return result of all of the database insert
     */
    suspend fun saveNewFilter(title: String, selectedFeeds: ArrayList<Feed>,
                      keyword: String, filterUrl: String): Boolean = withContext(Dispatchers.IO) {
        var result = true
        db.beginTransaction()
        var cur: Cursor? = null
        var newFilterId = INSERT_ERROR_ID.toLong()
        try {
            // Check same filter exists in DB
            val columns = arrayOf(Filter.ID)
            val condition = Filter.TITLE + " = '" + title + "' and " +
                    Filter.KEYWORD + " = '" + keyword + "' and " +
                    Filter.URL + " = '" + filterUrl + "'"
            val table = Filter.TABLE_NAME
            cur = db.query(table, columns, condition, null, null, null, null)
            if (cur.count != 0) {
                Timber.i("Same Filter Exist")
            } else {
                // Register filter
                val filterVal = ContentValues().apply {
                    put(Filter.TITLE, title)
                    put(Filter.URL, filterUrl)
                    put(Filter.KEYWORD, keyword)
                    put(Filter.ENABLED, true)
                }
                newFilterId = db.insert(Filter.TABLE_NAME, null, filterVal)
                if (newFilterId == INSERT_ERROR_ID.toLong()) {
                    result = false
                } else {
                    db.setTransactionSuccessful()
                }
            }
        } catch (e: Exception) {
            Timber.e("Failed to save new filter %s", e.message)
            result = false
        } finally {
            cur?.close()
            db.endTransaction()
        }
        if (result) {
            result = saveFilterFeedRegistration(newFilterId, selectedFeeds)
        }

        return@withContext result
    }

    /**
     * Update method for filter.
     *
     * @param filterId Filter ID to update
     * @param title New title
     * @param keyword New keyword
     * @param url New URL
     * @param feeds New feeds to filter
     * @return update result
     */
    suspend fun updateFilter(filterId: Int, title: String, keyword: String, url: String, feeds: ArrayList<Feed>): Boolean = withContext(Dispatchers.IO) {
        var result: Boolean
        try {
            val values = ContentValues().apply {
                put(Filter.ID, filterId)
                put(Filter.KEYWORD, keyword)
                put(Filter.URL, url)
                put(Filter.TITLE, title)
            }
            db.beginTransaction()
            var affectedNum = db.update(Filter.TABLE_NAME, values, Filter.ID + " = " + filterId, null)
            // Same ID filter should not exist and 0 means fail to update
            result = affectedNum == 1

            // Delete existing relation between filter and feed
            if (result) {
                val where = FilterFeedRegistration.FILTER_ID + " = " + filterId
                affectedNum = db.delete(FilterFeedRegistration.TABLE_NAME, where, null)
                result = affectedNum > 0
            }

            // Insert new relations
            if (result) {
                result = saveFilterFeedRegistration(filterId.toLong(), feeds)
            }

            if (result) db.setTransactionSuccessful()
        } catch (e: SQLException) {
            Timber.e(e)
            result = false
        } finally {
            db.endTransaction()
        }
        return@withContext result
    }

    /**
     *
     * Save method for relation between filter and feed set into database.
     * This method does not have transaction.
     *
     * @param filterId Filter ID
     * @param feeds Feed set to register the filter
     * @return result of all of the database insert
     */
    private suspend fun saveFilterFeedRegistration(filterId: Long, feeds: ArrayList<Feed>): Boolean = coroutineScope {
        if (filterId < MIN_TABLE_ID) return@coroutineScope false
        var result = true
        try {
            db.beginTransaction()
            for ((feedId) in feeds) {
                if (feedId < MIN_TABLE_ID) {
                    result = false
                    break
                }
                val contentValues = ContentValues().apply {
                    put(FilterFeedRegistration.FEED_ID, feedId)
                    put(FilterFeedRegistration.FILTER_ID, filterId)
                }
                val id = db.insert(FilterFeedRegistration.TABLE_NAME, null, contentValues)
                if (id == INSERT_ERROR_ID.toLong()) {
                    result = false
                    break
                }
            }
        } catch (e: SQLException) {
            Timber.e(e)
        } finally {
            if (result) db.setTransactionSuccessful()
            db.endTransaction()
        }
        return@coroutineScope result
    }

    companion object {
        private const val INSERT_ERROR_ID = -1
        private const val MIN_TABLE_ID = 1
    }
}
