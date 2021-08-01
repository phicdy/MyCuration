package com.phicdy.mycuration.data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.core.CoroutineDispatcherProvider
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.Filter
import com.phicdy.mycuration.entity.FilterFeedRegistration
import com.phicdy.mycuration.repository.Database
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterRepository @Inject constructor(
        private val db: SQLiteDatabase,
        private val database: Database,
        private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) {

    /**
     * Helper method to retrieve all of the filters.
     *
     * @return all of the filters in the database
     */
    suspend fun getAllFilters(): List<Filter> = coroutineScope {
        return@coroutineScope withContext(coroutineDispatcherProvider.io()) {
            val filters = ArrayList<Filter>()
            try {
                database.transaction {
                    val results = database.filtersQueries.getAll().executeAsList()
                    if (results.size > 0) {
                        var rssList = ArrayList<Feed>()
                        var filterId = results[0]._id
                        var title = results[0].title ?: ""
                        var keyword = results[0].keyword ?: ""
                        var url = results[0].url ?: ""
                        var enabled = results[0].enabled?.toInt() ?: 0
                        var rssId = results[0]._id__.toInt()
                        var rssTitle = results[0].title_
                        rssList.add(Feed(rssId, rssTitle, "", Feed.DEDAULT_ICON_PATH, "", 0, ""))
                        for (result in results) {
                            val cursorFilterId = result._id
                            if (filterId != cursorFilterId) {
                                // Next filter starts, add to filter list and init RSS list for next filter
                                val filter = Filter(filterId.toInt(), title, keyword, url, rssList, -1, enabled)
                                filters.add(filter)
                                filterId = cursorFilterId
                                rssList = ArrayList()
                            }
                            title = results[0].title ?: ""
                            keyword = results[0].keyword ?: ""
                            url = results[0].url ?: ""
                            enabled = results[0].enabled?.toInt() ?: 0
                            rssId = results[0]._id__.toInt()
                            rssTitle = results[0].title_
                            rssList.add(Feed(rssId, rssTitle, "", Feed.DEDAULT_ICON_PATH, "", 0, ""))
                        }
                        val filter = Filter(filterId.toInt(), title, keyword, url, rssList, -1, enabled)
                        filters.add(filter)
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return@withContext filters
        }
    }

    suspend fun getEnabledFiltersOfFeed(feedId: Int): List<Filter> = withContext(coroutineDispatcherProvider.io()) {
        try {
            return@withContext database.transactionWithResult<List<Filter>> {
                database.filtersQueries.getAllEnabled(feedId.toLong()).executeAsList().map {
                    Filter(it._id.toInt(), it.title, it.keyword ?: "", it.url
                            ?: "", arrayListOf(), -1, it.enabled.toInt())
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        return@withContext emptyList()
    }

    suspend fun getFilterById(filterId: Int): Filter? = withContext(coroutineDispatcherProvider.io()) {
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
    suspend fun deleteFilter(filterId: Int) = withContext(coroutineDispatcherProvider.io()) {
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
                              keyword: String, filterUrl: String): Boolean = withContext(coroutineDispatcherProvider.io()) {
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
    suspend fun updateFilter(filterId: Int, title: String, keyword: String, url: String, feeds: ArrayList<Feed>): Boolean = withContext(coroutineDispatcherProvider.io()) {
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

    suspend fun updateEnabled(id: Int, isEnabled: Boolean) = withContext(coroutineDispatcherProvider.io()) {
        try {
            val values = ContentValues().apply {
                put(Filter.ENABLED, if (isEnabled) Filter.TRUE else Filter.FALSE)
            }
            db.beginTransaction()
            db.update(Filter.TABLE_NAME, values, Filter.ID + " = " + id, null)
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            Timber.e(e)
        } finally {
            db.endTransaction()
        }
    }

    companion object {
        private const val INSERT_ERROR_ID = -1
        private const val MIN_TABLE_ID = 1
    }
}
