package com.phicdy.mycuration.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Article;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class GetHatenaBookmarkPointTask extends AsyncTask<Article, String, Void> {

	/**
	 * 
	 * @author phicdy
	 * @param String
	 *            : data type for task(ex.download URL)
	 * @param String
	 *            : data type for displaying the progress
	 * @param Feed
	 *            : submit data type when task will finish
	 */

	private DatabaseAdapter dbAdapter;
	private Article targetArticle;
	private Context context;
	
	private static final String GET_HATENA_BOOKMARK_COUNT_URL = "http://api.b.st-hatena.com/entry.count";
	private static final String CHAR_SET = "UTF-8";
	private static final String LOG_TAG = "FilFeed.GetHatena";

	public GetHatenaBookmarkPointTask(Context context) {
		dbAdapter = DatabaseAdapter.getInstance(context);
	}

	/**
	 * Execute on main thread
	 */
	@Override
	protected void onPostExecute(Void result) {
	}

	/**
	 * Execute before doing task
	 */
	@Override
	protected void onPreExecute() {
	}

	@Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
	}

	/**
	 * Get articles from RSS Feed
	 * 
	 * @return
	 */
	@Override
	protected Void doInBackground(Article... setting) {
		if ((setting == null) || (setting.length != 1) || (setting[0] == null)) {
			return null;
		}
		
		targetArticle = setting[0];
		addUpdateRequetToQueue();
		return null;
	}

	private void addUpdateRequetToQueue() {
		NetworkTaskManager mgr = NetworkTaskManager.getInstance(context);
		mgr.addHatenaBookmarkUpdateRequest(createRequest());
		return;
	}
	
	private InputStreamRequest createRequest() {
		String requestUrl = generateRequestUrlString();
		if (requestUrl == null || requestUrl.equals("")) {
			return null;
		}
		return new InputStreamRequest(requestUrl,   
			       new Listener<InputStream>() {  
			  
			        @Override  
			        public void onResponse(final InputStream in) {
			        	if (in == null) {
			        		return;
			        	}
			        	// Read response and save to db
			        	int point = readHatenaBookmarkApiResponse(in);
						saveHatenaBookmarkPoint(point);
			        }  
			    }, new ErrorListener() {  
			  
			        @Override  
			        public void onErrorResponse(VolleyError error) {  
			        	Log.d("LOG_TAG", "Request error:" + error.getMessage());
			        }  
			    });
	}
	
	private int readHatenaBookmarkApiResponse(InputStream in) {
    	// Hatena bookmark API returns only that URL's point
    	BufferedInputStream bis = new BufferedInputStream(in);
		BufferedReader br = null;
		int point = 0;
		try {
			br = new BufferedReader(new InputStreamReader(bis, CHAR_SET));
			String line;
			while ((line = br.readLine()) != null) {
				point = Integer.valueOf(line);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// Close for memory leak
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return point;
	}
	
	private void saveHatenaBookmarkPoint(int point) {
		// Save hatena bookmark point
		if (targetArticle != null && point > -1) {
			dbAdapter.saveHatenaPoint(targetArticle.getUrl(), String.valueOf(point));
		}
	}
	
	private String generateRequestUrlString() {
		if (targetArticle == null) {
			return null;
		}
		return GET_HATENA_BOOKMARK_COUNT_URL + "?url=" + targetArticle.getUrl();
	}
}
