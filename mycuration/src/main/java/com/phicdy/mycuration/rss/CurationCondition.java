package com.phicdy.mycuration.rss;

public class CurationCondition {
	public static final String TABLE_NAME = "curationConditions";
	public static final String CURATION_ID = "curationId";
	public static final String WORD = "word";
	public static final String ID = "_id";

	public static final String CREATE_TABLE_SQL =
			"create table " + TABLE_NAME + "(" +
					ID + " integer primary key autoincrement,"+
					WORD + " text," +
					CURATION_ID + " integer," +
					"foreign key(" + CurationSelection.CURATION_ID + ") references " + Curation.TABLE_NAME + "(" + Curation.ID + "))";
}
