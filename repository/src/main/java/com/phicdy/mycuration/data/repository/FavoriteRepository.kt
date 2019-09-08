package com.phicdy.mycuration.data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.FavoriteArticle
import com.phicdy.mycuration.entity.FavoriteArticleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FavoriteRepository(val db: SQLiteDatabase) {

    suspend fun store(articleId: Int): Long = withContext(Dispatchers.IO) {
        var id = -1L
        try {
            db.beginTransaction()
            val values = ContentValues().apply {
                put(FavoriteArticle.ARTICLE_ID, articleId)
            }
            id = db.insert(FavoriteArticle.TABLE_NAME, null, values)
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }

        return@withContext id
    }

    suspend fun delete(articleId: Int): Boolean = withContext(Dispatchers.IO) {
        var numOfDeleted = 0
        try {
            db.beginTransaction()
            numOfDeleted = db.delete(FavoriteArticle.TABLE_NAME, FavoriteArticle.ARTICLE_ID + " = " + articleId, null)
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
        return@withContext numOfDeleted == 1
    }

    suspend fun fetchAll(isNewestArticleTop: Boolean): List<FavoriteArticleItem> = withContext(Dispatchers.IO) {
        var columns = arrayOf(
                Article.ID, Article.TITLE, Article.URL, Article.STATUS, Article.POINT, Article.DATE
        ).joinToString(postfix = ", ") {
            Article.TABLE_NAME + "." + it
        }
        columns += arrayOf(FavoriteArticle.ID).joinToString { FavoriteArticle.TABLE_NAME + "." + it }
        val sql = StringBuilder().apply {
            append("select $columns")
            append(" from " + Article.TABLE_NAME + " inner join " + FavoriteArticle.TABLE_NAME)
            append(" where " + Article.TABLE_NAME + "." + Article.ID + " = " + FavoriteArticle.TABLE_NAME + "." + FavoriteArticle.ID)
            append(" order by " + Article.DATE)
            append(if (isNewestArticleTop) {
                " desc"
            } else {
                " asc"
            })
        }.toString()
        val articles = mutableListOf<FavoriteArticleItem>()
        var cursor: Cursor? = null
        try {
            db.beginTransaction()
            cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val title = cursor.getString(1)
                val url = cursor.getString(2)
                val status = cursor.getString(3)
                val point = cursor.getString(4)
                val dateLong = cursor.getLong(5)
                val feedTitle = cursor.getString(6)
                val feedIconPath = cursor.getString(7)
                val article = Article(id, title, url, status, point,
                        dateLong, 0, feedTitle, feedIconPath)
                val favoriteArticleId = cursor.getInt(7)
                articles.add(FavoriteArticleItem(favoriteArticleId, article))
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.endTransaction()
        }

        return@withContext articles
    }
}