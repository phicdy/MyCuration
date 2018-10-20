package com.phicdy.mycuration.data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.data.rss.Article
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.coroutineScope
import kotlinx.coroutines.experimental.withContext

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

}