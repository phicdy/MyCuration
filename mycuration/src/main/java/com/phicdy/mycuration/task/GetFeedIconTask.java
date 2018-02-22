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
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetFeedIconTask extends AsyncTask<String, Void, Void> {

	private final String iconSaveDir;

	GetFeedIconTask(@NonNull String iconSaveDir) {
		this.iconSaveDir = iconSaveDir;
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

        DataInputStream dataInStream = null;
        DataOutputStream dataOutStream = null;
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(iconUrlStr).openConnection();
			conn.setAllowUserInteraction(false);
			conn.setInstanceFollowRedirects(true);
			conn.setRequestMethod("GET");
			conn.connect();

			int httpStatusCode = conn.getResponseCode();
			if (httpStatusCode != HttpURLConnection.HTTP_OK) return;
			dataInStream = new DataInputStream(conn.getInputStream());
			File iconSaveFolder  = new File(iconSaveDir);
			if(!iconSaveFolder.exists()) {
				if(!iconSaveFolder.mkdir()) {
					return;
				}
			}

			String iconPath = FileUtil.INSTANCE.generateIconFilePath(iconSaveDir, siteUrl);
            if (iconPath == null) return;
			dataOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(iconPath)));

			byte[] b = new byte[4096];
			int readByte;
			while (-1 != (readByte = dataInStream.read(b))) {
				dataOutStream.write(b, 0, readByte);
			}

            DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance();
			dbAdapter.saveIconPath(siteUrl, iconPath);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    if (dataInStream != null) {
                try {
                    dataInStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dataOutStream != null) {
                try {
                    dataOutStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

	}
}
