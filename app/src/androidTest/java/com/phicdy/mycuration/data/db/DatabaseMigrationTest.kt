package com.phicdy.mycuration.data.db

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.deleteAll
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.Filter
import com.phicdy.mycuration.entity.FilterFeedRegistration
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.ArrayList

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    private lateinit var db: SQLiteDatabase
    private lateinit var rssRepository: RssRepository
    private lateinit var filterRepository: FilterRepository

    @Before
    fun setUp() {
        val helper = DatabaseHelper(ApplicationProvider.getApplicationContext())
        db = helper.writableDatabase
        filterRepository = FilterRepository(db)
        rssRepository = RssRepository(db, ArticleRepository(db), filterRepository)
    }

    @After
    fun tearDown() {
        val db = DatabaseHelper(ApplicationProvider.getApplicationContext()).writableDatabase
        deleteAll(db)
    }

    @Suppress("Deprecation")
    @Test
    fun migrationFrom1To3() = runBlocking {
        db.execSQL(Filter.DROP_TABLE_SQL)
        db.execSQL(FilterFeedRegistration.DROP_TABLE_SQL)
        db.execSQL(Filter.CREATE_TABLE_SQL_VER1)

        val testFeed = rssRepository.store(TEST_FEED_TITLE, TEST_FEED_URL, "RSS", TEST_FEED_URL)
        val values = ContentValues().apply {
            put(Filter.TITLE, "hoge")
            put(Filter.FEED_ID, testFeed!!.id)
            put(Filter.KEYWORD, "keyword")
            put(Filter.URL, "http://www.google.com")
        }
        val filterId = db.insert(Filter.TABLE_NAME, null, values)
        assertNotEquals(filterId, -1)

        val migration = DatabaseMigration(
                DatabaseMigration.FIRST_VERSION,
                DatabaseMigration.DATABASE_VERSION_ADD_FILTER_FEED_REGISTRATION
        )
        migration.migrate(db)

        var cursor: Cursor? = null
        try {
            // Check feed ID column does not exist
            cursor = db.rawQuery("PRAGMA table_info(" + Filter.TABLE_NAME + ")", null)
            val value = cursor!!.getColumnIndex(Filter.FEED_ID)
            assertThat(value, `is`(-1))
        } catch (e: SQLException) {
            e.printStackTrace()
            fail("SQL error: " + e.message)
        } finally {
            cursor?.close()
        }

        val filters = filterRepository.getAllFilters()
        assertThat(filters.size, `is`(1))
        val filter = filters[0]
        assertEquals(filter.title, "hoge")
        assertEquals(filter.isEnabled, true)
        val target = ArrayList<Feed>()
        testFeed?.let {
            target.add(testFeed)
            assertTrue(filterRepository.saveNewFilter("hoge", target, "hoge", "http://www.google.com"))
        } ?: fail("RSS is null")
    }

    @Suppress("Deprecation")
    @Test
    fun migrationFrom2To3() = runBlocking {
        db.execSQL(Filter.DROP_TABLE_SQL)
        db.execSQL(FilterFeedRegistration.DROP_TABLE_SQL)
        db.execSQL(Filter.CREATE_TABLE_SQL_VER2)
        val testFeed = rssRepository.store(TEST_FEED_TITLE, TEST_FEED_URL, "RSS", TEST_FEED_URL)
        val values = ContentValues().apply {
            put(Filter.TITLE, "hoge")
            put(Filter.FEED_ID, testFeed!!.id)
            put(Filter.KEYWORD, "keyword")
            put(Filter.URL, "http://www.google.com")
            put(Filter.ENABLED, false)
        }
        val filterId = db.insert(Filter.TABLE_NAME, null, values)
        assertNotEquals(filterId, -1)
        val migration = DatabaseMigration(
                DatabaseMigration.DATABASE_VERSION_ADD_ENABLED_TO_FILTER,
                DatabaseMigration.DATABASE_VERSION_ADD_FILTER_FEED_REGISTRATION
        )
        migration.migrate(db)

        var cursor: Cursor? = null
        try {
            // Check feed ID column does not exist
            cursor = db.rawQuery("PRAGMA table_info(" + Filter.TABLE_NAME + ")", null)
            val value = cursor!!.getColumnIndex(Filter.FEED_ID)
            assertThat(value, `is`(-1))
        } catch (e: SQLException) {
            e.printStackTrace()
            fail("SQL error: " + e.message)
        } finally {
            cursor?.close()
        }

        val filters = filterRepository.getAllFilters()
        assertThat(filters.size, `is`(1))
        val filter = filters[0]
        assertEquals(filter.title, "hoge")
        assertEquals(filter.isEnabled, false)
        val target = ArrayList<Feed>()
        testFeed?.let {
            target.add(testFeed)
            assertTrue(filterRepository.saveNewFilter("hoge", target, "hoge", "http://www.google.com"))
        } ?: fail("RSS is null")
    }

    @Test
    fun migrationFrom3To4() = runBlocking {
        val rss = rssRepository.store(TEST_FEED_TITLE, TEST_FEED_URL, Feed.ATOM, TEST_FEED_URL)
        rssRepository.saveIconPath(TEST_FEED_URL, "$TEST_FEED_URL/icon")
        val migration = DatabaseMigration(
                oldVersion = DatabaseMigration.DATABASE_VERSION_ADD_FILTER_FEED_REGISTRATION,
                newVersion = DatabaseMigration.DATABASE_VERSION_FETCH_ICON
        )
        migration.migrate(db)
        rss?.let {
            val migratedRss = rssRepository.getFeedById(rss.id)
            assertThat(migratedRss?.iconPath, `is`(Feed.DEDAULT_ICON_PATH))
        } ?: fail("RSS is null")
    }

    companion object {

        private const val TEST_FEED_TITLE = "testfeed"
        private const val TEST_FEED_URL = "http://www.yahoo.co.jp"
    }
}
