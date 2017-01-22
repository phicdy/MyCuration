package com.phicdy.mycuration.rss;



public class Curation {
	private int id;
	private String name;

	public static final String TABLE_NAME = "curations";
	public static final String NAME = "name";
	public static final String ID = "_id";

	public static final int DEFAULT_CURATION_ID = -100;

	public static final String CREATE_TABLE_SQL =
			"create table " + TABLE_NAME + "(" +
					ID + " integer primary key autoincrement,"+
					NAME + " text)";

	public Curation() {
		id = DEFAULT_CURATION_ID;
		name = null;
	}

	public Curation(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
}
