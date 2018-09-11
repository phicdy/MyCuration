package com.phicdy.mycuration.data.rss


data class Curation(
        val id: Int,
        val name: String
) {

    companion object {

        const val TABLE_NAME = "curations"
        const val NAME = "name"
        const val ID = "_id"

        const val CREATE_TABLE_SQL = "create table " + TABLE_NAME + "(" +
                ID + " integer primary key autoincrement," +
                NAME + " text)"
    }
}
