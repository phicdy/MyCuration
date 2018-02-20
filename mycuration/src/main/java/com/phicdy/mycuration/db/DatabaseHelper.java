package com.phicdy.mycuration.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.phicdy.mycuration.filter.Filter;
import com.phicdy.mycuration.filter.FilterFeedRegistration;
import com.phicdy.mycuration.rss.Article;
import com.phicdy.mycuration.rss.Curation;
import com.phicdy.mycuration.rss.CurationCondition;
import com.phicdy.mycuration.rss.CurationSelection;
import com.phicdy.mycuration.rss.Feed;

public class DatabaseHelper extends SQLiteOpenHelper{
  
	public static final String DATABASE_NAME = "rss_manage";
	private static final int DATABASE_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //onCreate() is called when database is created
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Feed.CREATE_TABLE_SQL);
        db.execSQL(Article.CREATE_TABLE_SQL);
        db.execSQL(Filter.CREATE_TABLE_SQL);
        db.execSQL(FilterFeedRegistration.CREATE_TABLE_SQL);
        db.execSQL(Curation.CREATE_TABLE_SQL);
        db.execSQL(CurationSelection.CREATE_TABLE_SQL);
        db.execSQL(CurationCondition.CREATE_TABLE_SQL);
    }
      
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
  
    //onUpgrade() is called when database version changes
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        new DatabaseMigration(oldVersion, newVersion).migrate(db);
    }
}
