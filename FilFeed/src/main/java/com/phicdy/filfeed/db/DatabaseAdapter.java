package com.phicdy.filfeed.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.util.Log;

import com.phicdy.filfeed.filter.Filter;
import com.phicdy.filfeed.rss.Article;
import com.phicdy.filfeed.rss.Curation;
import com.phicdy.filfeed.rss.CurationCondition;
import com.phicdy.filfeed.rss.CurationSelection;
import com.phicdy.filfeed.rss.Feed;
import com.phicdy.filfeed.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DatabaseAdapter {
	
	private Context context;
	private static DatabaseHelper dbHelper;
	private static DatabaseAdapter sharedDbAdapter;
	private static SQLiteDatabase db;

	private static final String BACKUP_FOLDER = "filfeed_backup";
	public static final int NOT_FOUND_ID = -1;

	private static final String LOG_TAG = "FilFeed."
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
		Map<Integer, ArrayList<String>> curationWordMap = getAllCurationWords();
		SQLiteStatement insertArticleSt = db.compileStatement(
				"insert into articles(title,url,status,point,date,feedId) values (?,?,?,?,?,?);");
		SQLiteStatement insertCurationSelectionSt = db.compileStatement(
				"insert into " + CurationSelection.TABLE_NAME +
				"(" + CurationSelection.ARTICLE_ID + "," + CurationSelection.CURATION_ID + ") values (?,?);");
		db.beginTransaction();
		try {
			for (Article article : articles) {
				insertArticleSt.bindString(1, article.getTitle());
				insertArticleSt.bindString(2, article.getUrl());
				insertArticleSt.bindString(3, article.getStatus());
				insertArticleSt.bindString(4, article.getPoint());
				Log.d(LOG_TAG, "insert date:" + article.getPostedDate());
				insertArticleSt.bindLong(5, article.getPostedDate());
				insertArticleSt.bindString(6, String.valueOf(feedId));

				long articleId = insertArticleSt.executeInsert();
				for (Map.Entry<Integer, ArrayList<String>> entry : curationWordMap.entrySet()) {
					int curationId = entry.getKey();
					ArrayList<String> words = entry.getValue();
					for (String word : words) {
						if (article.getTitle().contains(word)) {
							insertCurationSelectionSt.bindString(1, String.valueOf(articleId));
							insertCurationSelectionSt.bindString(2, String.valueOf(curationId));
							insertCurationSelectionSt.executeInsert();
							break;
						}
					}
				}
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
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
		} catch (SQLException e) {
			e.printStackTrace();
			unreadArticlesCount = -1;
		} finally {
			db.endTransaction();
		}
		
		return unreadArticlesCount;
	}
	
	public boolean isExistArticle(int feedId) {
		boolean isExist = false;
		db.beginTransaction();
		try {
			String getArticlesCountsql = "select _id from articles where feedId = "
					+ feedId + " limit 1";
			Cursor countCursor = db.rawQuery(getArticlesCountsql, null);
			isExist = (countCursor.getCount() > 0);
			countCursor.close();
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		
		return isExist;
	}
	
	public boolean isExistArticle() {
		boolean isExist = false;
		db.beginTransaction();
		try {
			String getArticlesCountsql = "select _id from articles limit 1";
			Cursor countCursor = db.rawQuery(getArticlesCountsql, null);
			isExist = (countCursor.getCount() > 0);
			countCursor.close();
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		
		return isExist;
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
		} catch (SQLException e) {
			e.printStackTrace();
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
			String[] columns = {Feed.ID,Feed.TITLE,Feed.URL,Feed.ICON_PATH,Feed.SITE_URL,Feed.UNREAD_ARTICLE};
			String orderBy = Feed.TITLE;
			Cursor cursor = db.query(Feed.TABLE_NAME, columns, null, null, null, null, orderBy);
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
		} catch (SQLException e) {
			e.printStackTrace();
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
		} catch (SQLException e) {
			e.printStackTrace();
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
		} catch (SQLException e) {
			e.printStackTrace();
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
		} catch (SQLException e) {
			e.printStackTrace();
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
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		
	}

    public void saveAllStatusToReadFromToRead() {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("status", Article.READ);
            String condition = "status = '" + Article.TOREAD + "'";
            db.update("articles", values, condition, null);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
			e.printStackTrace();
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
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		
	}

	public void updateUnreadArticleCount(int feedId, int unreadCount) {
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put("unreadArticle", unreadCount);
			db.update("feeds", values, "_id = " + feedId, null);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
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
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		
	}
	
	public void saveHatenaPoint(String url, String point) {
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put(Article.POINT, point);
			db.update(Article.TABLE_NAME, values, Article.URL + " = '" + url + "'", null);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
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
		} catch (SQLException e) {
			e.printStackTrace();
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
		} catch (SQLException e) {
			e.printStackTrace();
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
		} catch (SQLException e) {
			e.printStackTrace();
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
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		return feed;
	}

	public Feed getFeedById(int feedId) {
		Feed feed = null;
		db.beginTransaction();
		try {
			// Get feed
			String[] culumn = {Feed.TITLE, Feed.URL, Feed.ICON_PATH, Feed.SITE_URL};
			String selection = Feed.ID + " = " + feedId;
			Cursor cur = db.query(Feed.TABLE_NAME, culumn, selection, null, null, null, null);
			if (cur.getCount() != 0) {
				cur.moveToNext();
				String feedTitle = cur.getString(0);
				String feedUrl = cur.getString(1);
				String iconPath = cur.getString(2);
				String siteUrl = cur.getString(3);

				feed = new Feed(feedId, feedTitle, feedUrl, iconPath, siteUrl, 0);
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
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
		} catch (SQLException e) {
			 e.printStackTrace();
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
			String sql = "select articles._id,articles.title,articles.url,articles.point,articles.date,articles.feedId,feeds.title,feeds.iconPath " +
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
				String feedIconPath = cursor.getString(7);
				Article article = new Article(id, title, url, status, point,
						dateLong, feedId, feedTitle, feedIconPath);
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
		ArrayList<Article> articles = new ArrayList<>();
		db.beginTransaction();
		try {
			// Get unread articles
			String sql = "select articles._id,articles.title,articles.url,articles.status,articles.point,articles.date,articles.feedId,feeds.title,feeds.iconPath " +
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
				String feedIconPath = cursor.getString(8);
				Article article = new Article(id, title, url, status, point,
						dateLong, feedId, feedTitle, feedIconPath);
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
	
	public ArrayList<Article> searchArticles(String keyword, boolean isNewestArticleTop) {
		ArrayList<Article> articles = new ArrayList<Article>();
		db.beginTransaction();
		try {
			if(keyword.contains("%")) {
				keyword.replace("%", "$%");
			}
			if(keyword.contains("_")) {
				keyword.replace("_", "$_");
			}
			String sql = "select articles._id,articles.title,articles.url,articles.status,articles.point,articles.date,feeds.title,feeds.iconPath " +
					"from articles inner join feeds " +
					"where articles.title like '%" + keyword + "%' escape '$' and " +
					"articles.feedId = feeds._id " +
					"order by date";
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
				String feedTitle = cursor.getString(6);
				String feedIconPath = cursor.getString(7);
				Article article = new Article(id, title, url, status, point,
						dateLong, 0, feedTitle, feedIconPath);
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
						dateLong, feedId, null, null);
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
						dateLong, feedId, null, null);
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
				filterList.add(new Filter(id, title, keyword, url, feedId, null));
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
				db.endTransaction();
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
		} catch (SQLException e) {
			e.printStackTrace();
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
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	public boolean isArticle(Article article) {
		int num = 0;
		try {
			// Get same article
			String[] columns = {"_id"};
			String selection = "url = ?";
			String[] selectionArgs = {article.getUrl()};
			Cursor cursor = db.query("articles", columns, selection, selectionArgs, null, null, null);
			num = cursor.getCount();
			cursor.close();
		} catch (Exception e) {
			num = -1;
		} finally {
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

	public void deleteAllFeeds() {
		db.beginTransaction();
		try {
			db.delete("feeds", "", null);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}
	
	public ArrayList<Filter> getAllFilters() {
		ArrayList<Filter> filters = new ArrayList<Filter>();
		String[] columns = {"filters._id","filters.title","filters.keyword","filters.url","filters.feedId","feeds.title"};
		String selection = "filters.feedId = feeds._id";
		db.beginTransaction();
		try {
			Cursor cursor = db.query("filters inner join feeds", columns, selection, null, null, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int id = cursor.getInt(0);
					String title = cursor.getString(1);
					String keyword = cursor.getString(2);
					String url = cursor.getString(3);
					int feedId = cursor.getInt(4);
					String feedTitle = cursor.getString(5);
					Filter filter = new Filter(id, title, keyword, url, feedId, feedTitle);
					filters.add(filter);
				}
				cursor.close();
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		return filters;
	}


	public void exportDb() {
		try {
			File backupStrage;
			String sdcardRootPath = FileUtil.getSDCardRootPath();
			if (FileUtil.isSDCardMouted(sdcardRootPath)) {
				Log.d(LOG_TAG, "SD card is mounted");
				backupStrage = new File(FileUtil.getSDCardRootPath());
			}else {
				Log.d(LOG_TAG, "not mounted");
				backupStrage = Environment.getExternalStorageDirectory();
			}
			if (backupStrage.canWrite()) {
				Log.d(LOG_TAG, "Backup storage is writable");

				String backupDBFolderPath = BACKUP_FOLDER + "/";
				File backupDBFolder = new File(backupStrage, backupDBFolderPath);
				backupDBFolder.delete();
				if (backupDBFolder.mkdir()) {
					Log.d(LOG_TAG, "Succeeded to make directory");

				}else {
					Log.d(LOG_TAG, "Failed to make directory");
				}
				File backupDB = new File(backupStrage, backupDBFolderPath + DatabaseHelper.DATABASE_NAME);

				String  currentDBPath= "/data/data/" + context.getPackageName()
						+ "/databases/" + DatabaseHelper.DATABASE_NAME;
				File currentDB = new File(currentDBPath);

				// Copy database
				FileChannel src = new FileInputStream(currentDB).getChannel();
				FileChannel dst = new FileOutputStream(backupDB).getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void importDB() {
		try {
			File backupStrage;
			String sdcardRootPath = FileUtil.getSDCardRootPath();
			if (FileUtil.isSDCardMouted(sdcardRootPath)) {
				Log.d(LOG_TAG, "SD card is mounted");
				backupStrage = new File(FileUtil.getSDCardRootPath());
			}else {
				Log.d(LOG_TAG, "not mounted");
				backupStrage = Environment.getExternalStorageDirectory();
				Log.d(LOG_TAG, "path:" + backupStrage.getAbsolutePath());
			}
			if (backupStrage.canRead()) {
				Log.d(LOG_TAG, "Backup storage is readable");

				String backupDBPath = BACKUP_FOLDER + "/" + DatabaseHelper.DATABASE_NAME;
				File newDB  = new File(backupStrage, backupDBPath);
				if (!newDB.exists()) {
					return;
				}

				String  currentDBPath= "/data/data/" + context.getPackageName()
						+ "/databases/" + DatabaseHelper.DATABASE_NAME;
				File currentDB = new File(currentDBPath);

				FileChannel src = new FileInputStream(newDB).getChannel();
				FileChannel dst = new FileOutputStream(currentDB).getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean saveNewCuration(String name, ArrayList<String> words) {
		if(words.isEmpty()) {
			return false;
		}
		db.beginTransaction();
		boolean result = true;
		try {
			ContentValues values = new ContentValues();
			values.put(Curation.NAME, name);
			long addedCurationId = db.insert(Curation.TABLE_NAME, null, values);
			for (String word : words) {
				ContentValues condtionValue = new ContentValues();
				condtionValue.put(CurationCondition.CURATION_ID, addedCurationId);
				condtionValue.put(CurationCondition.WORD, word);
				db.insert(CurationCondition.TABLE_NAME, null, condtionValue);
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
			result = false;
		} finally {
			db.endTransaction();
		}
		return result;
	}

	public boolean adaptCurationToArticles(String curationName, ArrayList<String> words) {
		int curationId = getCurationIdByName(curationName);
		if (curationId == NOT_FOUND_ID) {
			return false;
		}

		boolean result = true;
		ArrayList<Article> articles = getAllArticles(true);
		SQLiteStatement insertSt = db
				.compileStatement("insert into " + CurationSelection.TABLE_NAME +
						"(" + CurationSelection.ARTICLE_ID + "," + CurationSelection.CURATION_ID + ") values (?," + curationId + ");");
		db.beginTransaction();
		try {
			for (Article article : articles) {
				for (String word : words) {
					if (article.getTitle().contains(word)) {
						insertSt.bindString(1, String.valueOf(article.getId()));
						insertSt.executeInsert();
					}
				}
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
			result = false;
		} finally {
			db.endTransaction();
		}
		return result;
	}

	public ArrayList<Curation> getAllCurations() {
		ArrayList<Curation> curationList = new ArrayList<>();
		db.beginTransaction();
		try {
			String[] columns = {Curation.ID, Curation.NAME};
			String orderBy = Curation.NAME;
			Cursor cursor = db.query(Curation.TABLE_NAME, columns, null, null, null, null, orderBy);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int id = cursor.getInt(0);
					String name = cursor.getString(1);
					curationList.add(new Curation(id, name));
				}
				cursor.close();
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}

		return curationList;
	}

	public boolean deleteCuration(int curationId) {
		int numOfDeleted = 0;
		db.beginTransaction();
		try {
			db.delete(CurationCondition.TABLE_NAME, CurationCondition.CURATION_ID + " = " + curationId, null);
			db.delete(CurationSelection.TABLE_NAME, CurationSelection.CURATION_ID + " = " + curationId, null);
			numOfDeleted = db.delete(Curation.TABLE_NAME, Curation.ID + " = " + curationId, null);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		if (numOfDeleted == 1) {
			return true;
		}
		return false;
	}

	public boolean deleteAllCuration() {
		boolean result = true;
		db.beginTransaction();
		try {
			db.delete(CurationCondition.TABLE_NAME, "", null);
			db.delete(CurationSelection.TABLE_NAME, "", null);
			db.delete(Curation.TABLE_NAME, "", null);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
			result = false;
		} finally {
			db.endTransaction();
		}
		return result;
	}

	public boolean isExistSameNameCuration(String name) {
		int num = 0;
		try {
			String[] columns = {Curation.ID};
			String selection = Curation.NAME + " = ?";
			String[] selectionArgs = {name};
			Cursor cursor = db.query(Curation.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
			num = cursor.getCount();
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(num > 0) {
				return true;
			}
			return false;
		}
	}

	public int getCurationIdByName(String name) {
		int id = NOT_FOUND_ID;
		try {
			String[] columns = {Curation.ID};
			String selection = Curation.NAME + " = '' || ? || ''";
			String[] selectionArgs = {name};
			Cursor cursor = db.query(Curation.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
			cursor.moveToFirst();
			id = cursor.getInt(0);
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return id;
		}
	}

	public String getCurationNameById(int curationId) {
		String name = "";
		try {
			String[] columns = {Curation.NAME};
			String selection = Curation.ID + " = ?";
			String[] selectionArgs = {String.valueOf(curationId)};
			Cursor cursor = db.query(Curation.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
			cursor.moveToFirst();
			name = cursor.getString(0);
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return name;
		}
	}

	public ArrayList<Article> getAllUnreadArticlesOfCuration(int curationId, boolean isNewestArticleTop) {
		ArrayList<Article> articles = new ArrayList<>();
		String sql = "select " + Article.TABLE_NAME + "." + Article.ID + "," +
				Article.TABLE_NAME + "." + Article.TITLE + "," +
				Article.TABLE_NAME + "." + Article.URL + "," +
				Article.TABLE_NAME + "." + Article.STATUS + "," +
				Article.TABLE_NAME + "." + Article.POINT + "," +
				Article.TABLE_NAME + "." + Article.DATE + "," +
				Article.TABLE_NAME + "." + Article.FEEDID + "," +
				Feed.TABLE_NAME + "." + Feed.TITLE + "," +
				Feed.TABLE_NAME + "." + Feed.ICON_PATH +
				" from (" + Article.TABLE_NAME + " inner join " + CurationSelection.TABLE_NAME +
				" on " + CurationSelection.CURATION_ID + " = " + curationId + " and " +
				Article.TABLE_NAME + "." + Article.STATUS + " = '" + Article.UNREAD + "' and " +
				Article.TABLE_NAME + "." + Article.ID + " = " + CurationSelection.TABLE_NAME + "." + CurationSelection.ARTICLE_ID + ")" +
				" inner join " + Feed.TABLE_NAME +
				" on " + Article.TABLE_NAME + "." + Article.FEEDID + " = " + Feed.TABLE_NAME + "." + Feed.ID +
				" order by " + Article.DATE;
		if(isNewestArticleTop) {
			sql += " desc";
		}else {
			sql += " asc";
		}
		try {
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
				String feedIconPath = cursor.getString(8);
				Article article = new Article(id, title, url, status, point,
						dateLong, feedId, feedTitle, feedIconPath);
				articles.add(article);
			}
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return articles;
		}
	}
	public int calcNumOfAllUnreadArticlesOfCuration(int curationId) {
		int num = 0;
		String sql = "select " + Article.TABLE_NAME + "." + Article.ID + "," +
				Article.TABLE_NAME + "." + Article.TITLE + "," +
				Article.TABLE_NAME + "." + Article.URL + "," +
				Article.TABLE_NAME + "." + Article.STATUS + "," +
				Article.TABLE_NAME + "." + Article.POINT + "," +
				Article.TABLE_NAME + "." + Article.DATE + "," +
				Article.TABLE_NAME + "." + Article.FEEDID +
				" from " + Article.TABLE_NAME + " inner join " + CurationSelection.TABLE_NAME +
				" where " + CurationSelection.CURATION_ID + " = " + curationId + " and " +
				Article.TABLE_NAME + "." + Article.STATUS + " = '" + Article.UNREAD + "' and " +
				Article.TABLE_NAME + "." + Article.ID + " = " + CurationSelection.TABLE_NAME + "." + CurationSelection.ARTICLE_ID +
				" order by " + Article.DATE;
		try {
			Cursor cursor = db.rawQuery(sql, null);
			num = cursor.getCount();
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return num;
		}
	}

	public ArrayList<String> getCurationWords(int curationId) {
		ArrayList<String> words = new ArrayList<>();
		String[] columns = {CurationCondition.WORD};
		String selection = CurationCondition.CURATION_ID + " = ?";
		String[] selectionArgs = {String.valueOf(curationId)};
		try {
			Cursor cursor = db.query(CurationCondition.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
			while (cursor.moveToNext()) {
				words.add(cursor.getString(0));
			}
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return words;
		}
	}

	public ArrayList<Article> getAllArticlesOfCuration(int curationId, boolean isNewestArticleTop) {
		ArrayList<Article> articles = new ArrayList<>();
		String sql = "select " + Article.TABLE_NAME + "." + Article.ID + "," +
				Article.TABLE_NAME + "." + Article.TITLE + "," +
				Article.TABLE_NAME + "." + Article.URL + "," +
				Article.TABLE_NAME + "." + Article.STATUS + "," +
				Article.TABLE_NAME + "." + Article.POINT + "," +
				Article.TABLE_NAME + "." + Article.DATE + "," +
				Article.TABLE_NAME + "." + Article.FEEDID + "," +
				Feed.TABLE_NAME + "." + Feed.TITLE + "," +
				Feed.TABLE_NAME + "." + Feed.ICON_PATH +
				" from (" + Article.TABLE_NAME + " inner join " + CurationSelection.TABLE_NAME +
				" on " + CurationSelection.CURATION_ID + " = " + curationId + " and " +
				Article.TABLE_NAME + "." + Article.ID + " = " + CurationSelection.TABLE_NAME + "." + CurationSelection.ARTICLE_ID + ")" +
				" inner join " + Feed.TABLE_NAME +
				" on " + Article.TABLE_NAME + "." + Article.FEEDID + " = " + Feed.TABLE_NAME + "." + Feed.ID +
				" order by " + Article.DATE;
		if(isNewestArticleTop) {
			sql += " desc";
		}else {
			sql += " asc";
		}
		try {
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
				String feedIconPath = cursor.getString(8);
				Article article = new Article(id, title, url, status, point,
						dateLong, feedId, feedTitle, feedIconPath);
				articles.add(article);
			}
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return articles;
		}
	}

	public Map<Integer, ArrayList<String>> getAllCurationWords() {
		Map<Integer, ArrayList<String>> curationWordsMap = new HashMap<>();
		String sql = "select " + Curation.TABLE_NAME + "." + Curation.ID + "," +
				CurationCondition.TABLE_NAME + "." + CurationCondition.WORD +
				" from " + Curation.TABLE_NAME + " inner join " + CurationCondition.TABLE_NAME +
				" where " + Curation.TABLE_NAME + "." + Curation.ID + " = " + CurationCondition.TABLE_NAME + "." + CurationCondition.CURATION_ID +
				" order by " + Curation.TABLE_NAME + "." + Curation.ID;
		try {
			Cursor cursor = db.rawQuery(sql, null);
			final int defaultCurationId = -1;
			int curationId = defaultCurationId;
			ArrayList<String> words = new ArrayList<>();
			while (cursor.moveToNext()) {
				int newCurationId = cursor.getInt(0);
				if (curationId == defaultCurationId) {
					curationId = newCurationId;
				}
				// Add words of curation to map when curation ID changes
				if (curationId != newCurationId) {
					curationWordsMap.put(curationId, words);
					curationId = newCurationId;
					words = new ArrayList<>();
				}
				String word = cursor.getString(1);
				words.add(word);
			}
			// Add last words of curation
			if (curationId != defaultCurationId) {
				curationWordsMap.put(curationId, words);
			}

			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return curationWordsMap;
		}
	}
}
