package com.phicdy.mycuration.filter;


import com.phicdy.mycuration.rss.Feed;

import java.util.ArrayList;

public class Filter {
	private int id_;
	private String title_;
	private String keyword_;
	private String url_;
	private ArrayList<Feed> feeds;
    private int feedId;
	private String feedTitle;
	private int enabled;

	public static final String TABLE_NAME = "filters";
	public static final String ID = "_id";
	@Deprecated
	public static final String FEED_ID = "feedId";
	public static final String KEYWORD = "keyword";
	public static final String URL = "url";
	public static final String TITLE = "title";
	public static final String ENABLED = "enabled";

	public static final int TRUE = 1;
	public static final int FALSE = 0;

	public Filter(int id, String title, String keyword, String url,
				  ArrayList<Feed> feeds, int enabled) {
		id_      = id;
		title_   = title;
		keyword_ = keyword;
		url_     = url;
		StringBuilder buf = new StringBuilder();
		for (Feed feed : feeds) {
			buf.append(feed.getTitle());
            buf.append(", ");
		}
        // delete last ", "
        buf.deleteCharAt(buf.length() - 2);
        this.feedTitle = buf.toString();
		this.feeds = feeds;
		this.enabled = enabled;
	}

    public Filter(int id, String title, String keyword, String url, int enabled) {
        id_      = id;
        title_   = title;
        keyword_ = keyword;
        url_     = url;
        this.enabled = enabled;
    }

    public Filter(int filterId, String title, String keyword, String url, int feedId, int enabled) {
        id_      = filterId;
        title_   = title;
        keyword_ = keyword;
        url_     = url;
        this.feedId = feedId;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
		return enabled == TRUE;
	}

	public void setEnabled(boolean enabled) {
		if (enabled) {
			this.enabled = TRUE;
		}else {
			this.enabled = FALSE;
		}
	}

	public ArrayList<Feed> feeds() {
		return feeds;
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

    public int getFeedId() {
        return feedId;
    }
}
