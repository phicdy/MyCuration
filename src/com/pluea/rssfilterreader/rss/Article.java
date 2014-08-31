package com.pluea.rssfilterreader.rss;


public class Article {
	private int id_;
	private String title_;
	private String url_;
	private String status_;
	private String point_;
	private long postedDate_;
	private int feedId_;
	private int arrayIndex_;
	
	public static final String UNREAD = "unread";
	public static final String TOREAD = "toRead";
	public static final String READ = "read";
	
	
	public Article(int id, String title, String url, String status, 
			String point, long postDate, int feedId) {
		id_    = id;
		title_ = title;
		status_ = status;
		url_   = url;
		point_ = point;
		postedDate_ = postDate;
		feedId_ = feedId;
	}
	
	public void setTitle(String title) {
		title_ = title;
	}
	
	public void setUrl(String url) {
		url_ = url;
	}
	
	public void setPoint(String point) {
		point_ = point;
	}
	
	public void setPostedDate(long postedDate) {
		postedDate_ = postedDate;
	}
	
	public void setId(int id) {
		id_ = id;
	}
	
	public void setFeedId(int feedId) {
		feedId_ = feedId;
	}
	
	public void setStatus(String status) {
		status_ = status;
	}
	
	public String getStatus() {
		return status_;
	}
	
	public Integer getFeedId() {
		return feedId_;
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
	
	public String getPoint() {
		return point_;
	}
	
	public long getPostedDate() {
		return postedDate_;
	}
	
	public int getArrayIndex() {
		return arrayIndex_;
	}

	public void setArrayIndex(int arrayIndex_) {
		this.arrayIndex_ = arrayIndex_;
	}

}
