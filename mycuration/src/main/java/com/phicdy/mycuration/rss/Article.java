package com.phicdy.mycuration.rss;


public class Article {
	private final int id_;
	private String title_;
	private String url_;
	private String status_;
	private final String point_;
	private long postedDate_;
	private final int feedId_;
	private final String feedTitle;
	private final String feedIconPath;

	public static final String TABLE_NAME = "articles";
	public static final String ID = "_id";
	public static final String TITLE = "title";
	public static final String URL = "url";
	public static final String STATUS = "status";
	public static final String POINT = "point";
	public static final String DATE = "date";
	public static final String FEEDID = "feedId";

	public static final String UNREAD = "unread";
	public static final String TOREAD = "toRead";
	public static final String READ = "read";

	public static final String DEDAULT_HATENA_POINT = "-1";

	public static final String CREATE_TABLE_SQL =
			"create table " + TABLE_NAME + "(" +
					ID + " integer primary key autoincrement,"+
					TITLE + " text,"+
					URL + " text,"+
					STATUS + " text default "+ UNREAD+","+
					POINT + " text,"+
					DATE + " text,"+
					FEEDID + " integer,"+
					"foreign key(" + FEEDID + ") references " + Feed.TABLE_NAME + "(" + Feed.ID + "))";

	public Article(int id, String title, String url, String status,
			String point, long postDate, int feedId, String feedTitle, String feedIconPath) {
		id_    = id;
		title_ = title;
		status_ = status;
		url_   = url;
		point_ = point;
		postedDate_ = postDate;
		feedId_ = feedId;
		this.feedTitle = feedTitle;
		this.feedIconPath = feedIconPath;
	}
	
	public String getFeedTitle() {
		return feedTitle;
	}

	public void setTitle(String title) {
		title_ = title;
	}
	
	public void setUrl(String url) {
		url_ = url;
	}
	
	public void setPostedDate(long postedDate) {
		postedDate_ = postedDate;
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

	public String getFeedIconPath() {
		return feedIconPath;
	}
}
