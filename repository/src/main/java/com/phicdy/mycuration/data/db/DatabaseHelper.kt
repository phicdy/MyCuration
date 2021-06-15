package com.phicdy.mycuration.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.phicdy.mycuration.entity.CurationCondition
import com.phicdy.mycuration.entity.CurationSelection
import com.phicdy.mycuration.entity.FavoriteArticle
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseHelper @Inject constructor(
        @ApplicationContext context: Context,
        private val databaseMigration: DatabaseMigration
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    //onCreate() is called when database is created
    override fun onCreate(db: SQLiteDatabase) {
        db.apply {
            execSQL(CurationSelection.CREATE_TABLE_SQL)
            execSQL(CurationCondition.CREATE_TABLE_SQL)
            execSQL(FavoriteArticle.CREATE_TABLE_SQL)
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
        databaseMigration.migrate(db, oldVersion, newVersion)
    }

    companion object {
        const val DATABASE_NAME = "rss_manage"
        private const val DATABASE_VERSION = 5
    }
}
