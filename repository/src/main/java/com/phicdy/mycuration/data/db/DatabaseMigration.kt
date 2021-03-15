package com.phicdy.mycuration.data.db

import android.database.sqlite.SQLiteDatabase
import javax.inject.Inject

class DatabaseMigration @Inject constructor(
        private val resetIconPathTask: ResetIconPathTask
) {

    fun migrate(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < newVersion) {
            if (oldVersion < DATABASE_VERSION_ADD_FILTER_FEED_REGISTRATION) {
                AddFilterFeedRegistrationTask().execute(db, oldVersion)
            }
            if (oldVersion < DATABASE_VERSION_FETCH_ICON) {
                resetIconPathTask.execute(db, oldVersion)
            }
            if (oldVersion < DATABASE_VERSION_FAVORITE) {
                AddFavoriteTask().execute(db, oldVersion)
            }
        }
    }

    companion object {
        const val FIRST_VERSION = 1
        const val DATABASE_VERSION_ADD_ENABLED_TO_FILTER = 2
        const val DATABASE_VERSION_ADD_FILTER_FEED_REGISTRATION = 3
        const val DATABASE_VERSION_FETCH_ICON = 4
        const val DATABASE_VERSION_FAVORITE = 5
    }
}
