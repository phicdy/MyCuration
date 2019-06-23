package com.phicdy.mycuration.domain.entity

object CurationCondition {
    const val TABLE_NAME = "curationConditions"
    const val CURATION_ID = "curationId"
    const val WORD = "word"
    private const val ID = "_id"

    const val CREATE_TABLE_SQL = "create table " + TABLE_NAME + "(" +
            ID + " integer primary key autoincrement," +
            WORD + " text," +
            CURATION_ID + " integer," +
            "foreign key(" + CurationSelection.CURATION_ID + ") references " + Curation.TABLE_NAME + "(" + Curation.ID + "))"
}
