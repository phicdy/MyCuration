package com.phicdy.mycuration.data.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.phicdy.mycuration.data.rss.Article;
import com.phicdy.mycuration.data.rss.Curation;
import com.phicdy.mycuration.data.rss.CurationCondition;
import com.phicdy.mycuration.data.rss.CurationSelection;
import com.phicdy.mycuration.data.rss.Feed;
import com.phicdy.mycuration.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import timber.log.Timber;

public class DatabaseAdapter {
	
    private static DatabaseAdapter sharedDbAdapter;
	private SQLiteDatabase db;

	private static final String BACKUP_FOLDER = "filfeed_backup";
	public static final int NOT_FOUND_ID = -1;

	private DatabaseAdapter() {
	}

	public static void setUp(@NonNull DatabaseHelper dbHelper) {
		if (sharedDbAdapter == null) {
			synchronized (DatabaseAdapter.class) {
				if (sharedDbAdapter == null) {
					sharedDbAdapter = new DatabaseAdapter();
					sharedDbAdapter.db = dbHelper.getWritableDatabase();
				}
			}
		}
	}

	@VisibleForTesting
	public static void inject(@NonNull DatabaseAdapter adapter) {
	    sharedDbAdapter = adapter;
    }

	public static DatabaseAdapter getInstance() {
		if (sharedDbAdapter == null) {
		    throw new IllegalStateException("Not setup yet");
		}
		return sharedDbAdapter;
	}


	public void exportDb(@NonNull File currentDB) {
		try {
			File backupStrage;
			String sdcardRootPath = FileUtil.INSTANCE.getSdCardRootPath();
            Timber.d("SD Card path: %s", sdcardRootPath);
			if (FileUtil.INSTANCE.isSDCardMouted(sdcardRootPath)) {
				Timber.d("SD card is mounted");
				backupStrage = new File(FileUtil.INSTANCE.getSdCardRootPath());
			}else {
				Timber.d("not mounted");
				backupStrage = Environment.getExternalStorageDirectory();
			}
			if (backupStrage.canWrite()) {
				Timber.d("Backup storage is writable");

				String backupDBFolderPath = BACKUP_FOLDER + "/";
				File backupDBFolder = new File(backupStrage, backupDBFolderPath);
                if (backupDBFolder.exists()) {
                    if (backupDBFolder.delete()) {
                        Timber.d("Succeeded to delete backup directory");
                    } else {
                        Timber.d("Failed to delete backup directory");
                    }
                }
                if (backupDBFolder.mkdir()) {
                    Timber.d("Succeeded to make directory");
                } else {
                    Timber.d("Failed to make directory");
                }
				File backupDB = new File(backupStrage, backupDBFolderPath + DatabaseHelper.DATABASE_NAME);

				// Copy database
				FileChannel src = new FileInputStream(currentDB).getChannel();
				FileChannel dst = new FileOutputStream(backupDB).getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
			} else {
                // TODO Runtime Permission
                Timber.d("SD Card is not writabble, enable storage permission in Android setting");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void importDB(@NonNull File currentDB) {
		try {
			File backupStrage;
			String sdcardRootPath = FileUtil.INSTANCE.getSdCardRootPath();
			if (FileUtil.INSTANCE.isSDCardMouted(sdcardRootPath)) {
				Timber.d("SD card is mounted");
				backupStrage = new File(FileUtil.INSTANCE.getSdCardRootPath());
			}else {
				Timber.d("not mounted");
				backupStrage = Environment.getExternalStorageDirectory();
				Timber.d("path:%s", backupStrage.getAbsolutePath());
			}
			if (backupStrage.canRead()) {
				Timber.d("Backup storage is readable");

				String backupDBPath = BACKUP_FOLDER + "/" + DatabaseHelper.DATABASE_NAME;
				File newDB  = new File(backupStrage, backupDBPath);
				if (!newDB.exists()) {
					return;
				}

				FileChannel src = new FileInputStream(newDB).getChannel();
				FileChannel dst = new FileOutputStream(currentDB).getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
			} else {
                // TODO Runtime Permission
                Timber.d("SD Card is not readabble, enable storage permission in Android setting");
            }
		} catch (Exception e) {
			e.printStackTrace();
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
		}
        return id;
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
		}
        return name;
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
		}
        return articles;
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
		}
        return words;
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
		}
        return articles;
	}

}
