package com.phicdy.mycuration

import android.database.SQLException
import com.phicdy.mycuration.repository.Database
import timber.log.Timber

fun deleteAll(db: Database) {
    try {
        db.transaction {
            db.curationConditionQueries.deleteAll()
            db.curationSelectionQueries.deleteAll()
            db.articleQueries.deleteAll()
            db.filterFeedRegistrationQueries.deleteAll()
            db.filtersQueries.deleteAll()
            db.curationQueries.deleteAll()
            db.feedQueries.deleteAll()
        }
    } catch (e: SQLException) {
        Timber.e(e)
    }
}