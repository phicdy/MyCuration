package com.pluea.filfeed.task;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import com.pluea.filfeed.db.DatabaseAdapter;
import com.pluea.filfeed.rss.Article;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class GetHatenaBookmarkPointTask extends AsyncTask<Article, String, Article> {

	/**
	 * 
	 * @author kyamaguchi
	 * @param String
	 *            : data type for task(ex.download URL)
	 * @param String
	 *            : data type for displaying the progress
	 * @param Feed
	 *            : submit data type when task will finish
	 */

	private DatabaseAdapter dbAdapter;
	private static final String GET_HATENA_BOOKMARK_COUNT_URL = "http://api.b.st-hatena.com/entry.count";
	private static final String CHAR_SET = "UTF-8";
	public static final String FINISH_GET_HATENA = "finishGetHatena";
	public static final String ARTICLE_ID = "articleId";
	public static final String ARTICLE_ARRAY_INDEX = "articleArrayIndex";
	public static final String ARTICLE_POINT = "articlePoint";
	public static final String LOG_TAG = "RSSReader.GetHatena";

	public GetHatenaBookmarkPointTask(Context context) {
		dbAdapter = DatabaseAdapter.getInstance(context);
	}

	/**
	 * Execute on main thread
	 */
	@Override
	protected void onPostExecute(Article result) {
	}

	/**
	 * Execute before doing task
	 */
	@Override
	protected void onPreExecute() {
	}

	@Override
	protected void onProgressUpdate(String... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}

	/**
	 * Get articles from RSS Feed
	 * 
	 * @return
	 */
	@Override
	protected Article doInBackground(Article... setting) {
		Article article = setting[0];
		try {
			// Set URLConnection
			String getBookmarkUrlString = article.getUrl();
			URL url = new URL(GET_HATENA_BOOKMARK_COUNT_URL + "?url="
					+ getBookmarkUrlString);
			URLConnection con = url.openConnection();
			// Set Timeout 1min
			con.setConnectTimeout(60000);
			con.setReadTimeout(60000);

			int point = getHatenaCount(url);
			article.setPoint(String.valueOf(point));
			saveHatenaCount(article);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return article;

	}

	private int getHatenaCount(URL url) throws IOException {
		int count = 0;
		try {
			// �ڑ�
			URLConnection uc = url.openConnection();
			// HTML��ǂݍ���
			BufferedInputStream bis = new BufferedInputStream(
					uc.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(bis,
					CHAR_SET));
			String line;
			while ((line = br.readLine()) != null) {
				count = Integer.valueOf(line);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return count;
	}
	
	private void saveHatenaCount(Article article) {
//		Log.d(LOG_TAG, "article id:" + article.getId());
//		Log.d(LOG_TAG, "article point:" + article.getPoint());
		dbAdapter.saveHatenaPoint(article.getId(), article.getPoint());
	}

}
