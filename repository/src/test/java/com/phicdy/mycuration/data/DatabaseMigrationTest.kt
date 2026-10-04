package com.phicdy.mycuration.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.phicdy.mycuration.repository.Database
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Checks that databases created by older app versions are upgraded correctly.
 *
 * Existing installs store the schema version in SQLite's user_version, and the app migrates
 * from that version to [Database.Schema.version] using the .sqm files.
 */
class DatabaseMigrationTest {

    private lateinit var driver: SqlDriver

    @Before
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    }

    @After
    fun tearDown() {
        driver.close()
    }

    @Test
    fun schemaVersionIsUnchanged() {
        // Installs of the current release are at version 5 (4 migration files + 1).
        // If this changes, migrations would run again (or a downgrade crash would happen) on update.
        assertThat(Database.Schema.version).isEqualTo(5L)
    }

    @Test
    fun migrateFromVersion4KeepsDataAndAddsFavoriteArticles() {
        createVersion4Schema()
        insertSampleData()

        Database.Schema.migrate(driver, 4, Database.Schema.version)

        assertThat(tableNames()).contains("favoriteArticles")
        assertSampleDataIsKept(expectedIconPath = "https://example.com/icon.png")

        // The upgraded database works with the generated queries
        val database = Database(driver)
        database.favoriteArticleQueries.insert(1)
        assertThat(count("favoriteArticles")).isEqualTo(1)
    }

    @Test
    fun migrateFromVersion3ResetsIconPathAndKeepsData() {
        createVersion4Schema()
        insertSampleData()

        Database.Schema.migrate(driver, 3, Database.Schema.version)

        assertThat(tableNames()).contains("favoriteArticles")
        assertSampleDataIsKept(expectedIconPath = "defaultIconPath")
    }

    @Test
    fun migratedSchemaHasSameTablesAndColumnsAsFreshInstall() {
        createVersion4Schema()
        Database.Schema.migrate(driver, 4, Database.Schema.version)
        val migrated = tableColumns()

        val freshDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            Database.Schema.create(freshDriver)
            assertThat(migrated).isEqualTo(tableColumns(freshDriver))
        } finally {
            freshDriver.close()
        }
    }

    /**
     * Version 4 is the current schema without favoriteArticles, which was added by 4.sqm.
     */
    private fun createVersion4Schema() {
        Database.Schema.create(driver)
        driver.execute(null, "DROP TABLE favoriteArticles", 0)
    }

    private fun insertSampleData() {
        driver.execute(
            null,
            "INSERT INTO feeds(title, url, format, siteUrl, iconPath, unreadArticle) " +
                "VALUES ('Example', 'https://example.com/feed', 'RSS2.0', 'https://example.com', 'https://example.com/icon.png', 1)",
            0
        )
        driver.execute(
            null,
            "INSERT INTO articles(title, url, status, point, date, feedId) " +
                "VALUES ('Article', 'https://example.com/1', 'unread', '-1', 1700000000000, 1)",
            0
        )
        driver.execute(null, "INSERT INTO curations(name) VALUES ('Curation')", 0)
        driver.execute(null, "INSERT INTO curationConditions(curationId, word) VALUES (1, 'word')", 0)
        driver.execute(null, "INSERT INTO filters(keyword, url, title, enabled) VALUES ('ad', '', 'Filter', 1)", 0)
        driver.execute(null, "INSERT INTO filterFeedRegistrations(filterId, feedId) VALUES (1, 1)", 0)
    }

    private fun assertSampleDataIsKept(expectedIconPath: String) {
        assertThat(count("feeds")).isEqualTo(1)
        assertThat(count("articles")).isEqualTo(1)
        assertThat(count("curations")).isEqualTo(1)
        assertThat(count("curationConditions")).isEqualTo(1)
        assertThat(count("filters")).isEqualTo(1)
        assertThat(count("filterFeedRegistrations")).isEqualTo(1)
        assertThat(singleString("SELECT iconPath FROM feeds")).isEqualTo(expectedIconPath)
        assertThat(singleString("SELECT title FROM articles")).isEqualTo("Article")
    }

    private fun count(table: String): Long =
        driver.executeQuery(null, "SELECT COUNT(*) FROM $table", { cursor ->
            cursor.next()
            QueryResult.Value(cursor.getLong(0)!!)
        }, 0).value

    private fun singleString(sql: String): String? =
        driver.executeQuery(null, sql, { cursor ->
            cursor.next()
            QueryResult.Value(cursor.getString(0))
        }, 0).value

    private fun tableNames(target: SqlDriver = driver): List<String> =
        target.executeQuery(
            null,
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name",
            { cursor ->
                val names = mutableListOf<String>()
                while (cursor.next().value) {
                    names.add(cursor.getString(0)!!)
                }
                QueryResult.Value(names)
            },
            0
        ).value

    /**
     * Column names per table. Nullability is not compared because 4.sqm created
     * favoriteArticles.articleId without NOT NULL, unlike FavoriteArticle.sq.
     */
    private fun tableColumns(target: SqlDriver = driver): Map<String, List<String>> =
        tableNames(target).associateWith { table ->
            target.executeQuery(null, "PRAGMA table_info($table)", { cursor ->
                val columns = mutableListOf<String>()
                while (cursor.next().value) {
                    columns.add(cursor.getString(1)!!)
                }
                QueryResult.Value(columns.sorted())
            }, 0).value
        }
}
