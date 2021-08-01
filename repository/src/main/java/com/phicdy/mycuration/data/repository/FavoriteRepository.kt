package com.phicdy.mycuration.data.repository

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.entity.FavoritableArticle
import com.phicdy.mycuration.repository.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
        val db: SQLiteDatabase,
        private val database: Database,
) {

    suspend fun store(articleId: Int): Long = withContext(Dispatchers.IO) {
        var id = -1L
        try {
            database.transaction {
                database.favoriteArticleQueries.insert(articleId.toLong())
                id = database.favoriteArticleQueries.selectLastInsertRowId().executeAsOne()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return@withContext id
    }

    suspend fun delete(articleId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            database.transactionWithResult<Boolean> {
                database.favoriteArticleQueries.delete(articleId.toLong())
                database.favoriteArticleQueries.selectChanges().executeAsOne() == 1L
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return@withContext false
    }

    suspend fun fetchAll(isNewestArticleTop: Boolean): List<FavoritableArticle> = withContext(Dispatchers.IO) {
        try {
            return@withContext database.transactionWithResult<List<FavoritableArticle>> {
                if (isNewestArticleTop) {
                    database.favoriteArticleQueries.getAllOrderByDesc().executeAsList().map {
                        val isFavorite = if (it._id__ == null) false else it._id__ > 0
                        FavoritableArticle(it._id.toInt(), it.title, it.url, it.status, it.point,
                                it.date, it.feedId.toInt(), it.title_, it.iconPath, isFavorite)
                    }
                } else {
                    database.favoriteArticleQueries.getAllOrderByAsc().executeAsList().map {
                        val isFavorite = if (it._id__ == null) false else it._id__ > 0
                        FavoritableArticle(it._id.toInt(), it.title, it.url, it.status, it.point,
                                it.date, it.feedId.toInt(), it.title_, it.iconPath, isFavorite)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext emptyList()
    }
}