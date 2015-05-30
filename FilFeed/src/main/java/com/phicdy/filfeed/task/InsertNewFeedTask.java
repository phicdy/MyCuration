package com.phicdy.filfeed.task;
  
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.phicdy.filfeed.R;
import com.phicdy.filfeed.db.DatabaseAdapter;
import com.phicdy.filfeed.rss.Feed;
import com.phicdy.filfeed.rss.RssParser;
  
/**
 * 
 * @author kyamaguchi
 * @param  String : data type for task(ex.download URL)
 * @param  String : data type for displaying the progress
 * @param  Feed : submit data type when task will finish 
 */
public class InsertNewFeedTask extends AsyncTask<String, String, Feed>{
  
    private Context context;
    private ProgressDialog progress_;
    private DatabaseAdapter dbAdapter;
    private RssParser rssParser;
    
    public static final String FINISH_INSERT_SUCCEEDED = "FINISH_INSERT_SUCCEEDED";
    public static final String FINISH_INSERT_FAILED = "FINISH_INSERT_FAILED";
      
    public InsertNewFeedTask(Context context) {
        this.context = context;
        dbAdapter  = DatabaseAdapter.getInstance(context);
        rssParser  = new RssParser(context);
    }
  
    /**
     * Execute on main thread
     */
    @Override
    protected void onPostExecute(Feed result) {
        progress_.dismiss();
        if(result == null) {
        	Toast.makeText(context, R.string.add_feed_error, Toast.LENGTH_SHORT).show();
        	context.sendBroadcast(new Intent(FINISH_INSERT_SUCCEEDED));
        }else {
        	Toast.makeText(context, R.string.add_feed_success, Toast.LENGTH_SHORT).show();
        	context.sendBroadcast(new Intent(FINISH_INSERT_FAILED));
        }
    }
    /**
     * Execute before doing task
     */
    @Override
    protected void onPreExecute() {
        //Display a dialog
        progress_ = new ProgressDialog(context);
        progress_.setMessage("Now loading");
    }
  
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }
      
    /**
     * Get articles from RSS Feed
     */
    @Override
    protected Feed doInBackground(String... setting ) {
        Feed newFeed = null;
        try {
            //Set URLConnection
            String urlString = setting[0]; 
            
            //Judge XML
            newFeed = rssParser.parseFeedInfo(urlString);
            
            //Update new feed
            if(newFeed != null) {
	            //Get Feed id from feed URL
	            Feed feed = dbAdapter.getFeedByUrl(urlString);
	            
	        	//Parse XML and get new Articles
	            UpdateTaskManager taskManager = UpdateTaskManager.getInstance(context);
	            taskManager.updateFeed(feed);
	            
	            //Set num of unread articles
	            newFeed.setUnreadArticlesCount(dbAdapter.calcNumOfUnreadArticles(newFeed.getId()));
            }
            
            return newFeed;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return newFeed;
    }
}
