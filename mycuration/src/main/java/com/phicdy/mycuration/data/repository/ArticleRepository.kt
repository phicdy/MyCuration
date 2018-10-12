package com.phicdy.mycuration.data.repository

import android.content.ContentValues
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.data.rss.Article


class ArticleRepository(val db: SQLiteDatabase) {

    /**
     * Update method from "to read" to "read" for all of the articles.
     */
    fun saveAllStatusToReadFromToRead() {
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