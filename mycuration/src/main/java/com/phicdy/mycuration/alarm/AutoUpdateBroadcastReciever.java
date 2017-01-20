package com.phicdy.mycuration.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Article;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.task.GetHatenaBookmarkPointTask;
import com.phicdy.mycuration.task.NetworkTaskManager;
import com.phicdy.mycuration.util.NetworkUtil;

import java.util.ArrayList;

public class AutoUpdateBroadcastReciever extends BroadcastReceiver {

	public static final String AUTO_UPDATE_ACTION = "autoUpdateFeed";
	public static final String AUTO_UPDATE_HATENA_ACTION = "autoUpdateHatena";
	
	DatabaseAdapter dbAdapter;

	@Override
	public void onReceive(Context context, Intent intent) {
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
			ArrayList<Feed> feeds = dbAdapter.getAllFeedsWithNumOfUnreadArticles();
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
