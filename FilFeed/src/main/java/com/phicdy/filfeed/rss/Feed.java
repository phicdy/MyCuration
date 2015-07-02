package com.phicdy.filfeed.rss;



public class Feed {
	private int id_;
	private String title_;
	private String url_;
	private String iconPath;
	private int unreadAriticlesCount_;
	private String siteUrl;

	public static final String TABLE_NAME = "feeds";
	public static final String TITLE = "title";
	public static final String ID = "_id";
	public static final String URL = "url";
	public static final String FORMAT = "format";
	public static final String SITE_URL = "siteUrl text";
	public static final String ICON_PATH = "iconPath";
	public static final String UNREAD_ARTICLE = "unreadArticle";

	public static final String DEDAULT_ICON_PATH = "defaultIconPath";
	public static final int ALL_FEED_ID = -1;

	public Feed(int id,String title,String url, String iconPath, String siteUrl, int unreadAriticlesCount) {
		id_    = id;
		title_ = title;
		url_   = url;
		this.iconPath = iconPath;
		this.siteUrl = siteUrl;
		unreadAriticlesCount_ = unreadAriticlesCount;
	}
	
	public String getSiteUrl() {
		return siteUrl;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}

	public void setTitle(String title) {
		title_ = title;
	}
	
	public void setUrl(String url) {
		url_ = url;
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
	
	public String getUrl() {
		return url_;
	}

	public String getIconPath() {
		return iconPath;
	}
	
	
}
