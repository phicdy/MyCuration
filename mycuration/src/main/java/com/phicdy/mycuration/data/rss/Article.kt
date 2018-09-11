package com.phicdy.mycuration.data.rss


data class Article(
        val id: Int,
        var title: String,
        var url: String,
        var status: String,
        val point: String,
        var postedDate: Long,
        val feedId: Int,
        val feedTitle: String,
        val feedIconPath: String
) {

    companion object {

        const val TABLE_NAME = "articles"
        const val ID = "_id"
        const val TITLE = "title"
        const val URL = "url"
        const val STATUS = "status"
        const val POINT = "point"
        const val DATE = "date"
        const val FEEDID = "feedId"

        const val UNREAD = "unread"
        const val TOREAD = "toRead"
        const val READ = "read"

        const val DEDAULT_HATENA_POINT = "-1"

        const val CREATE_TABLE_SQL = "create table " + TABLE_NAME + "(" +
                ID + " integer primary key autoincrement," +
                TITLE + " text," +
                URL + " text," +
                STATUS + " text default " + UNREAD + "," +
                POINT + " text," +
                DATE + " text," +
                FEEDID + " integer," +
                "foreign key(" + FEEDID + ") references " + Feed.TABLE_NAME + "(" + Feed.ID + "))"
    }
}
