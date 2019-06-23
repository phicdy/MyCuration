package com.phicdy.mycuration.domain.entity

object CurationSelection {
    const val TABLE_NAME = "curationSelections"
    const val CURATION_ID = "curationId"
    const val ARTICLE_ID = "articleId"
    private const val ID = "_id"

    const val CREATE_TABLE_SQL = "create table " + TABLE_NAME + "(" +
            ID + " integer primary key autoincrement," +
            CURATION_ID + " integer," +
            ARTICLE_ID + " integer," +
            "foreign key(" + CURATION_ID + ") references " + Curation.TABLE_NAME + "(" + Curation.ID + ")," +
            "foreign key(" + ARTICLE_ID + ") references " + Article.TABLE_NAME + "(" + Article.ID + "))"
}
