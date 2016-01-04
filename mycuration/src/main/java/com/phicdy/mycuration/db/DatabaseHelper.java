package com.phicdy.mycuration.db;
  
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.phicdy.mycuration.filter.Filter;
import com.phicdy.mycuration.rss.Article;
import com.phicdy.mycuration.rss.Curation;
import com.phicdy.mycuration.rss.CurationCondition;
import com.phicdy.mycuration.rss.CurationSelection;
import com.phicdy.mycuration.rss.Feed;

public class DatabaseHelper extends SQLiteOpenHelper{
  
	public static final String DATABASE_NAME = "rss_manage";
	private static final int DATABASE_VERSION = 2;
	
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //onCreate() is called when database is created
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createFeedsTableSQL =
                "create table " + Feed.TABLE_NAME + "(" +
                Feed.ID + " integer primary key autoincrement,"+
                Feed.TITLE + " text,"+
                Feed.URL + " text,"+
                Feed.FORMAT + " text," +
                Feed.SITE_URL + " text," +
                Feed.ICON_PATH + " text," +
                Feed.UNREAD_ARTICLE + " integer)";
        String createArticlesTableSQL =
                "create table " + Article.TABLE_NAME + "(" +
                Article.ID + " integer primary key autoincrement,"+
                Article.TITLE + " text,"+
                Article.URL + " text,"+
                Article.STATUS + " text default "+ Article.UNREAD+","+
                Article.POINT + " text,"+
                Article.DATE + " text,"+
                Article.FEEDID + " integer,"+
                "foreign key(" + Article.FEEDID + ") references " + Feed.TABLE_NAME + "(" + Article.ID + "))";
        String createFiltersTableSQL =
                "create table " + Filter.TABLE_NAME + "(" +
                Filter.ID + " integer primary key autoincrement,"+
                Filter.FEED_ID + " integer,"+
                Filter.KEYWORD + " text,"+
                Filter.URL + " text," +
                Filter.TITLE + " text,"+
                Filter.ENABLED + " integer,"+
                "foreign key(" + Filter.FEED_ID + ") references feeds(" + Filter.ID + "))";
        String createCurationsTableSQL =
                "create table " + Curation.TABLE_NAME + "(" +
                        Curation.ID + " integer primary key autoincrement,"+
                        Curation.NAME + " text)";
        String createCurationSelectionsTableSQL =
                "create table " + CurationSelection.TABLE_NAME + "(" +
                        CurationSelection.ID + " integer primary key autoincrement,"+
                        CurationSelection.CURATION_ID + " integer," +
                        CurationSelection.ARTICLE_ID + " integer," +
                        "foreign key(" + CurationSelection.CURATION_ID + ") references " + Curation.TABLE_NAME + "(" + Curation.ID + ")," +
                        "foreign key(" + CurationSelection.ARTICLE_ID + ") references " + Article.TABLE_NAME + "(" + Article.ID + "))";
        String createCurationConditionTableSQL =
                "create table " + CurationCondition.TABLE_NAME + "(" +
                        CurationCondition.ID + " integer primary key autoincrement,"+
                        CurationCondition.WORD + " text," +
                        CurationCondition.CURATION_ID + " integer," +
                        "foreign key(" + CurationSelection.CURATION_ID + ") references " + Curation.TABLE_NAME + "(" + Curation.ID + "))";

        db.execSQL(createFeedsTableSQL);
        db.execSQL(createArticlesTableSQL);
        db.execSQL(createFiltersTableSQL);
        db.execSQL(createCurationsTableSQL);
        db.execSQL(createCurationSelectionsTableSQL);
        db.execSQL(createCurationConditionTableSQL);
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
        if (newVersion > oldVersion) {
            String sql = "ALTER TABLE " + Filter.TABLE_NAME + " ADD COLUMN " + Filter.ENABLED + " integer";
            db.execSQL(sql);
        }
    }
  
}
