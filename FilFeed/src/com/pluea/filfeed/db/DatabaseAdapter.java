package com.pluea.filfeed.db;

import java.util.ArrayList;

import com.pluea.filfeed.filter.Filter;
import com.pluea.filfeed.rss.Article;
import com.pluea.filfeed.rss.Feed;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DatabaseAdapter {
	
	private Context context;
	private static DatabaseHelper dbHelper;
	private static DatabaseAdapter sharedDbAdapter;
	private static SQLiteDatabase db;
	private static final String LOG_TAG = "RSSReader."
			+ DatabaseAdapter.class.getName();

	private DatabaseAdapter(Context context) {
		this.context = context;
		dbHelper = new DatabaseHelper(this.context);
		if (db == null) {
			db = dbHelper.getWritableDatabase();
		}
	}
	
	public static DatabaseAdapter getInstance(Context context) {
		if (sharedDbAdapter == null) {
			synchronized (DatabaseAdapter.class) {
				if (sharedDbAdapter == null) {
					sharedDbAdapter = new DatabaseAdapter(context);
				}
			}
		}
		return sharedDbAdapter;
	}

	public void saveNewArticles(ArrayList<Article> articles, int feedId) {
		if(articles.isEmpty()) {
			return;
		}
		db.beginTransaction();
		try {
			// db.delete("articles", "title like '%PR%'", null);
			SQLiteStatement insertSt = db
					.compileStatement("insert into articles(title,url,status,point,date,feedId) values (?,?,?,?,?,?);");
			// articles passed isArticle(), so not need to check same article exist
			for (Article article : articles) {
				insertSt.bindString(1, article.getTitle());
				insertSt.bindString(2, article.getUrl());
				insertSt.bindString(3, "unread");
				insertSt.bindString(4, Feed.DEDAULT_HATENA_POINT);
				Log.d(LOG_TAG, "insert date:" + article.getPostedDate());
				insertSt.bindLong(5, article.getPostedDate());
				insertSt.bindString(6, String.valueOf(feedId));

				insertSt.executeInsert();
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
	}

	public int calcNumOfUnreadArticles(int feedId) {
		int unreadArticlesCount = 0;
		db.beginTransaction();
		try {
			String getArticlesCountsql = "select _id from articles where status = \"unread\" and feedId = "
					+ feedId;
			Cursor countCursor = db.rawQuery(getArticlesCountsql, null);
			unreadArticlesCount = countCursor.getCount();
			countCursor.close();
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		return unreadArticlesCount;
	}
	
	public int calcNumOfArticles(int feedId) {
		int unreadArticlesCount = 0;
		db.beginTransaction();
		try {
			String getArticlesCountsql = "select _id from articles where feedId = "
					+ feedId;
			Cursor countCursor = db.rawQuery(getArticlesCountsql, null);
			unreadArticlesCount = countCursor.getCount();
			countCursor.close();
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		return unreadArticlesCount;
	}
	
	public int calcNumOfArticles() {
		int unreadArticlesCount = 0;
		db.beginTransaction();
		try {
			String getArticlesCountsql = "select _id from articles";
			Cursor countCursor = db.rawQuery(getArticlesCountsql, null);
			unreadArticlesCount = countCursor.getCount();
			countCursor.close();
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		return unreadArticlesCount;
	}

	public ArrayList<Feed> getAllFeedsThatHaveUnreadArticles() {
		ArrayList<Feed> feedList = new ArrayList<Feed>();
		db.beginTransaction();
		try {
			String sql = "select feeds._id,feeds.title,feeds.url,feeds.iconPath,feeds.siteUrl,count(articles._id) " +
					"from feeds inner join articles " +
					"where feeds._id = articles.feedId and articles.status = \"unread\"" +
					"group by feeds.title " + 
					"order by feeds.title";
			Cursor cursor = db.rawQuery(sql, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int id = cursor.getInt(0);
					String title = cursor.getString(1);
					String url = cursor.getString(2);
					String iconPath = cursor.getString(3);
					String siteUrl = cursor.getString(4);
					int unreadAriticlesCount = cursor.getInt(5);
					feedList.add(new Feed(id, title, url, iconPath, siteUrl, unreadAriticlesCount));
				}
				cursor.close();
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		if (feedList.size() == 0) {
			feedList = getAllFeedsWithoutNumOfUnreadArticles();
		}
		return feedList;
	}
	
	public ArrayList<Feed> getAllFeedsWithNumOfUnreadArticles() {
		ArrayList<Feed> feedList = new ArrayList<Feed>();
		db.beginTransaction();
		try {
			String sql = "select feeds._id,feeds.title,feeds.url,feeds.iconPath,feeds.siteUrl,count(articles._id) " +
					"from feeds inner join articles " +
					"where feeds._id = articles.feedId" +
					"group by feeds.title " + 
					"order by feeds.title";
			Cursor cursor = db.rawQuery(sql, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int id = cursor.getInt(0);
					String title = cursor.getString(1);
					String url = cursor.getString(2);
					String iconPath = cursor.getString(3);
					String siteUrl = cursor.getString(4);
					int unreadAriticlesCount = cursor.getInt(5);
					feedList.add(new Feed(id, title, url, iconPath, siteUrl, unreadAriticlesCount));
				}
				cursor.close();
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		if (feedList.size() == 0) {
			feedList = getAllFeedsWithoutNumOfUnreadArticles();
		}
		return feedList;
	}
	
	public ArrayList<Feed> getAllFeedsWithoutNumOfUnreadArticles() {
		ArrayList<Feed> feedList = new ArrayList<Feed>();
		String[] columns = {"_id","title","url","iconPath","siteUrl"};
		String orderBy = "title";
		db.beginTransaction();
		try {
			Cursor cursor = db.query("feeds", columns, null, null, null, null, orderBy);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int id = cursor.getInt(0);
					String title = cursor.getString(1);
					String url = cursor.getString(2);
					String iconPath = cursor.getString(3);
					String siteUrl = cursor.getString(4);
					feedList.add(new Feed(id, title, url, iconPath, siteUrl, 0));
				}
				cursor.close();
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		return feedList;
	}

	public void saveStatusBeforeUpdate(int articleId) {
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put("status", "toRead");
			db.update("articles", values, "_id = " + articleId, null);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	
	public void saveStatusToRead(int feedId) {
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put("status", Article.READ);
			String whereClause = "feedId = " + feedId;
			db.update("articles", values, whereClause, null);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
	}
	
	public void saveAllStatusToRead() {
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put("status", Article.READ);
			db.update("articles", values, null, null);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
	}
	
	public void saveStatus(int articleId, String status) {
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put("status", status);
			db.update("articles", values, "_id = " + articleId, null);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
	}
	
	public void saveIconPath(String siteUrl, String iconPath) {
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put("iconPath", iconPath);
			db.update("feeds", values, "siteUrl = '" + siteUrl + "'", null);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
	}
	
	public void saveHatenaPoint(int articleId, String point) {
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put("point", point);
			db.update("articles", values, "_id = " + articleId, null);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	
	public int saveNewTitle(int feedId, String newTitle) {
		int numOfUpdated = 0;
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put("title", newTitle);
			numOfUpdated = db.update("feeds", values, "_id = " + feedId, null);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return numOfUpdated;
	}

	public String getStatus(int articleId) {
		String status = null;
		db.beginTransaction();
		try {
			String sql = "select status from articles where _id = " + articleId;
			Cursor cur = db.rawQuery(sql, null);
			cur.moveToNext();
			status = cur.getString(0);
			cur.close();
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		return status;
	}

	public boolean deleteFeed(int feedId) {
		int numOfDeleted = 0;
		db.beginTransaction();
		try {
			db.delete("articles", "feedId = " + feedId, null);
			db.delete("filters", "feedId = " + feedId, null);
			// db.delete("priorities","feedId = "+feedId,null);
			numOfDeleted = db.delete("feeds", "_id = " + feedId, null);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		if (numOfDeleted == 1) {
			return true;
		}
		return false;
	}

	public Feed getFeedByUrl(String feedUrl) {
		Feed feed = null;
		db.beginTransaction();
		try {
			// Get feed
			String getFeedSql = "select _id,title,iconPath,siteUrl from feeds where url = \""
					+ feedUrl + "\"";
			Cursor cur = db.rawQuery(getFeedSql, null);
			if (cur.getCount() != 0) {
				cur.moveToNext();
				int feedId = cur.getInt(0);
				String feedTitle = cur.getString(1);
				String iconPath = cur.getString(2);
				String siteUrl = cur.getString(3);

				feed = new Feed(feedId, feedTitle, feedUrl, iconPath, siteUrl, 0);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return feed;
	}

	public Feed saveNewFeed(String feedTitle, String feedUrl, String format, String siteUrl) {
		boolean sameFeedExist = false;
		db.beginTransaction();
		try {
			// Get same feeds from DB
			String sql = "select _id from feeds " + "where title=\""
					+ feedTitle + "\" and url=\"" + feedUrl
					+ "\" and format=\"" + format + "\"";
			Cursor cursor = db.rawQuery(sql, null);

			// If there aren't same feeds in DB,Insert into DB
			if (cursor.getCount() == 0) {
				ContentValues values = new ContentValues();
				values.put("title", feedTitle);
				values.put("url", feedUrl);
				values.put("format", format);
				values.put("iconPath", Feed.DEDAULT_ICON_PATH);
				values.put("siteUrl", siteUrl);
				if (db.insert("feeds", null, values) == -1) {
					Log.v("insert error", "error occurred");
				}
			} else {
				// Same feed already exists
				sameFeedExist = true;
			}
			cursor.close();
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		if (sameFeedExist) {
			return null;
		}
		return getFeedByUrl(feedUrl);
	}

	public void changeArticlesStatusToRead() {
		db.beginTransaction();
		try {
			// Update articles read status in the "readstatus" to DB
			ContentValues value = new ContentValues();
			value.put("status", Article.READ);
			db.update("articles", value, "status = \"" + Article.TOREAD + "\"", null);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	public int getNumOfUnreadArtilces(int feedId) {
		int num = 0;
		db.beginTransaction();
		try {
			// Get unread articles and set num of unread articles
			String sql = "select _id from articles where status = \"unread\" and feedId = "
					+ feedId;
			Cursor cursor = db.rawQuery(sql, null);
			num = cursor.getCount();
			cursor.close();
			db.setTransactionSuccessful();
		} catch (Exception e) {
			num = -1;
		} finally {
			db.endTransaction();
		}
		
		return num;
	}

	public ArrayList<Article> getAllUnreadArticles(boolean isNewestArticleTop) {
		ArrayList<Article> articles = new ArrayList<Article>();
		db.beginTransaction();
		try {
			// Get unread articles
			String sql = "select articles._id,articles.title,articles.url,articles.point,articles.date,articles.feedId,feeds.title " +
					"from articles inner join feeds " +
					"where articles.status = \"unread\" and articles.feedId = feeds._id " +
					"order by date ";
			if(isNewestArticleTop) {
				sql += "desc";
			}else {
				sql += "asc";
			}
			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {
				int id = cursor.getInt(0);
				String title = cursor.getString(1);
				String url = cursor.getString(2);
				String status = Article.UNREAD;
				String point = cursor.getString(3);
				long dateLong = cursor.getLong(4);
				int feedId = cursor.getInt(5);
				String feedTitle = cursor.getString(6);
				Article article = new Article(id, title, url, status, point,
						dateLong, feedId, feedTitle);
				articles.add(article);
			}
			cursor.close();
			db.setTransactionSuccessful();
		} catch (Exception e) {
			
			return articles;
		} finally {
			db.endTransaction();
		}
		
		return articles;
	}
	
	public ArrayList<Article> getAllArticles(boolean isNewestArticleTop) {
		ArrayList<Article> articles = new ArrayList<Article>();
		db.beginTransaction();
		try {
			// Get unread articles
			String sql = "select articles._id,articles.title,articles.url,articles.status,articles.point,articles.date,articles.feedId,feeds.title " +
					"from articles inner join feeds " +
					"where articles.feedId = feeds._id " +
					"order by date ";
			if(isNewestArticleTop) {
				sql += "desc";
			}else {
				sql += "asc";
			}
			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {
				int id = cursor.getInt(0);
				String title = cursor.getString(1);
				String url = cursor.getString(2);
				String status = cursor.getString(3);
				String point = cursor.getString(4);
				long dateLong = cursor.getLong(5);
				int feedId = cursor.getInt(6);
				String feedTitle = cursor.getString(7);
				Article article = new Article(id, title, url, status, point,
						dateLong, feedId, feedTitle);
				articles.add(article);
			}
			cursor.close();
			db.setTransactionSuccessful();
		} catch (Exception e) {
			
			return articles;
		} finally {
			db.endTransaction();
		}
		
		return articles;
	}
	
	public ArrayList<Article> searchArticles(String keyword, int feedId, boolean isNewestArticleTop) {
		ArrayList<Article> articles = new ArrayList<Article>();
		db.beginTransaction();
		try {
			if(keyword.contains("%")) {
				keyword.replace("%", "$%");
			}
			if(keyword.contains("_")) {
				keyword.replace("_", "$_");
			}
			String sql = "select articles._id,articles.title,articles.url,articles.status,articles.point,articles.date,feeds.title " +
					"from articles inner join feeds " +
					"where articles.title like '%" + keyword + "%' escape '$' and " +
					"articles.feedId = " + feedId + " and articles.feedId = feeds._id " +
					" order by date";
			if(isNewestArticleTop) {
				sql += " desc";
			}else {
				sql += " asc";
			}
			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {
				int id = cursor.getInt(0);
				String title = cursor.getString(1);
				String url = cursor.getString(2);
				String status = cursor.getString(3);
				String point = cursor.getString(4);
				long dateLong = cursor.getLong(5);
				String feedTitle = cursor.getString(7);
				Article article = new Article(id, title, url, status, point,
						dateLong, feedId, feedTitle);
				articles.add(article);
			}
			cursor.close();
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		
		return articles;
	}
	
	public ArrayList<Article> getUnreadArticlesInAFeed(int feedId, boolean isNewestArticleTop) {
		ArrayList<Article> articles = new ArrayList<Article>();
		db.beginTransaction();
		try {
			// Get unread articles
			String sql = "select _id,title,url,point,date from articles where status = \"unread\" and feedId = "
					+ feedId + " order by date ";
			if(isNewestArticleTop) {
				sql += "desc";
			}else {
				sql += "asc";
			}
			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {
				int id = cursor.getInt(0);
				String title = cursor.getString(1);
				String url = cursor.getString(2);
				String status = "unread";
				String point = cursor.getString(3);
				long dateLong = cursor.getLong(4);
				Article article = new Article(id, title, url, status, point,
						dateLong, feedId, null);
				articles.add(article);
			}
			cursor.close();
			db.setTransactionSuccessful();
		} catch (Exception e) {
			
			return articles;
		} finally {
			db.endTransaction();
		}
		
		return articles;
	}
	
	public ArrayList<Article> getAllArticlesInAFeed(int feedId, boolean isNewestArticleTop) {
		ArrayList<Article> articles = new ArrayList<Article>();
		db.beginTransaction();
		try {
			// Get unread articles
			String sql = "select _id,title,url,status,point,date from articles where feedId = "
					+ feedId + " order by date ";
			if(isNewestArticleTop) {
				sql += "desc";
			}else {
				sql += "asc";
			}
			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {
				int id = cursor.getInt(0);
				String title = cursor.getString(1);
				String url = cursor.getString(2);
				String status = cursor.getString(3);
				String point = cursor.getString(4);
				long dateLong = cursor.getLong(5);
				Article article = new Article(id, title, url, status, point,
						dateLong, feedId, null);
				articles.add(article);
			}
			cursor.close();
			db.setTransactionSuccessful();
		} catch (Exception e) {
			
			return articles;
		} finally {
			db.endTransaction();
		}
		
		return articles;
	}

	public ArrayList<Filter> getFiltersOfFeed(int feedId) {
		ArrayList<Filter> filterList = new ArrayList<Filter>();
		db.beginTransaction();
		try {
			// Get all filters which feed ID is "feedId"
			String[] columns = { "_id", "title", "keyword", "url" };
			String condition = "feedId = " + feedId;
			Cursor cur = db.query("filters", columns, condition, null, null,
					null, null);
			// Change to ArrayList
			while (cur.moveToNext()) {
				int id = cur.getInt(0);
				String title = cur.getString(1);
				String keyword = cur.getString(2);
				String url = cur.getString(3);
				filterList.add(new Filter(id, title, keyword, url, feedId));
			}
			cur.close();
			db.setTransactionSuccessful();
		} catch (Exception e) {
			return null;
		} finally {
			db.endTransaction();
		}
		
		return filterList;
	}

	public boolean applyFiltersOfFeed(ArrayList<Filter> filterList, int feedId) {
		// If articles are hit in condition, Set articles status to "read"
		ContentValues value = new ContentValues();
		value.put("status", "read");
		for (Filter filter : filterList) {
			db.beginTransaction();
			try {
				// Initialize condition
				String condition = "feedId = " + feedId;

				// If keyword or url exists, add condition
				String keyword = filter.getKeyword();
				String url = filter.getUrl();
				if (keyword.equals("") && url.equals("")) {
					Log.w("Set filtering conditon",
							"keyword and url don't exist fileter ID ="
									+ filter.getId());
					continue;
				}
				if (!keyword.equals("")) {
					condition = condition + " and title like '%" + keyword
							+ "%'";
				}
				if (!url.equals("")) {
					condition = condition + " and url like '%" + url + "%'";
				}
				db.update("articles", value, condition, null);
				db.setTransactionSuccessful();
			} catch (Exception e) {
				Log.e("Apply Filtering", "Article can't be updated.Feed ID = "
						+ feedId);
				
				return false;
			} finally {
				db.endTransaction();
			}
			
		}
		return true;
	}

	public void deleteFilter(int filterId) {
		db.beginTransaction();
		try {
			db.delete("filters", "_id = " + filterId, null);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public int getNumOfFeeds() {
		int num = 0;
		db.beginTransaction();
		try {
			// Get unread feeds and set num of unread feeds
			String sql = "select _id from feeds";
			Cursor cursor = db.rawQuery(sql, null);
			num = cursor.getCount();
			cursor.close();
			db.setTransactionSuccessful();
		} catch (Exception e) {
			num = -1;
		} finally {
			db.endTransaction();
		}
		
		return num;
	}

	public void saveNewFilter(String title, int selectedFeedId, String keyword,
			String filterUrl) {
		db.beginTransaction();
		try {
			// Check same fileter exists in DB
			String getSameFilterSql = "select _id from filters "
					+ "where title='" + title + "' and " + "feedId = "
					+ selectedFeedId + " and " + "keyword = '" + keyword
					+ "' and " + "url = '" + filterUrl + "'";
			Cursor cur = db.rawQuery(getSameFilterSql, null);
			if (cur.getCount() != 0) {
				Log.i("Register Filter", "Same Filter Exist");
			} else {
				// Register filter
				ContentValues values = new ContentValues();
				values.put("title", title);
				values.put("url", filterUrl);
				values.put("keyword", keyword);
				values.put("feedId", selectedFeedId);
				db.insert("filters", null, values);
				db.setTransactionSuccessful();
			}
		} catch (Exception e) {
			Log.e("insert error", "error occurred");
		} finally {
			db.endTransaction();
		}
		
	}

	public static String sanitizing(String str) {

		if (str == null || "".equals(str)) {
			return str;
		}
		str = str.replaceAll("'", "''");
		str = str.replaceAll("%", "\\%");
		return str;
	}

	public void addManyFeeds() {
		ArrayList<Feed> feeds = new ArrayList<Feed>();
		//RSS 2.0
//		feeds.add(new Feed(0, "スポーツナビ - ピックアップ　ゲーム",
//				"http://sports.yahoo.co.jp/rss/pickup_game/pc"));
//		feeds.add(new Feed(0, "Yahoo!ニュース・トピックス - トップ",
//				"http://rss.dailynews.yahoo.co.jp/fc/rss.xml"));
//		feeds.add(new Feed(0, "Yahoo!ニュース・トピックス - 海外",
//				"http://rss.dailynews.yahoo.co.jp/fc/world/rss.xml"));
//		feeds.add(new Feed(0, "Yahoo!ニュース・トピックス - 経済",
//				"http://rss.dailynews.yahoo.co.jp/fc/economy/rss.xml"));
//		feeds.add(new Feed(0, "Yahoo!ニュース・トピックス - エンターテインメント",
//				"http://rss.dailynews.yahoo.co.jp/fc/entertainment/rss.xml"));
		feeds.add(new Feed(0, "IT速報",
				"http://blog.livedoor.jp/itsoku/index.rdf","", "http://blog.livedoor.jp/itsoku/", 0));
		feeds.add(new Feed(0, "あじゃじゃしたー",
				"http://blog.livedoor.jp/chihhylove/index.rdf","", "http://blog.livedoor.jp/chihhylove/", 0));
		feeds.add(new Feed(0, "はてなブログ人気エントリー",
				"http://b.hatena.ne.jp/hotentry.rss","", "http://b.hatena.ne.jp", 0));
		feeds.add(new Feed(0, "はてなブックマーク - 人気エントリー - テクノロジー",
				"http://b.hatena.ne.jp/hotentry/it.rss","", "http://b.hatena.ne.jp/hotentry", 0));
		feeds.add(new Feed(0, "暇人速報",
				"http://himasoku.com/index.rdf","", "http://himasoku.com", 0));
		feeds.add(new Feed(0, "ドメサカブログ",
				"http://blog.livedoor.jp/domesoccer/index.rdf","", "http://blog.livedoor.jp/domesoccer/", 0));
		feeds.add(new Feed(0, "きんどう",
				"http://kindou.info/feed","", "http://kindou.info", 0));
		feeds.add(new Feed(0, "GGSOKU - ガジェット速報",
				"http://ggsoku.com/feed","", "http://ggsoku.com", 0));
		feeds.add(new Feed(0, "Act as Professional",
				"http://hiroki.jp/feed/","", "http://hiroki.jp", 0));
		feeds.add(new Feed(0, "Developers.IO",
				"http://dev.classmethod.jp/feed/","", "http://dev.classmethod.jp", 0));
		feeds.add(new Feed(0, "GREE Engineers' Blog",
				"http://labs.gree.jp/blog/feed","", "http://labs.gree.jp/blog", 0));
		feeds.add(new Feed(0, "HTC速報",
				"http://htcsoku.info/feed/","", "http://htcsoku.info", 0));
		feeds.add(new Feed(0, "Hatena Developer Blog",
				"http://developer.hatenastaff.com/rss","", "http://developer.hatenastaff.com/", 0));
		feeds.add(new Feed(0, "ITmedia 総合記事一覧",
				"http://rss.rssad.jp/rss/itmtop/2.0/itmedia_all.xml","", "http://www.itmedia.co.jp/", 0));
		feeds.add(new Feed(0, "Publickey",
				"http://www.publickey1.jp/atom.xml","", "http://www.publickey1.jp/", 0));
		feeds.add(new Feed(0, "Tech Booster",
				"http://techbooster.jpn.org/feed/","", "http://techbooster.jpn.org", 0));
		feeds.add(new Feed(0, "TechCrunch Japan",
				"http://jp.techcrunch.com/feed/","", "http://jp.techcrunch.com", 0));
		feeds.add(new Feed(0, "あんどろいど速報",
				"http://androidken.blog119.fc2.com/?xml","", "http://androidken.blog119.fc2.com/", 0));
		feeds.add(new Feed(0, "＠IT 全フォーラム 最新記事一覧",
				"http://www.atmarkit.co.jp/","", "http://rss.rssad.jp/rss/itmatmarkit/rss.xml", 0));
		//atom
//		feeds.add(new Feed(0, "TweetBuzz - 注目エントリー",
//				"http://feeds.feedburner.com/tb-hotentry"));
		//RDF
//		feeds.add(new Feed(0, "二十歳街道まっしぐら",
//				"http://20kaido.com/index.rdf"));
		
		db.beginTransaction();
		try {

			// If there aren't same feeds in DB,Insert into DB
			for (Feed feed : feeds) {
				saveNewFeed(feed.getTitle(), feed.getUrl(), "RSS2.0", feed.getSiteUrl());
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public boolean isArticle(Article article) {
		int num = 0;
		db.beginTransaction();
		try {
			// Get same article
			String sql = "select _id from articles where url = '"
					+ article.getUrl() + "';";
			
			Cursor cursor = db.rawQuery(sql, null);
			num = cursor.getCount();
			cursor.close();
			db.setTransactionSuccessful();
		} catch (Exception e) {
			num = -1;
		} finally {
			db.endTransaction();
		}
		
		if(num > 0) {
			return true;
		}
		return false;
	}
	
	public void deleteAllArticles() {
		db.beginTransaction();
		try {
			db.delete("articles", "", null);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
}
