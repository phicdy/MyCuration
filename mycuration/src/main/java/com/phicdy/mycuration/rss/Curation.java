package com.phicdy.mycuration.rss;



public class Curation {
	private final int id;
	private final String name;

	public static final String TABLE_NAME = "curations";
	public static final String NAME = "name";
	public static final String ID = "_id";

	public static final String CREATE_TABLE_SQL =
			"create table " + TABLE_NAME + "(" +
					ID + " integer primary key autoincrement,"+
					NAME + " text)";

	public Curation(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public Integer getId() {
		return id;
	}
	
	public String getName() {
		if (name == null) return "";
		return name;
	}
}
