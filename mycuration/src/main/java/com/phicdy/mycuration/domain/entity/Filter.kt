package com.phicdy.mycuration.domain.entity


import com.phicdy.mycuration.data.rss.Feed

import java.util.ArrayList

data class Filter(
        val id: Int,
        val title: String,
        val keyword: String,
        val url: String,
        val feeds: ArrayList<Feed> = ArrayList(),
        val feedId: Int = -1, // Not used now. It was used when the filter only for one RSS
        private var enabled: Int = 0
) {

    val feedTitle: String by lazy {
        StringBuilder().apply {
            for ((index, feed) in feeds.withIndex()) {
                if (index >= 1) append(", ")
                append(feed.title)
            }
        }.toString()
    }

    var isEnabled: Boolean
        get() = enabled == TRUE
        set(enabled) = if (enabled) {
            this.enabled = TRUE
        } else {
            this.enabled = FALSE
        }

    companion object {

        const val TABLE_NAME = "filters"
        const val ID = "_id"
        @Deprecated("")
        const val FEED_ID = "feedId"
        const val KEYWORD = "keyword"
        const val URL = "url"
        const val TITLE = "title"
        const val ENABLED = "enabled"

        const val TRUE = 1
        const val FALSE = 0

        const val CREATE_TABLE_SQL = "create table " + TABLE_NAME + "(" +
                ID + " integer primary key autoincrement," +
                KEYWORD + " text," +
                URL + " text," +
                TITLE + " text," +
                ENABLED + " integer)"

        @Suppress("DEPRECATION")
        const val CREATE_TABLE_SQL_VER2 = "create table " + TABLE_NAME + "(" +
                ID + " integer primary key autoincrement," +
                FEED_ID + " integer," +
                KEYWORD + " text," +
                URL + " text," +
                TITLE + " text," +
                ENABLED + " integer, " +
                "foreign key(" + FEED_ID + ") references " + Feed.TABLE_NAME + "(" + Feed.ID + "))"

        @Suppress("DEPRECATION")
        const val CREATE_TABLE_SQL_VER1 = "create table " + TABLE_NAME + "(" +
                ID + " integer primary key autoincrement," +
                FEED_ID + " integer," +
                KEYWORD + " text," +
                URL + " text," +
                TITLE + " text," +
                "foreign key(" + FEED_ID + ") references " + Feed.TABLE_NAME + "(" + Feed.ID + "))"

        const val DROP_TABLE_SQL = "DROP TABLE $TABLE_NAME"
    }
}
