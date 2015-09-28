package com.phicdy.mycuration.task;

import android.content.Context;
import android.os.AsyncTask;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.IconParser;
import com.phicdy.mycuration.util.FileUtil;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class GetFeedIconTask extends AsyncTask<String, Void, Void> {

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
	private Context context;

	public static final String LOG_TAG = "FilFeed.GetIcon";

	public GetFeedIconTask(Context context) {
		this.context = context;
		dbAdapter = DatabaseAdapter.getInstance(context);
	}

	/**
	 * Execute on main thread
	 */
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
	}

	/**
	 * Execute before doing task
	 */
	@Override
	protected void onPreExecute() {
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
	}

	/**
	 * Get articles from RSS Feed
	 * 
	 * @return
	 */
	@Override
	protected Void doInBackground(String... url) {
		getFeedIcon(url[0]);
		return null;
	}

	private void getFeedIcon(String siteUrl) {
		IconParser parser = new IconParser();
		String iconUrlStr = parser.parseHtml(siteUrl);
		
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(iconUrlStr).openConnection();
			conn.setAllowUserInteraction(false);
			conn.setInstanceFollowRedirects(true);
			conn.setRequestMethod("GET");
			conn.connect();

			int httpStatusCode = conn.getResponseCode();

			if (httpStatusCode != HttpURLConnection.HTTP_OK) {
				throw new Exception();
			}

			// Input Stream
			DataInputStream dataInStream = new DataInputStream(
					conn.getInputStream());

			// Output Stream
			String iconSaveFolderStr = FileUtil.iconSaveFolder(context);
			File iconSaveFolder  = new File(iconSaveFolderStr);
			if(!iconSaveFolder.exists()) {
				iconSaveFolder.mkdir();
			}
			
			String iconPath = FileUtil.generateIconFilePath(context, siteUrl);
			
			DataOutputStream dataOutStream = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(
							 iconPath)));

			// Read Data
			byte[] b = new byte[4096];
			int readByte = 0;

			while (-1 != (readByte = dataInStream.read(b))) {
				dataOutStream.write(b, 0, readByte);
			}

			// Close Stream
			dataInStream.close();
			dataOutStream.close();
			
			dbAdapter.saveIconPath(siteUrl, iconPath);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
