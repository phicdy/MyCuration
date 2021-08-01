package com.phicdy.mycuration.data.repository

import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.core.CoroutineDispatcherProvider
import com.phicdy.mycuration.data.GetAllCurationWords
import com.phicdy.mycuration.di.common.ApplicationCoroutineScope
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Curation
import com.phicdy.mycuration.entity.CurationCondition
import com.phicdy.mycuration.entity.CurationSelection
import com.phicdy.mycuration.repository.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurationRepository @Inject constructor(
        private val db: SQLiteDatabase,
        private val database: Database,
        private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
        @ApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
) {

    suspend fun calcNumOfAllUnreadArticlesOfCuration(curationId: Int): Int = withContext(coroutineDispatcherProvider.io()) {
        try {
            return@withContext database.transactionWithResult<Long> {
                database.curationQueries.getCountOfAllUnreadArticlesOfCuration(curationId.toLong()).executeAsOne()
            }.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext 0
    }

    suspend fun getAllCurationWords(): HashMap<Int, ArrayList<String>> = withContext(coroutineDispatcherProvider.io()) {
        val curationWordsMap = hashMapOf<Int, ArrayList<String>>()
        try {
            val defaultCurationId = -1
            var curationId = defaultCurationId
            var words = ArrayList<String>()
            val results = database.transactionWithResult<List<GetAllCurationWords>> {
                database.curationQueries.getAllCurationWords().executeAsList()
            }
            for (result in results) {
                val word = result.word ?: continue
                val newCurationId = result.curationId?.toInt() ?: continue
                if (curationId == defaultCurationId) {
                    curationId = newCurationId
                }
                // Add words of curation to map when curation ID changes
                if (curationId != newCurationId) {
                    curationWordsMap[curationId] = words
                    curationId = newCurationId
                    words = ArrayList()
                }
                words.add(word)
            }
            // Add last words of curation
            if (curationId != defaultCurationId) {
                curationWordsMap[curationId] = words
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext curationWordsMap
    }

    suspend fun saveCurationsOf(articles: List<Article>) = coroutineScope {
        withContext(coroutineDispatcherProvider.io()) {
            val curationWordMap = getAllCurationWords()
            for (curationId in curationWordMap.keys) {
                val words = curationWordMap[curationId]
                database.transaction {
                    words?.forEach { word ->
                        for (article in articles) {
                            if (article.title.contains(word)) {
                                try {
                                    database.curationSelectionQueries.insert(article.id.toLong(), curationId.toLong())
                                } catch (e: SQLException) {
                                    Timber.e(e.toString())
                                    Timber.e("article ID: %s, curatation ID: %s", article.id, curationId)
                                    Timber.e(curationWordMap.toString())
                                    Timber.e(article.toString())
                                }
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun update(curationId: Int, name: String, words: ArrayList<String>): Boolean = withContext(coroutineDispatcherProvider.io()) {
        var result = true
        try {
            database.transaction {
                database.curationQueries.updateNmae(name, curationId.toLong())

                // Delete old curation conditions and insert new one
                database.curationConditionQueries.delete(curationId.toLong())

                for (word in words) {
                    database.curationConditionQueries.insert(curationId.toLong(), word)
                }
            }
        } catch (e: SQLException) {
            Timber.e(e)
            result = false
        } finally {
            db.endTransaction()
        }
        return@withContext result
    }

    suspend fun store(name: String, words: ArrayList<String>): Long = withContext(coroutineDispatcherProvider.io()) {
        if (words.isEmpty()) return@withContext -1L
        var addedCurationId = -1L
        try {
            database.transaction {
                database.curationQueries.insert(name)
                addedCurationId = database.curationQueries.selectLastInsertRowId().executeAsOne()
                for (word in words) {
                    database.curationConditionQueries.insert(addedCurationId, word)
                }
            }
        } catch (e: SQLException) {
            Timber.e(e)
        }
        return@withContext addedCurationId
    }

    suspend fun adaptToArticles(curationId: Int, words: ArrayList<String>): Boolean = withContext(coroutineDispatcherProvider.io()) {
        if (curationId == NOT_FOUND_ID) return@withContext false

        var result = true
        try {
            database.transaction {
                // Delete old curation selection
                db.delete(CurationSelection.TABLE_NAME, CurationSelection.CURATION_ID + " = " + curationId, null)
                database.curationSelectionQueries.deleteByCurationId(curationId.toLong())

                // Get all articles
                val allArticles = database.articleQueries.getAll().executeAsList()

                // Adapt
                for (article in allArticles) {
                    val articleId = article._id
                    val articleTitle = article.title
                    for (word in words) {
                        if (articleTitle.contains(word)) {
                            database.curationSelectionQueries.insert(articleId, curationId.toLong())
                            break
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            Timber.e(e)
            result = false
        }
        return@withContext result
    }

    suspend fun getAllCurations(): List<Curation> = withContext(coroutineDispatcherProvider.io()) {
        return@withContext database.transactionWithResult<List<Curation>> {
            database.curationQueries.getAll()
                    .executeAsList()
                    .map {
                        Curation(
                                id = it._id.toInt(),
                                name = it.name
                        )
                    }
        }
    }

    suspend fun delete(curationId: Int): Boolean = withContext(coroutineDispatcherProvider.io()) {
        var numOfDeleted = 0
        try {
            database.transaction {
                database.curationConditionQueries.delete(curationId.toLong())
                database.curationSelectionQueries.deleteByCurationId(curationId.toLong())
                database.curationQueries.delete(curationId.toLong())
                numOfDeleted = database.curationQueries.selectChanges().executeAsOne().toInt()
            }
        } catch (e: SQLException) {
            Timber.e(e)
        }
        return@withContext numOfDeleted == 1
    }

    suspend fun isExist(name: String): Boolean = withContext(coroutineDispatcherProvider.io()) {
        var num = 0
        var cursor: Cursor? = null
        try {
            val columns = arrayOf(Curation.ID)
            val selection = Curation.NAME + " = ?"
            val selectionArgs = arrayOf(name)
            db.beginTransaction()
            cursor = db.query(Curation.TABLE_NAME, columns, selection, selectionArgs, null, null, null)
            num = cursor.count
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            cursor?.close()
            db.endTransaction()
        }

        return@withContext num > 0
    }

    suspend fun getCurationNameById(curationId: Int): String = withContext(coroutineDispatcherProvider.io()) {
        var name = ""
        val columns = arrayOf(Curation.NAME)
        val selection = Curation.ID + " = ?"
        val selectionArgs = arrayOf(curationId.toString())
        var cursor: Cursor? = null
        try {
            db.beginTransaction()
            cursor = db.query(Curation.TABLE_NAME, columns, selection, selectionArgs, null, null, null)
            cursor.moveToFirst()
            name = cursor.getString(0)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            cursor?.close()
            db.endTransaction()
        }

        return@withContext name
    }

    suspend fun getCurationWords(curationId: Int): ArrayList<String> = withContext(coroutineDispatcherProvider.io()) {
        val words = arrayListOf<String>()
        val columns = arrayOf(CurationCondition.WORD)
        val selection = CurationCondition.CURATION_ID + " = ?"
        val selectionArgs = arrayOf(curationId.toString())
        var cursor: Cursor? = null
        try {
            db.beginTransaction()
            cursor = db.query(CurationCondition.TABLE_NAME, columns, selection, selectionArgs, null, null, null)
            while (cursor.moveToNext()) {
                words.add(cursor.getString(0))
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            cursor?.close()
            db.endTransaction()
        }

        return@withContext words
    }

    companion object {
        private const val NOT_FOUND_ID = -1
    }
}
