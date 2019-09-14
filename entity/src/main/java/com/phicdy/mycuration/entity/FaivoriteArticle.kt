package com.phicdy.mycuration.entity

data class FavoriteArticle(
        val id: Int,
        val articleId: Int
) {

    companion object {
        const val TABLE_NAME = "favoriteArticles"
        const val ID = "_id"
        const val ARTICLE_ID = "articleId"

        const val CREATE_TABLE_SQL = "create table " + TABLE_NAME + "(" +
                ID + " integer primary key autoincrement," +
                ARTICLE_ID + " integer," +
                "foreign key(" + ARTICLE_ID + ") references " + Article.TABLE_NAME + "(" + Article.ID + "))"
    }
}

