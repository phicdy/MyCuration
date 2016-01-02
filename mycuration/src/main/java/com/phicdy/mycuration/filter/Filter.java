package com.phicdy.mycuration.filter;


public class Filter {
	private int id_;
	private String title_;
	private String keyword_;
	private String url_;
	private int feedId_;
	private String feedTitle;
	private int enabled;

	public static final String TABLE_NAME = "filters";
	public static final String ID = "_id";
	public static final String FEED_ID = "feedId";
	public static final String KEYWORD = "keyword";
	public static final String URL = "url";
	public static final String TITLE = "title";
	public static final String ENABLED = "enabled";

	private static final int TRUE = 1;
	private static final int FALSE = 0;

	public Filter(int id,String title,String keyword,String url,int feedId,String feedTitle, int enabled) {
		id_      = id;
		title_   = title;
		keyword_ = keyword;
		url_     = url;
		feedId_  = feedId;
		this.feedTitle = feedTitle;
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled == TRUE ? true : false;
	}

	public void setEnabled(boolean enabled) {
		if (enabled) {
			this.enabled = TRUE;
		}else {
			this.enabled = FALSE;
		}
	}

	public int getFeedId() {
		return feedId_;
	}

	public void setFeedId(int feedId_) {
		this.feedId_ = feedId_;
	}

	public void setTitle(String title) {
		title_ = title;
	}
	
	public void setId(Integer id) {
		id_ = id;
	}
	
	public Integer getId() {
		return id_;
	}
	
	public String getTitle() {
		return title_;
	}
	
	public String getKeyword() {
		return keyword_;
	}
	
	public String getUrl() {
		return url_;
	}

	public String getFeedTitle() {
		return feedTitle;
	}

	public void setFeedTitle(String feedTitle) {
		this.feedTitle = feedTitle;
	}
}
