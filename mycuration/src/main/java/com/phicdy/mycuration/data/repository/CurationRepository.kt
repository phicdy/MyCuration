package com.phicdy.mycuration.data.repository

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.data.rss.Article
import com.phicdy.mycuration.data.rss.CurationSelection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CurationRepository(private val db: SQLiteDatabase) {

    suspend fun calcNumOfAllUnreadArticlesOfCuration(curationId: Int): Int = withContext(Dispatchers.IO) {
        var num = 0
        val sql = "select " + Article.TABLE_NAME + "." + Article.ID + "," +
                Article.TABLE_NAME + "." + Article.TITLE + "," +
                Article.TABLE_NAME + "." + Article.URL + "," +
                Article.TABLE_NAME + "." + Article.STATUS + "," +
                Article.TABLE_NAME + "." + Article.POINT + "," +
                Article.TABLE_NAME + "." + Article.DATE + "," +
                Article.TABLE_NAME + "." + Article.FEEDID +
                " from " + Article.TABLE_NAME + " inner join " + CurationSelection.TABLE_NAME +
                " where " + CurationSelection.CURATION_ID + " = " + curationId + " and " +
                Article.TABLE_NAME + "." + Article.STATUS + " = '" + Article.UNREAD + "' and " +
                Article.TABLE_NAME + "." + Article.ID + " = " + CurationSelection.TABLE_NAME + "." + CurationSelection.ARTICLE_ID +
                " order by " + Article.DATE
        var cursor: Cursor? = null
        try {
            db.beginTransaction()
            cursor = db.rawQuery(sql, null)
            num = cursor.getCount()
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.endTransaction()
        }

        return@withContext num
    }
}