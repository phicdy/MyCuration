package com.phicdy.mycuration.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.SparseArray;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.db.DatabaseHelper;
import com.phicdy.mycuration.db.DatabaseMigrationTask;
import com.phicdy.mycuration.filter.Filter;
import com.phicdy.mycuration.filter.FilterFeedRegistration;
import com.phicdy.mycuration.rss.Article;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.ui.TopActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;

import static android.support.test.InstrumentationRegistry.getContext;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class DatabaseMigrationTest {

	private SQLiteDatabase db;
    private DatabaseAdapter adapter;

	private static final String TEST_FEED_TITLE = "testfeed";
	private static final String TEST_FEED_URL = "http://www.yahoo.co.jp";

	public DatabaseMigrationTest() {
		super();
	}

    @Rule
    public ActivityTestRule<TopActivity> mActivityRule = new ActivityTestRule<>(
            TopActivity.class);

    @Before
	public void setUp() {
        adapter = DatabaseAdapter.getInstance(getTargetContext());
        DatabaseHelper helper = new DatabaseHelper(getTargetContext());
        db = helper.getWritableDatabase();
	}

    @After
    public void tearDown() {
        adapter.deleteAll();
    }

    @Test
    public void migrationFrom1To3() {
        db.execSQL(Filter.DROP_TABLE_SQL);
        db.execSQL(FilterFeedRegistration.DROP_TABLE_SQL);
        db.execSQL(Filter.CREATE_TABLE_SQL_VER1);

        Feed testFeed = adapter.saveNewFeed(TEST_FEED_TITLE, TEST_FEED_URL, "RSS", TEST_FEED_URL);
        ContentValues values = new ContentValues();
        values.put(Filter.TITLE, "hoge");
        values.put(Filter.FEED_ID, testFeed.getId());
        values.put(Filter.KEYWORD, "keyword");
        values.put(Filter.URL, "http://www.google.com");
        long filterId = db.insert(Filter.TABLE_NAME, null, values);
        assertNotEquals(filterId, -1);

        DatabaseMigration migration = new DatabaseMigration(
                DatabaseMigration.FIRST_VERSION,
                DatabaseMigration.DATABASE_VERSION_ADD_FILTER_FEED_REGISTRATION
        );
        migration.migrate(db);

        Cursor cursor = null;
        try {
            // Check feed ID column does not exist
            cursor = db.rawQuery("PRAGMA table_info(" + Filter.TABLE_NAME + ")", null);
            int value = cursor.getColumnIndex(Filter.FEED_ID);
            assertThat(value, is(-1));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("SQL error: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        ArrayList<Filter> filters = adapter.getAllFilters();
        assertThat(filters.size(), is(1));
        Filter filter = filters.get(0);
        assertEquals(filter.getTitle(), "hoge");
        assertEquals(filter.isEnabled(), true);
        ArrayList<Feed> target = new ArrayList<>();
        target.add(testFeed);
        assertTrue(adapter.saveNewFilter("hoge", target, "hoge", "http://www.google.com"));
    }

    @Test
    public void migrationFrom2To3() {
        db.execSQL(Filter.DROP_TABLE_SQL);
        db.execSQL(FilterFeedRegistration.DROP_TABLE_SQL);
        db.execSQL(Filter.CREATE_TABLE_SQL_VER2);
        Feed testFeed = adapter.saveNewFeed(TEST_FEED_TITLE, TEST_FEED_URL, "RSS", TEST_FEED_URL);
        ContentValues values = new ContentValues();
        values.put(Filter.TITLE, "hoge");
        values.put(Filter.FEED_ID, testFeed.getId());
        values.put(Filter.KEYWORD, "keyword");
        values.put(Filter.URL, "http://www.google.com");
        values.put(Filter.ENABLED, false);
        long filterId = db.insert(Filter.TABLE_NAME, null, values);
        assertNotEquals(filterId, -1);
        DatabaseMigration migration = new DatabaseMigration(
                DatabaseMigration.DATABASE_VERSION_ADD_ENABLED_TO_FILTER,
                DatabaseMigration.DATABASE_VERSION_ADD_FILTER_FEED_REGISTRATION
        );
        migration.migrate(db);

        Cursor cursor = null;
        try {
            // Check feed ID column does not exist
            cursor = db.rawQuery("PRAGMA table_info(" + Filter.TABLE_NAME + ")", null);
            int value = cursor.getColumnIndex(Filter.FEED_ID);
            assertThat(value, is(-1));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("SQL error: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        ArrayList<Filter> filters = adapter.getAllFilters();
        assertThat(filters.size(), is(1));
        Filter filter = filters.get(0);
        assertEquals(filter.getTitle(), "hoge");
        assertEquals(filter.isEnabled(), false);
        ArrayList<Feed> target = new ArrayList<>();
        target.add(testFeed);
        assertTrue(adapter.saveNewFilter("hoge", target, "hoge", "http://www.google.com"));
    }
}
