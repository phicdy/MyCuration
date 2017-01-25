package com.phicdy.mycuration.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

class DatabaseMigration {

    private List<DatabaseMigrationTask> tasks = new ArrayList<>();
    private int oldVersion;

    static final int FIRST_VERSION = 1;
    static final int DATABASE_VERSION_ADD_ENABLED_TO_FILTER = 2;
    static final int DATABASE_VERSION_ADD_FILTER_FEED_REGISTRATION = 3;

    DatabaseMigration(int oldVersion, int newVersion) {
        this.oldVersion = oldVersion;
        if (oldVersion >= newVersion) return;
        if (oldVersion < DATABASE_VERSION_ADD_FILTER_FEED_REGISTRATION) {
            tasks.add(new AddFilterFeedRegistrationTask());
        }
    }

    void migrate(SQLiteDatabase db) {
        for (DatabaseMigrationTask task : tasks) {
            task.execute(db, oldVersion);
        }
    }
}
