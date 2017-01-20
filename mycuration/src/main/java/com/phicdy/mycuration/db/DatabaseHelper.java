package com.phicdy.mycuration.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.phicdy.mycuration.filter.Filter;
import com.phicdy.mycuration.filter.FilterFeedRegistration;
import com.phicdy.mycuration.rss.Article;
import com.phicdy.mycuration.rss.Curation;
import com.phicdy.mycuration.rss.CurationCondition;
import com.phicdy.mycuration.rss.CurationSelection;
import com.phicdy.mycuration.rss.Feed;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper{
  
	public static final String DATABASE_NAME = "rss_manage";
	private static final int DATABASE_VERSION = 3;
	private static final int DATABASE_VERSION_ADD_ENABLED_TO_FILTER = 2;
    private static final int DATABASE_VERSION_ADD_FILTER_FEED_REGISTRATION = 3;

    private String createFiltersTableSQL =
            "create table " + Filter.TABLE_NAME + "(" +
                    Filter.ID + " integer primary key autoincrement,"+
                    Filter.KEYWORD + " text,"+
                    Filter.URL + " text," +
                    Filter.TITLE + " text,"+
                    Filter.ENABLED + " integer)";
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
                "foreign key(" + Article.FEEDID + ") references " + Feed.TABLE_NAME + "(" + Feed.ID + "))";
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
            // Drop feed ID column in filter table, but Androd does not support drop column.
            // Copy and drop table and insert.
            ArrayList<Filter> filters = getAllFilters(db);
            String sql = "DROP TABLE " + Filter.TABLE_NAME;
            db.execSQL(sql);
            db.execSQL(createFiltersTableSQL);

            // Insert all of the filters
            insertFilters(db, filters);

            // Migration feed and filter relation
            db.execSQL(createFilterFeedRegistrationTableSQL);
            insertFilterFeedRegistration(db, filters);
        }
    }

    private void insertFilterFeedRegistration(@NonNull SQLiteDatabase db, @NonNull ArrayList<Filter> filters) {
        db.beginTransaction();
        try {
            for (Filter filter : filters) {
                int filterId = filter.getId();
                int feedId = filter.getFeedId();
                ContentValues condtionValue = new ContentValues();
                condtionValue.put(FilterFeedRegistration.FILTER_ID, filterId);
                condtionValue.put(FilterFeedRegistration.FEED_ID, feedId);
                db.insert(FilterFeedRegistration.TABLE_NAME, null, condtionValue);
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    private void insertFilters(@NonNull SQLiteDatabase db, @NonNull ArrayList<Filter> filters) {
        try {
            db.beginTransaction();
            boolean result = true;
            for (Filter filter : filters) {
                ContentValues filterVal = new ContentValues();
                filterVal.put(Filter.TITLE, filter.getTitle());
                filterVal.put(Filter.KEYWORD, filter.getKeyword());
                filterVal.put(Filter.URL, filter.getUrl());
                filterVal.put(Filter.ENABLED, filter.isEnabled());
                long newFilterId = db.insert(Filter.TABLE_NAME, null, filterVal);
                if (newFilterId == -1) {
                    result = false;
                    break;
                }
            }
            if (result) db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    private ArrayList<Filter> getAllFilters(SQLiteDatabase db) {
        Cursor cursor = null;
        ArrayList<Filter> filters = new ArrayList<>();
        try {
            db.beginTransaction();
            String[] columns = {
                    Filter.ID,
                    Filter.TITLE,
                    Filter.KEYWORD,
                    Filter.URL,
                    Filter.FEED_ID,
                    Filter.ENABLED
            };
            cursor = db.query(Filter.TABLE_NAME, columns, "", null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int filterId = cursor.getInt(0);
                    String title = cursor.getString(1);
                    String keyword = cursor.getString(2);
                    String url = cursor.getString(3);
                    int feedId = cursor.getInt(4);
                    int enabled = cursor.getInt(5);
                    Filter filter = new Filter(filterId, title, keyword, url, feedId, enabled);
                    filters.add(filter);
                }
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.endTransaction();
        }
        return filters;
    }
}
