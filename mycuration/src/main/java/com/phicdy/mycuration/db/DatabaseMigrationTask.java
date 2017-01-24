package com.phicdy.mycuration.db;

import android.database.sqlite.SQLiteDatabase;

public interface DatabaseMigrationTask {
    void execute(SQLiteDatabase db, int oldVersion);
}
