package com.phicdy.mycuration.data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.phicdy.mycuration.data.filter.Filter
import com.phicdy.mycuration.data.rss.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class ArticleRepository(val db: SQLiteDatabase) {

    /**
     * Update method from "to read" to "read" for all of the articles.
     */
    suspend fun saveAllStatusToReadFromToRead() = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put(Article.STATUS, Article.READ)
                val condition = Article.STATUS + " = '" + Article.TOREAD + "'"
                db.update(Article.TABLE_NAME, values, condition, null)
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                db.endTransaction()
            }
        }
    }

    suspend fun getAllArticlesInRss(rssId: Int, isNewestArticleTop: Boolean): ArrayList<Article> = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            val articles = ArrayList<Article>()
            var cursor: Cursor? = null
            try {
                db.beginTransaction()
                // Get unread articles
                val sql = ("select " + Article.ID + ", " + Article.TITLE + ", " + Article.URL + ", " + Article.STATUS + "" +
                        ", " + Article.POINT + ", " + Article.DATE + " from " + Article.TABLE_NAME + " where " + Article.FEEDID + " = "
                        + rssId + " order by " + Article.DATE) + if (isNewestArticleTop) " desc" else " asc"
                cursor = db.rawQuery(sql, null)
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(0)
                    val title = cursor.getString(1)
                    val url = cursor.getString(2)
                    val status = cursor.getString(3)
                    val point = cursor.getString(4)
                    val dateLong = cursor.getLong(5)
                    val article = Article(id, title, url, status, point,
                            dateLong, rssId, "", "")
                    articles.add(article)
                }
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                return@withContext articles
            } finally {
                db.endTransaction()
                cursor?.close()
            }

            return@withContext articles
        }
    }

    suspend fun applyFiltersOfRss(filterList: ArrayList<Filter>, rssId: Int) = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            // If articles are hit in condition, Set articles status to "read"
            val value = ContentValues().apply {
                put(Article.STATUS, Article.READ)
            }
            for ((id, _, keyword, url) in filterList) {
                try {
                    // If keyword or url exists, add condition
                    if (keyword.isBlank() && url.isBlank()) {
                        Log.w("Set filtering conditon",
                                "keyword and url don't exist fileter ID =$id")
                        continue
                    }

                    db.beginTransaction()
                    // Initialize condition
                    var condition = Article.FEEDID + " = $rssId"
                    if (keyword.isNotBlank()) { condition = "$condition and title like '%$keyword%'" }
                    if (url.isNotBlank()) { condition = "$condition and url like '%$url%'" }
                    db.update(Article.TABLE_NAME, value, condition, null)
                    db.setTransactionSuccessful()
                } catch (e: Exception) {
                    Log.e("Apply Filtering", "Article can't be updated.Feed ID = $rssId")
                    db.endTransaction()
                    return@withContext
                } finally {
                    db.endTransaction()
                }
            }
        }
    }

}