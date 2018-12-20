package com.phicdy.mycuration.data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.data.rss.Article
import com.phicdy.mycuration.data.rss.Curation
import com.phicdy.mycuration.data.rss.CurationCondition
import com.phicdy.mycuration.data.rss.CurationSelection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.ArrayList

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
            num = cursor.count
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.endTransaction()
        }

        return@withContext num
    }

    suspend fun getAllCurationWords(): HashMap<Int, ArrayList<String>> = withContext(Dispatchers.IO) {
        val curationWordsMap = hashMapOf<Int, ArrayList<String>>()
        val sql = "select " + Curation.TABLE_NAME + "." + Curation.ID + "," +
                CurationCondition.TABLE_NAME + "." + CurationCondition.WORD +
                " from " + Curation.TABLE_NAME + " inner join " + CurationCondition.TABLE_NAME +
                " where " + Curation.TABLE_NAME + "." + Curation.ID + " = " + CurationCondition.TABLE_NAME + "." + CurationCondition.CURATION_ID +
                " order by " + Curation.TABLE_NAME + "." + Curation.ID
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(sql, null)
            val defaultCurationId = -1
            var curationId = defaultCurationId
            var words = ArrayList<String>()
            while (cursor.moveToNext()) {
                val newCurationId = cursor.getInt(0)
                if (curationId == defaultCurationId) {
                    curationId = newCurationId
                }
                // Add words of curation to map when curation ID changes
                if (curationId != newCurationId) {
                    curationWordsMap[curationId] = words
                    curationId = newCurationId
                    words = ArrayList()
                }
                val word = cursor.getString(1)
                words.add(word)
            }
            // Add last words of curation
            if (curationId != defaultCurationId) {
                curationWordsMap[curationId] = words
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return@withContext curationWordsMap
    }

    suspend fun saveCurationsOf(articles: List<Article>) = coroutineScope {
        withContext(Dispatchers.IO) {
            val curationWordMap = getAllCurationWords()
            val insertCurationSelectionSt = db.compileStatement(
                    "insert into " + CurationSelection.TABLE_NAME +
                            "(" + CurationSelection.ARTICLE_ID + "," + CurationSelection.CURATION_ID + ") values (?,?);")
            for (curationId in curationWordMap.keys) {
                val words = curationWordMap[curationId]
                words?.forEach { word ->
                    for (article in articles) {
                        if (article.title.contains(word)) {
                            try {
                                db.beginTransaction()
                                insertCurationSelectionSt.bindString(1, article.id.toString())
                                insertCurationSelectionSt.bindString(2, curationId.toString())
                                insertCurationSelectionSt.executeInsert()
                                db.setTransactionSuccessful()
                            } catch (e: SQLException) {
                                Timber.e(e.toString())
                                Timber.e("article ID: %s, curatation ID: %s", article.id, curationId)
                                Timber.e(curationWordMap.toString())
                                Timber.e(article.toString())
                            } finally {
                                db.endTransaction()
                            }
                            break
                        }
                    }
                }
            }
        }
    }

    suspend fun update(curationId: Int, name: String, words: ArrayList<String>): Boolean = withContext(Dispatchers.IO) {
        var result = true
        try {
            // Update curation name
            val values = ContentValues().apply {
                put(Curation.NAME, name)
            }
            db.beginTransaction()
            db.update(Curation.TABLE_NAME, values, Curation.ID + " = " + curationId, null)

            // Delete old curation conditions and insert new one
            db.delete(CurationCondition.TABLE_NAME, CurationCondition.CURATION_ID + " = " + curationId, null)
            for (word in words) {
                val condtionValue = ContentValues().apply {
                    put(CurationCondition.CURATION_ID, curationId)
                    put(CurationCondition.WORD, word)
                }
                db.insert(CurationCondition.TABLE_NAME, null, condtionValue)
            }
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            Timber.e(e)
            result = false
        } finally {
            db.endTransaction()
        }
        return@withContext result
    }

    suspend fun store(name: String, words: ArrayList<String>): Long = withContext(Dispatchers.IO) {
        if (words.isEmpty()) return@withContext -1L
        var addedCurationId  = -1L
        try {
            val values = ContentValues().apply {
                put(Curation.NAME, name)
            }
            db.beginTransaction()
            addedCurationId = db.insert(Curation.TABLE_NAME, null, values)
            for (word in words) {
                val condtionValue = ContentValues().apply {
                    put(CurationCondition.CURATION_ID, addedCurationId)
                    put(CurationCondition.WORD, word)
                }
                db.insert(CurationCondition.TABLE_NAME, null, condtionValue)
            }
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            Timber.e(e)
        } finally {
            db.endTransaction()
        }
        return@withContext addedCurationId
    }

    suspend fun adaptToArticles(curationId: Int, words: ArrayList<String>): Boolean = withContext(Dispatchers.IO) {
        if (curationId == NOT_FOUND_ID) return@withContext false

        var result = true
        var cursor: Cursor? = null
        try {
            val insertSt = db.compileStatement("insert into " + CurationSelection.TABLE_NAME +
                            "(" + CurationSelection.ARTICLE_ID + "," + CurationSelection.CURATION_ID + ") values (?," + curationId + ");")
            db.beginTransaction()
            // Delete old curation selection
            db.delete(CurationSelection.TABLE_NAME, CurationSelection.CURATION_ID + " = " + curationId, null)

            // Get all articles
            val columns = arrayOf(Article.ID, Article.TITLE)
            cursor = db.query(Article.TABLE_NAME, columns, null, null, null, null, null)

            // Adapt
            while (cursor!!.moveToNext()) {
                val articleId = cursor.getInt(0)
                val articleTitle = cursor.getString(1)
                for (word in words) {
                    if (articleTitle.contains(word)) {
                        insertSt.bindString(1, articleId.toString())
                        insertSt.executeInsert()
                        break
                    }
                }
            }
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            Timber.e(e)
            result = false
        } finally {
            cursor?.close()
            db.endTransaction()
        }
        return@withContext result
    }

    companion object {
        private const val NOT_FOUND_ID = -1
    }
}
