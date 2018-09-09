package com.phicdy.mycuration.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.phicdy.mycuration.data.filter.Filter
import com.phicdy.mycuration.data.filter.FilterFeedRegistration
import com.phicdy.mycuration.data.rss.Article
import com.phicdy.mycuration.data.rss.Curation
import com.phicdy.mycuration.data.rss.CurationCondition
import com.phicdy.mycuration.data.rss.CurationSelection
import com.phicdy.mycuration.data.rss.Feed

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    //onCreate() is called when database is created
    override fun onCreate(db: SQLiteDatabase) {
        db.apply {
            execSQL(Feed.CREATE_TABLE_SQL)
            execSQL(Article.CREATE_TABLE_SQL)
            execSQL(Filter.CREATE_TABLE_SQL)
            execSQL(FilterFeedRegistration.CREATE_TABLE_SQL)
            execSQL(Curation.CREATE_TABLE_SQL)
            execSQL(CurationSelection.CREATE_TABLE_SQL)
            execSQL(CurationCondition.CREATE_TABLE_SQL)
        }
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (!db.isReadOnly) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;")
        }
    }

    //onUpgrade() is called when database version changes
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        DatabaseMigration(oldVersion, newVersion).migrate(db)
    }

    companion object {
        const val DATABASE_NAME = "rss_manage"
        private const val DATABASE_VERSION = 3
    }
}
