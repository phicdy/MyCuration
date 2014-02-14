package com.example.rssfilterreader;


public class Filter {
	private int id_;
	private String title_;
	private String keyword_;
	private String url_;
	private int feedId_;
	
	public Filter(int id,String title,String keyword,String url,int feedId) {
		id_      = id;
		title_   = title;
		keyword_ = keyword;
		url_     = url;
		feedId_  = feedId;
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
}
