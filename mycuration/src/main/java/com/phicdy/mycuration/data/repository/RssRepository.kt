package com.phicdy.mycuration.data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.VisibleForTesting
import com.phicdy.mycuration.domain.entity.Article
import com.phicdy.mycuration.domain.entity.CurationSelection
import com.phicdy.mycuration.domain.entity.Feed
import com.phicdy.mycuration.domain.entity.Filter
import com.phicdy.mycuration.domain.entity.FilterFeedRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.ArrayList

class RssRepository(private val db: SQLiteDatabase,
                    private val articleRepository: ArticleRepository,
                    private val filterRepository: FilterRepository) {

    suspend fun getNumOfRss(): Int = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            var num: Int
            var cursor: Cursor? = null
            try {
                db.beginTransaction()
                cursor = db.query(Feed.TABLE_NAME, arrayOf(Feed.ID), "", emptyArray(), "", "", "")
                num = cursor.count
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                num = -1
            } finally {
                cursor?.close()
                db.endTransaction()
            }

            return@withContext num
        }
    }

    /**
     * Update method for rss title.
     *
     * @param rssId RSS ID to update
     * @param newTitle New rss title
     * @return Num of updated rss
     */
    suspend fun saveNewTitle(rssId: Int, newTitle: String): Int = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            var numOfUpdated = 0
            db.beginTransaction()
            try {
                val values = ContentValues().apply {
                    put(Feed.TITLE, newTitle)
                }
                numOfUpdated = db.update(Feed.TABLE_NAME, values, Feed.ID + " = " + rssId, null)
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                db.endTransaction()
            }
            return@withContext numOfUpdated
        }
    }

    /**
     * Delete method for rss and related data.
     *
     * @param rssId Feed ID to delete
     * @return result of delete
     */
    suspend fun deleteRss(rssId: Int): Boolean = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            var numOfDeleted = 0
            try {
                db.beginTransaction()

                val allArticlesInRss = articleRepository.getAllArticlesInRss(rssId, true)
                for (article in allArticlesInRss) {
                    db.delete(CurationSelection.TABLE_NAME, CurationSelection.ARTICLE_ID + " = " + article.id, null)
                }
                db.delete(Article.TABLE_NAME, Article.FEEDID + " = " + rssId, null)

                // Delete related filter
                db.delete(FilterFeedRegistration.TABLE_NAME, FilterFeedRegistration.FEED_ID + " = " + rssId, null)
                val filters = filterRepository.getAllFilters()
                for (filter in filters) {
                    val rsss = filter.feeds
                    if (rsss.size == 1 && rsss.get(0).id == rssId) {
                        // This filter had relation with this rss only
                        db.delete(Filter.TABLE_NAME, Filter.ID + " = " + filter.id, null)
                    }
                }

                numOfDeleted = db.delete(Feed.TABLE_NAME, Feed.ID + " = " + rssId, null)
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                db.endTransaction()
            }
            return@withContext numOfDeleted == 1
        }
    }

    /**
     * Get method to feed array with unread count of articles.
     *
     * @return Feed array with unread count of articles
     */
    suspend fun getAllFeedsWithNumOfUnreadArticles(): ArrayList<Feed> = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            var feedList = ArrayList<Feed>()
            db.beginTransaction()
            var cursor: Cursor? = null
            try {
                val columns = arrayOf(Feed.ID, Feed.TITLE, Feed.URL, Feed.ICON_PATH, Feed.SITE_URL, Feed.UNREAD_ARTICLE)
                val orderBy = Feed.TITLE
                cursor = db.query(Feed.TABLE_NAME, columns, null, null, null, null, orderBy)
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val id = cursor.getInt(0)
                        val title = cursor.getString(1)
                        val url = cursor.getString(2)
                        val iconPath = cursor.getString(3)
                        val siteUrl = cursor.getString(4)
                        val unreadAriticlesCount = cursor.getInt(5)
                        feedList.add(Feed(id, title, url, iconPath, "", unreadAriticlesCount, siteUrl))
                    }
                }
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                cursor?.close()
                db.endTransaction()
            }

            if (feedList.size == 0) {
                feedList = getAllFeedsWithoutNumOfUnreadArticles()
            }
            return@withContext feedList
        }
    }

    /**
     * Get method to feed array without unread count of articles.
     *
     * @return Feed array without unread count of articles
     */
    suspend fun getAllFeedsWithoutNumOfUnreadArticles(): ArrayList<Feed> = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            val feedList = ArrayList<Feed>()
            val columns = arrayOf(Feed.ID, Feed.TITLE, Feed.URL, Feed.ICON_PATH, Feed.SITE_URL)
            val orderBy = Feed.TITLE
            var cursor: Cursor? = null
            try {
                db.beginTransaction()
                cursor = db.query(Feed.TABLE_NAME, columns, null, null, null, null, orderBy)
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val id = cursor.getInt(0)
                        val title = cursor.getString(1)
                        val url = cursor.getString(2)
                        val iconPath = cursor.getString(3)
                        val siteUrl = cursor.getString(4)
                        feedList.add(Feed(id, title, url, iconPath, "", 0, siteUrl))
                    }
                }
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                cursor?.close()
                db.endTransaction()
            }

            return@withContext feedList
        }
    }

    /**
     * Update method for unread article count of the feed.
     *
     * @param feedId Feed ID to change
     * @param unreadCount New article unread count
     */
    suspend fun updateUnreadArticleCount(feedId: Int, unreadCount: Int) = withContext(Dispatchers.IO) {
        try {
            db.beginTransaction()
            val values = ContentValues().apply {
                put(Feed.UNREAD_ARTICLE, unreadCount)
            }
            db.update(Feed.TABLE_NAME, values, Feed.ID + " = $feedId", null)
            db.setTransactionSuccessful()
            Timber.d("Finished to update unread article count to $unreadCount in DB. RSS ID is $feedId")
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    fun resetIconPath() {
        try {
            db.beginTransaction()
            val values = ContentValues().apply {
                put(Feed.ICON_PATH, Feed.DEDAULT_ICON_PATH)
            }
            db.update(Feed.TABLE_NAME, values, null, null)
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    @VisibleForTesting
    fun getFeedWithUnreadCountBy(rssId: Int): Feed? {
        var feed: Feed? = null
        db.beginTransaction()
        var cur: Cursor? = null
        try {
            val culumn = arrayOf(Feed.TITLE, Feed.URL, Feed.ICON_PATH, Feed.SITE_URL, Feed.UNREAD_ARTICLE)
            val selection = Feed.ID + " = " + rssId
            cur = db.query(Feed.TABLE_NAME, culumn, selection, null, null, null, null)
            if (cur.count != 0) {
                cur.moveToNext()
                val feedTitle = cur.getString(0)
                val feedUrl = cur.getString(1)
                val iconPath = cur.getString(2)
                val siteUrl = cur.getString(3)
                val count = cur.getInt(4)
                feed = Feed(rssId, feedTitle, feedUrl, iconPath, "", count, siteUrl)
            }
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            cur?.close()
            db.endTransaction()
        }
        return feed
    }

    /**
     * Update method for feed icon path.
     *
     * @param siteUrl Site URL of the feed to change
     * @param iconPath New icon path
     */
    suspend fun saveIconPath(siteUrl: String, iconPath: String) = withContext(Dispatchers.IO) {
        try {
            db.beginTransaction()
            val values = ContentValues().apply {
                put(Feed.ICON_PATH, iconPath)
            }
            db.update(Feed.TABLE_NAME, values, Feed.SITE_URL + " = '" + siteUrl + "'", null)
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    /**
     * @return Stored RSS or null if failed or same RSS exist
     */
    suspend fun store(feedTitle: String, feedUrl: String, format: String, siteUrl: String): Feed? = withContext(Dispatchers.IO) {
        var rss: Feed? = null
        var isBeginTransaction = false
        try {
            // Get same feeds from DB
            val selection = Feed.TITLE + "=\"$feedTitle\" and " + Feed.URL + "=\"$feedUrl\" and " +
                    Feed.FORMAT + "=\"$format\""
            val stored = query(arrayOf(Feed.ID), selection)
            if (stored != null) return@withContext null

            // If there aren't same feeds in DB,Insert into DB
            db.beginTransaction()
            isBeginTransaction = true
            val values = ContentValues()
            values.put(Feed.TITLE, feedTitle)
            values.put(Feed.URL, feedUrl)
            values.put(Feed.FORMAT, format)
            values.put(Feed.ICON_PATH, Feed.DEDAULT_ICON_PATH)
            values.put(Feed.SITE_URL, siteUrl)
            values.put(Feed.UNREAD_ARTICLE, 0)
            val id = db.insert(Feed.TABLE_NAME, null, values)
            rss = Feed(
                    id = id.toInt(),
                    title = feedTitle,
                    url = feedUrl,
                    format = format,
                    siteUrl = siteUrl
            )
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            if (isBeginTransaction) db.endTransaction()
        }

        return@withContext rss
    }

    suspend fun getFeedByUrl(feedUrl: String): Feed? = withContext(Dispatchers.IO) {
        val columns = arrayOf(Feed.ID, Feed.TITLE, Feed.URL, Feed.ICON_PATH, Feed.SITE_URL, Feed.UNREAD_ARTICLE)
        val selection = Feed.URL + " = \"" + feedUrl + "\""
        return@withContext query(columns, selection)
    }

    suspend fun getFeedById(feedId: Int): Feed? = withContext(Dispatchers.IO) {
        val columns = arrayOf(Feed.TITLE, Feed.URL, Feed.ICON_PATH, Feed.SITE_URL)
        val selection = Feed.ID + " = " + feedId
        return@withContext query(columns, selection)
    }

    private suspend fun query(columns: Array<String>, selection: String? = null): Feed? = coroutineScope {
        var feed: Feed? = null
        var cur: Cursor? = null
        try {
            db.beginTransaction()
            cur = db.query(Feed.TABLE_NAME, columns, selection, null, null, null, null)
            if (cur.count != 0) {
                cur.moveToNext()
                var id = 0
                var title = ""
                var url = ""
                var iconPath = ""
                var siteUrl = ""
                var count = 0
                for ((i, column) in columns.withIndex()) {
                    when (column) {
                        Feed.ID -> id = cur.getInt(i)
                        Feed.TITLE -> title = cur.getString(i)
                        Feed.URL -> url = cur.getString(i)
                        Feed.ICON_PATH -> iconPath = cur.getString(i)
                        Feed.SITE_URL -> siteUrl = cur.getString(i)
                        Feed.UNREAD_ARTICLE -> count = cur.getInt(i)
                    }
                }
                feed = Feed(id, title, url, iconPath, "", count, siteUrl)
            }
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            cur?.close()
            db.endTransaction()
        }
        return@coroutineScope feed
    }

    suspend fun addManyFeeds() {
        val feeds = ArrayList<Feed>()
        //RSS 2.0
        //		feeds.add(new Feed(0, "スポーツナビ - ピックアップ　ゲーム",
        //				"http://sports.yahoo.co.jp/rss/pickup_game/pc"));
        //		feeds.add(new Feed(0, "Yahoo!ニュース・トピックス - トップ",
        //				"http://rss.dailynews.yahoo.co.jp/fc/rss.xml"));
        //		feeds.add(new Feed(0, "Yahoo!ニュース・トピックス - 海外",
        //				"http://rss.dailynews.yahoo.co.jp/fc/world/rss.xml"));
        //		feeds.add(new Feed(0, "Yahoo!ニュース・トピックス - 経済",
        //				"http://rss.dailynews.yahoo.co.jp/fc/economy/rss.xml"));
        //		feeds.add(new Feed(0, "Yahoo!ニュース・トピックス - エンターテインメント",
        //				"http://rss.dailynews.yahoo.co.jp/fc/entertainment/rss.xml"));
        feeds.add(Feed(0, "IT速報",
                "http://blog.livedoor.jp/itsoku/index.rdf", "", "http://blog.livedoor.jp/itsoku/", 0, ""))
        feeds.add(Feed(0, "あじゃじゃしたー",
                "http://blog.livedoor.jp/chihhylove/index.rdf", "", "http://blog.livedoor.jp/chihhylove/", 0, ""))
        feeds.add(Feed(0, "はてなブログ人気エントリー",
                "http://b.hatena.ne.jp/hotentry.rss", "", "http://b.hatena.ne.jp", 0, ""))
        feeds.add(Feed(0, "はてなブックマーク - 人気エントリー - テクノロジー",
                "http://b.hatena.ne.jp/hotentry/it.rss", "", "http://b.hatena.ne.jp/hotentry", 0, ""))
        feeds.add(Feed(0, "暇人速報",
                "http://himasoku.com/index.rdf", "", "http://himasoku.com", 0, ""))
        feeds.add(Feed(0, "ドメサカブログ",
                "http://blog.livedoor.jp/domesoccer/index.rdf", "", "http://blog.livedoor.jp/domesoccer/", 0, ""))
        feeds.add(Feed(0, "きんどう",
                "http://kindou.info/feed", "", "http://kindou.info", 0, ""))
        feeds.add(Feed(0, "GGSOKU - ガジェット速報",
                "http://ggsoku.com/feed", "", "http://ggsoku.com", 0, ""))
        feeds.add(Feed(0, "Act as Professional",
                "http://hiroki.jp/feed/", "", "http://hiroki.jp", 0, ""))
        feeds.add(Feed(0, "Developers.IO",
                "http://dev.classmethod.jp/feed/", "", "http://dev.classmethod.jp", 0, ""))
        feeds.add(Feed(0, "GREE Engineers' Blog",
                "http://labs.gree.jp/blog/feed", "", "http://labs.gree.jp/blog", 0, ""))
        feeds.add(Feed(0, "HTC速報",
                "http://htcsoku.info/feed/", "", "http://htcsoku.info", 0, ""))
        feeds.add(Feed(0, "Hatena Developer Blog",
                "http://developer.hatenastaff.com/rss", "", "http://developer.hatenastaff.com/", 0, ""))
        feeds.add(Feed(0, "ITmedia 総合記事一覧",
                "http://rss.rssad.jp/rss/itmtop/2.0/itmedia_all.xml", "", "http://www.itmedia.co.jp/", 0, ""))
        feeds.add(Feed(0, "Publickey",
                "http://www.publickey1.jp/atom.xml", "", "http://www.publickey1.jp/", 0, ""))
        feeds.add(Feed(0, "Tech Booster",
                "http://techbooster.jpn.org/feed/", "", "http://techbooster.jpn.org", 0, ""))
        feeds.add(Feed(0, "TechCrunch Japan",
                "http://jp.techcrunch.com/feed/", "", "http://jp.techcrunch.com", 0, ""))
        feeds.add(Feed(0, "あんどろいど速報",
                "http://androidken.blog119.fc2.com/?xml", "", "http://androidken.blog119.fc2.com/", 0, ""))
        feeds.add(Feed(0, "＠IT 全フォーラム 最新記事一覧",
                "http://www.atmarkit.co.jp/", "", "http://rss.rssad.jp/rss/itmatmarkit/rss.xml", 0, ""))
        //atom
        //		feeds.add(new Feed(0, "TweetBuzz - 注目エントリー",
        //				"http://feeds.feedburner.com/tb-hotentry"));
        //RDF
        //		feeds.add(new Feed(0, "二十歳街道まっしぐら",
        //				"http://20kaido.com/index.rdf"));

        for ((_, title, url, _, _, _, siteUrl) in feeds) {
            store(title, url, "RSS2.0", siteUrl)
        }
    }
}