package com.example.rssfilterreader;



public class Feed {
	private int id_;
	private String title_;
	private String url_;
	private int unreadAriticlesCount_;
	
	public Feed(int id,String title,String url) {
		id_    = id;
		title_ = title;
		url_   = url;
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
	
}
