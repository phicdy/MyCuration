package com.phicdy.filfeed.alarm;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.phicdy.filfeed.db.DatabaseAdapter;
import com.phicdy.filfeed.rss.Article;
import com.phicdy.filfeed.rss.Feed;
import com.phicdy.filfeed.task.GetHatenaBookmarkPointTask;
import com.phicdy.filfeed.task.NetworkTaskManager;
import com.phicdy.filfeed.util.NetworkUtil;

public class AutoUpdateBroadcastReciever extends BroadcastReceiver {

	public static final String AUTO_UPDATE_ACTION = "autoUpdateFeed";
	public static final String AUTO_UPDATE_HATENA_ACTION = "autoUpdateHatena";
	
	DatabaseAdapter dbAdapter;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("AutoUpdate", "onReceive, action:" + intent.getAction());
		if(intent == null) {
			return;
		}
		dbAdapter = DatabaseAdapter.getInstance(context);
		if(intent.getAction().equals(AUTO_UPDATE_ACTION)) {
			NetworkTaskManager updateTask = NetworkTaskManager.getInstance(context);
	
			updateTask.updateAllFeeds(dbAdapter.getAllFeedsWithoutNumOfUnreadArticles());
			AlarmManagerTaskManager.setNewHatenaUpdateAlarmAfterFeedUpdate(context);
			
			// Save new time
			AlarmManagerTaskManager.setNewAlarm(context);
		}else if(intent.getAction().equals(AUTO_UPDATE_HATENA_ACTION)) {
			// Update Hatena point
			ArrayList<Feed> feeds = dbAdapter.getAllFeedsThatHaveUnreadArticles();
			if (feeds == null || feeds.isEmpty()) {
				return;
			}
			// Update has higher priority
			if (NetworkTaskManager.getInstance(context).isUpdatingFeed()) {
				AlarmManagerTaskManager.setNewHatenaUpdateAlarmAfterFeedUpdate(context);
				return;
			}
			boolean isWifiConnected = NetworkUtil.isWifiConnected(context);
			for (Feed feed : feeds) {
				ArrayList<Article> unreadArticles = dbAdapter
						.getUnreadArticlesInAFeed(feed.getId(), true);
				if (unreadArticles == null || unreadArticles.isEmpty()) {
					continue;
				}
				for (int i = 0; i < unreadArticles.size(); i++) {
					Article unreadArticle = unreadArticles.get(i);
					if (unreadArticle == null) {
						continue;
					}
					if ((unreadArticle.getPoint() != Article.DEDAULT_HATENA_POINT) && !isWifiConnected) {
						Log.d("AutoUpdate", "Device is not connected with Wi-Fi");
						continue;
					}
					GetHatenaBookmarkPointTask hatenaTask = new GetHatenaBookmarkPointTask(
							context);
					hatenaTask.execute(unreadArticle);
				}
			}
		}
	}
}
