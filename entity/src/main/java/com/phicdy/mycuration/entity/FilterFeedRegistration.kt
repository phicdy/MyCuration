package com.phicdy.mycuration.entity


object FilterFeedRegistration {
    const val TABLE_NAME = "filterFeedRegistrations"
    private const val ID = "_id"
    const val FEED_ID = "feedId"
    const val FILTER_ID = "filterId"

    const val CREATE_TABLE_SQL = "create table " + TABLE_NAME + "(" +
            ID + " integer primary key autoincrement," +
            FILTER_ID + " integer," +
            FEED_ID + " integer," +
            "foreign key(" + FILTER_ID + ") references " + Filter.TABLE_NAME + "(" + Filter.ID + ")," +
            "foreign key(" + FEED_ID + ") references " + Feed.TABLE_NAME + "(" + Feed.ID + ")" +
            ")"

    const val DROP_TABLE_SQL = "DROP TABLE $TABLE_NAME"
}
