package com.phicdy.mycuration.data.db

import android.content.ContentValues
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.entity.Feed
import javax.inject.Inject

class ResetIconPathTask @Inject constructor() : DatabaseMigrationTask {

    override fun execute(db: SQLiteDatabase, oldVersion: Int) {
        try {
            db.beginTransaction()
            val values = ContentValues().apply {
                put(Feed.ICON_PATH, Feed.DEDAULT_ICON_PATH)
            }
            db.update(Feed.TABLE_NAME, values, null, null)
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }
}
