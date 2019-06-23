package com.phicdy.mycuration.data.db

import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository

internal class ResetIconPathTask : DatabaseMigrationTask {

    override fun execute(db: SQLiteDatabase, oldVersion: Int) {
        val rssRepository = RssRepository(db, ArticleRepository(db), FilterRepository(db))
        rssRepository.resetIconPath()
    }
}
