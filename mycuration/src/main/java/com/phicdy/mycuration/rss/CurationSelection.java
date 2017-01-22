package com.phicdy.mycuration.rss;

public class CurationSelection {
	public static final String TABLE_NAME = "curationSelections";
	public static final String CURATION_ID = "curationId";
	public static final String ARTICLE_ID = "articleId";
	public static final String ID = "_id";

	public static final String CREATE_TABLE_SQL =
			"create table " + TABLE_NAME + "(" +
					ID + " integer primary key autoincrement,"+
					CURATION_ID + " integer," +
					ARTICLE_ID + " integer," +
					"foreign key(" + CURATION_ID + ") references " + Curation.TABLE_NAME + "(" + Curation.ID + ")," +
					"foreign key(" + ARTICLE_ID + ") references " + Article.TABLE_NAME + "(" + Article.ID + "))";
}
