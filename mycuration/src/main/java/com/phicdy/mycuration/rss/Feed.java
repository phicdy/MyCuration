package com.phicdy.mycuration.rss;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Feed implements Parcelable{
	private final int id_;
	private String title_;
	private String url_;
	private String iconPath;
	private String format;
	private int unreadAriticlesCount_;
	private String siteUrl;

	public static final String TABLE_NAME = "feeds";
	public static final String TITLE = "title";
	public static final String ID = "_id";
	public static final String URL = "url";
	private static final String FORMAT = "format";
	public static final String SITE_URL = "siteUrl";
	public static final String ICON_PATH = "iconPath";
	public static final String UNREAD_ARTICLE = "unreadArticle";

	public static final String DEDAULT_ICON_PATH = "defaultIconPath";
	public static final int ALL_FEED_ID = -1;
	public static final int DEFAULT_FEED_ID = -100;

	static final String RSS_1 = "RSS1.0";
	static final String RSS_2 = "RSS2.0";
	static final String ATOM = "ATOM";

	public static final String CREATE_TABLE_SQL =
			"create table " + TABLE_NAME + "(" +
					ID + " integer primary key autoincrement,"+
					TITLE + " text,"+
					URL + " text,"+
					FORMAT + " text," +
					SITE_URL + " text," +
					ICON_PATH + " text," +
					UNREAD_ARTICLE + " integer)";

	public Feed() {
		id_      = DEFAULT_FEED_ID;
		title_   = null;
		url_     = null;
		iconPath = DEDAULT_ICON_PATH;
		siteUrl  = null;
		unreadAriticlesCount_ = 0;
	}

	public Feed(int id,String title,String url, String iconPath, String siteUrl, int unreadAriticlesCount) {
		id_    = id;
		title_ = title;
		url_   = url;
		this.iconPath = iconPath;
		this.siteUrl = siteUrl;
		unreadAriticlesCount_ = unreadAriticlesCount;
	}

	public Feed(int feedId, String feedTitle) {
		id_    = feedId;
		title_ = feedTitle;
	}

    public Feed(@NonNull String title, @NonNull String baseUrl, @NonNull String format, @NonNull String siteUrl) {
        id_ = -1;
		title_ = title;
        url_ = baseUrl;
		this.format = format;
		this.siteUrl = siteUrl;
    }

    public String getSiteUrl() {
		return siteUrl;
	}

	public void setTitle(String title) {
		title_ = title;
	}
	
	public int getUnreadAriticlesCount() {
		return unreadAriticlesCount_;
	}

    public void setUnreadAriticlesCount(int unreadAriticlesCount) {
        unreadAriticlesCount_ = unreadAriticlesCount;
    }

    public int getId() {
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

	public String getFormat() {
        return format;
    }

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id_);
		dest.writeString(title_);
		dest.writeString(url_);
		dest.writeString(iconPath);
		dest.writeString(siteUrl);
		dest.writeInt(unreadAriticlesCount_);
	}

	public static final Parcelable.Creator<Feed> CREATOR
			= new Parcelable.Creator<Feed>() {
		public Feed createFromParcel(Parcel in) {
			return new Feed(in);
		}

		public Feed[] newArray(int size) {
			return new Feed[size];
		}
	};

	private Feed(Parcel in) {
		id_      = in.readInt();
		title_   = in.readString();
		url_     = in.readString();
		iconPath = in.readString();
		siteUrl  = in.readString();
		unreadAriticlesCount_ = in.readInt();
	}
}
