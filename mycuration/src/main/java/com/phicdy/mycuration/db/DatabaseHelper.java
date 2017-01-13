package com.phicdy.mycuration.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
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
	private static final int DATABASE_VERSION_ADD_ENABLED_TO_FILTER = 2;
    private static final int DATABASE_VERSION_ADD_FILTER_FEED_REGISTRATION = 3;

    private String createFilterFeedRegistrationTableSQL =
            "create table " + FilterFeedRegistration.TABLE_NAME + "(" +
                    FilterFeedRegistration.ID + " integer primary key autoincrement," +
                    FilterFeedRegistration.FILTER_ID + " integer," +
                    FilterFeedRegistration.FEED_ID + " integer," +
                    "foreign key(" + FilterFeedRegistration.FILTER_ID + ") references " + Filter.TABLE_NAME + "(" + Filter.ID + ")," +
                    "foreign key(" + FilterFeedRegistration.FEED_ID + ") references " + Feed.TABLE_NAME + "(" + Feed.ID + ")" +
                    ")";
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
        db.execSQL(createFilterFeedRegistrationTableSQL);
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
        if (newVersion < oldVersion) return;
        if ((oldVersion < DATABASE_VERSION_ADD_ENABLED_TO_FILTER)) {
            String sql = "ALTER TABLE " + Filter.TABLE_NAME + " ADD COLUMN " + Filter.ENABLED + " integer";
            db.execSQL(sql);
            String enableAll = "UPDATE " + Filter.TABLE_NAME + " SET " + Filter.ENABLED + " = " + Filter.TRUE;
            db.execSQL(enableAll);
        }
        if ((oldVersion < DATABASE_VERSION_ADD_FILTER_FEED_REGISTRATION)) {
            db.execSQL(createFilterFeedRegistrationTableSQL);

            // Migration feed and filter relation
            String[] columns = {
                    Filter.ID,
                    Filter.FEED_ID
            };
            db.beginTransaction();
            try {
                Cursor cursor = db.query(Filter.TABLE_NAME, columns, null, null, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        int filterId = cursor.getInt(0);
                        int feedId = cursor.getInt(1);
                        ContentValues condtionValue = new ContentValues();
                        condtionValue.put(FilterFeedRegistration.FILTER_ID, filterId);
                        condtionValue.put(FilterFeedRegistration.FEED_ID, feedId);
                        db.insert(FilterFeedRegistration.TABLE_NAME, null, condtionValue);
                    }
                    cursor.close();
                }
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }
    }
}
