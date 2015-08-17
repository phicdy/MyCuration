package com.phicdy.filfeed.rss;



public class Curation {
	private int id_;
	private String title_;
	private int unreadAriticlesCount_;

	public static final String TABLE_NAME = "curations";
	public static final String TITLE = "title";
	public static final String ID = "_id";
	public static final String UNREAD_ARTICLE = "unreadArticle";

	public static final int DEFAULT_CURATION_ID = -100;

	public Curation() {
		id_      = DEFAULT_CURATION_ID;
		title_   = null;
		unreadAriticlesCount_ = 0;
	}

	public Curation(int id, String title, int unreadAriticlesCount) {
		id_    = id;
		title_ = title;
		unreadAriticlesCount_ = unreadAriticlesCount;
	}
	
	public void setTitle(String title) {
		title_ = title;
	}
	
	public void setId(Integer id) {
		id_ = id;
	}
	
	public void setUnreadArticlesCount(int unreadArticlesCount) {
		unreadAriticlesCount_ = unreadArticlesCount;
	}
	
	public int getUnreadAriticlesCount() {
		return unreadAriticlesCount_;
	}
	
	public Integer getId() {
		return id_;
	}
	
	public String getTitle() {
		return title_;
	}
}
