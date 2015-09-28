package com.phicdy.mycuration.filter;


public class Filter {
	private int id_;
	private String title_;
	private String keyword_;
	private String url_;
	private int feedId_;
	private String feedTitle;
	
	public Filter(int id,String title,String keyword,String url,int feedId,String feedTitle) {
		id_      = id;
		title_   = title;
		keyword_ = keyword;
		url_     = url;
		feedId_  = feedId;
		this.feedTitle = feedTitle;
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
