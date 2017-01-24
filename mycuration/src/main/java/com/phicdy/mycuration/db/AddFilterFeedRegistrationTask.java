package com.phicdy.mycuration.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;

import com.phicdy.mycuration.filter.Filter;
import com.phicdy.mycuration.filter.FilterFeedRegistration;

import java.util.ArrayList;

class AddFilterFeedRegistrationTask implements DatabaseMigrationTask {

    @Override
    public void execute(SQLiteDatabase db, int oldVersion) {
        // Drop feed ID column in filter table, but Androd does not support drop column.
        // Copy and drop table and insert.
        ArrayList<Filter> filters = getOldAllFilters(db, oldVersion);
        String sql = "DROP TABLE " + Filter.TABLE_NAME;
        db.execSQL(sql);
        db.execSQL(Filter.CREATE_TABLE_SQL);

        // Insert all of the filters
        insertFilters(db, filters);

        // Migration feed and filter relation
        db.execSQL(FilterFeedRegistration.CREATE_TABLE_SQL);
        insertFilterFeedRegistration(db, filters);
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

    private ArrayList<Filter> getOldAllFilters(SQLiteDatabase db, int oldVersion) {
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
            };
            if (oldVersion == DatabaseMigration.DATABASE_VERSION_ADD_ENABLED_TO_FILTER) {
                columns = new String[]{
                        Filter.ID,
                        Filter.TITLE,
                        Filter.KEYWORD,
                        Filter.URL,
                        Filter.FEED_ID,
                        Filter.ENABLED
                };
            }
            cursor = db.query(Filter.TABLE_NAME, columns, "", null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int filterId = cursor.getInt(0);
                    String title = cursor.getString(1);
                    String keyword = cursor.getString(2);
                    String url = cursor.getString(3);
                    int feedId = cursor.getInt(4);
                    int enabled = Filter.TRUE;
                    if (oldVersion == DatabaseMigration.DATABASE_VERSION_ADD_ENABLED_TO_FILTER) {
                        enabled = cursor.getInt(5);
                    }
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
