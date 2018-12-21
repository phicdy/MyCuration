package com.phicdy.mycuration

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.data.filter.Filter
import com.phicdy.mycuration.data.filter.FilterFeedRegistration
import com.phicdy.mycuration.data.rss.Article
import com.phicdy.mycuration.data.rss.Curation
import com.phicdy.mycuration.data.rss.CurationCondition
import com.phicdy.mycuration.data.rss.CurationSelection
import com.phicdy.mycuration.data.rss.Feed
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