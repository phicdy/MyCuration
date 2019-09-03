package com.phicdy.mycuration.data.db

import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.entity.FavoriteArticle

internal class AddFavoriteTask : DatabaseMigrationTask {

    override fun execute(db: SQLiteDatabase, oldVersion: Int) {
        db.execSQL(FavoriteArticle.CREATE_TABLE_SQL)
    }
}
