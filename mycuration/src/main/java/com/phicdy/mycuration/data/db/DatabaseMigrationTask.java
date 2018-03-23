package com.phicdy.mycuration.data.db;

import android.database.sqlite.SQLiteDatabase;

interface DatabaseMigrationTask {
    void execute(SQLiteDatabase db, int oldVersion);
}
