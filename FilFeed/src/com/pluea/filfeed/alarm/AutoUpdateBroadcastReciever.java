package com.pluea.filfeed.alarm;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pluea.filfeed.db.DatabaseAdapter;
import com.pluea.filfeed.rss.Article;
import com.pluea.filfeed.rss.Feed;
import com.pluea.filfeed.task.GetHatenaBookmarkPointTask;
import com.pluea.filfeed.task.UpdateTaskManager;

public class AutoUpdateBroadcastReciever extends BroadcastReceiver {

	public static final String AUTO_UPDATE_ACTION = "autoUpdateFeed";
	public static final String AUTO_UPDATE_HATENA_ACTION = "autoUpdateHatena";
	
	DatabaseAdapter dbAdapter;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("AutoUpdateBroadcastReciever", "onReceive");
		if(intent == null) {
			return;
		}
		dbAdapter = DatabaseAdapter.getInstance(context);
		if(intent.getAction().equals(AUTO_UPDATE_ACTION)) {
			UpdateTaskManager updateTask = UpdateTaskManager.getInstance(context);
	
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
			if (UpdateTaskManager.getInstance(context).isUpdatingFeed()) {
				AlarmManagerTaskManager.setNewHatenaUpdateAlarmAfterFeedUpdate(context);
				return;
			}
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
					GetHatenaBookmarkPointTask hatenaTask = new GetHatenaBookmarkPointTask(
							context);
					hatenaTask.execute(unreadArticle);
				}
			}
			
			// Update hatena bookmark point after interval
			AlarmManagerTaskManager.setNewHatenaUpdateAlarm(context);
		}
	}
}
