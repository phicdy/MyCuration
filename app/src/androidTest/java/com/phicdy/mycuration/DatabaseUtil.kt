package com.phicdy.mycuration

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Curation
import com.phicdy.mycuration.entity.CurationCondition
import com.phicdy.mycuration.entity.CurationSelection
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.Filter
import com.phicdy.mycuration.entity.FilterFeedRegistration
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