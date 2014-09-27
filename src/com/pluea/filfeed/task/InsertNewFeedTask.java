package com.pluea.filfeed.task;
  
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.xmlpull.v1.XmlPullParser;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Xml;
import android.widget.Toast;

import com.pluea.filfeed.R;
import com.pluea.filfeed.db.DatabaseAdapter;
import com.pluea.filfeed.rss.Feed;
import com.pluea.filfeed.rss.RssParser;
  
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
      
    public InsertNewFeedTask(Context context) {
        this.context = context;
        dbAdapter  = new DatabaseAdapter(context);
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
        }else {
        	Toast.makeText(context, R.string.add_feed_success, Toast.LENGTH_SHORT).show();
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
	            int feedId = feed.getId();
	            
	        	//Parse XML and get new Articles
	            rssParser.parseXml(urlString, feedId);
	            
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
