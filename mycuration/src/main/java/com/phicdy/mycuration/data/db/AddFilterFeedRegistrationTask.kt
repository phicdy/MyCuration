package com.phicdy.mycuration.data.db

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException

import com.phicdy.mycuration.domain.entity.Filter
import com.phicdy.mycuration.domain.entity.FilterFeedRegistration

import java.util.ArrayList

internal class AddFilterFeedRegistrationTask : DatabaseMigrationTask {

    override fun execute(db: SQLiteDatabase, oldVersion: Int) {
        // Drop feed ID column in filter table, but Androd does not support drop column.
        // Copy and drop table and insert.
        val filters = getOldAllFilters(db, oldVersion)
        val sql = "DROP TABLE " + Filter.TABLE_NAME
        db.execSQL(sql)
        db.execSQL(Filter.CREATE_TABLE_SQL)

        // Insert all of the filters
        insertFilters(db, filters)

        // Migration feed and filter relation
        db.execSQL(FilterFeedRegistration.CREATE_TABLE_SQL)
        insertFilterFeedRegistration(db, filters)
    }

    private fun insertFilterFeedRegistration(db: SQLiteDatabase, filters: ArrayList<Filter>) {
        db.beginTransaction()
        try {
            for (filter in filters) {
                ContentValues().apply {
                    put(FilterFeedRegistration.FILTER_ID, filter.id)
                    put(FilterFeedRegistration.FEED_ID, filter.feedId)
                    db.insert(FilterFeedRegistration.TABLE_NAME, null, this)
                }
            }
            db.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    private fun insertFilters(db: SQLiteDatabase, filters: ArrayList<Filter>) {
        try {
            db.beginTransaction()
            var result = true
            for (filter in filters) {
                val filterVal = ContentValues().apply {
                    put(Filter.TITLE, filter.title)
                    put(Filter.KEYWORD, filter.keyword)
                    put(Filter.URL, filter.url)
                    put(Filter.ENABLED, filter.isEnabled)
                }
                val newFilterId = db.insert(Filter.TABLE_NAME, null, filterVal)
                if (newFilterId == -1L) {
                    result = false
                    break
                }
            }
            if (result) db.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    @Suppress("DEPRECATION")
    private fun getOldAllFilters(db: SQLiteDatabase, oldVersion: Int): ArrayList<Filter> {
        var cursor: Cursor? = null
        val filters = ArrayList<Filter>()
        try {
            db.beginTransaction()
            val columns = if (oldVersion == DatabaseMigration.DATABASE_VERSION_ADD_ENABLED_TO_FILTER) {
                arrayOf(Filter.ID, Filter.TITLE, Filter.KEYWORD, Filter.URL, Filter.FEED_ID, Filter.ENABLED)
            } else {
                arrayOf(Filter.ID, Filter.TITLE, Filter.KEYWORD, Filter.URL, Filter.FEED_ID)
            }
            cursor = db.query(Filter.TABLE_NAME, columns, "", null, null, null, null)
            cursor?.let {
                if (it.count > 0) {
                    while (it.moveToNext()) {
                        val filterId = it.getInt(0)
                        val title = it.getString(1)
                        val keyword = it.getString(2)
                        val url = it.getString(3)
                        val feedId = it.getInt(4)
                        val enabled = if (oldVersion == DatabaseMigration.DATABASE_VERSION_ADD_ENABLED_TO_FILTER) {
                            it.getInt(5)
                        } else {
                            Filter.TRUE
                        }
                        val filter = Filter(
                                id = filterId,
                                title = title,
                                keyword = keyword,
                                url = url,
                                feedId = feedId,
                                enabled = enabled)
                        filters.add(filter)
                    }
                }
            }
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.endTransaction()
        }
        return filters
    }
}
