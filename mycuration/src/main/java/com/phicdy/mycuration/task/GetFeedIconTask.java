package com.phicdy.mycuration.task;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.IconParser;
import com.phicdy.mycuration.util.FileUtil;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetFeedIconTask extends AsyncTask<String, Void, Void> {

	private final String iconSaveDir;

	public GetFeedIconTask(@NonNull String iconSaveDir) {
		this.iconSaveDir = iconSaveDir;
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
			File iconSaveFolder  = new File(iconSaveDir);
			if(!iconSaveFolder.exists()) {
				if(!iconSaveFolder.mkdir()) {
					return;
				}
			}
			
			String iconPath = FileUtil.INSTANCE.generateIconFilePath(iconSaveDir, siteUrl);
            if (iconPath == null) return;
			DataOutputStream dataOutStream = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(
							 iconPath)));

			// Read Data
			byte[] b = new byte[4096];
			int readByte;

			while (-1 != (readByte = dataInStream.read(b))) {
				dataOutStream.write(b, 0, readByte);
			}

			// Close Stream
			dataInStream.close();
			dataOutStream.close();

            DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance();
			dbAdapter.saveIconPath(siteUrl, iconPath);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
