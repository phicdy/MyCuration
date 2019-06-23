package com.phicdy.mycuration

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.domain.entity.Article
import com.phicdy.mycuration.domain.entity.Curation
import com.phicdy.mycuration.domain.entity.CurationCondition
import com.phicdy.mycuration.domain.entity.CurationSelection
import com.phicdy.mycuration.domain.entity.Feed
import com.phicdy.mycuration.domain.entity.Filter
import com.phicdy.mycuration.domain.entity.FilterFeedRegistration
import timber.log.Timber

fun deleteAll(db: SQLiteDatabase) {
    try {
        db.beginTransaction()
        db.delete(CurationCondition.TABLE_NAME, null, null)
        db.delete(CurationSelection.TABLE_NAME, null, null)
        db.delete(Article.TABLE_NAME, null, null)
        db.delete(FilterFeedRegistration.TABLE_NAME, null, null)
        db.delete(Filter.TABLE_NAME, null, null)
        db.delete(Curation.TABLE_NAME, null, null)
        db.delete(Feed.TABLE_NAME, null, null)
        db.setTransactionSuccessful()
    } catch (e: SQLException) {
        Timber.e(e)
    } finally {
        db.endTransaction()
    }
}