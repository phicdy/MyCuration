package com.phicdy.mycuration.data.db

import android.database.sqlite.SQLiteDatabase

internal interface DatabaseMigrationTask {
    fun execute(db: SQLiteDatabase, oldVersion: Int)
}
