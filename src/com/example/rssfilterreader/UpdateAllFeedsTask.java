package com.example.rssfilterreader;
  
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
  
/**
 * 
 * @author kyamaguchi
 * @param  String : data type for task(ex.download URL)
 * @param  String : data type for displaying the progress
 * @param  Integer : submit data type when task will finish 
 */
public class UpdateAllFeedsTask extends AsyncTask<ArrayList<Feed>, String, Boolean>{
  
    private Context context_;
    private ProgressDialog progress_;
    private RssParser rssParser;
    private FilterTask filterTask;
    private DatabaseAdapter dbAdapter;
    private Intent intent = new Intent();
    private ArrayList<Feed> feeds;
    private int numOfUnreadArticles;
    private int feedId;
    private boolean showProgress = false;
    
    //Singleton
    private static UpdateAllFeedsTask updateTask = new UpdateAllFeedsTask();
    
    private static final String LOG_TAG = "RSS_READER."+UpdateAllFeedsTask.class.getName();
    
    private UpdateAllFeedsTask() {
        Log.i(LOG_TAG, "Create instance");
    }
  
    public static UpdateAllFeedsTask getInstance() {
    	return updateTask;
    }
    
    public void setActivity(Context context) {
    	context_  = context;
        rssParser  = new RssParser(context_);
        filterTask = new FilterTask(context_);
        dbAdapter  = new DatabaseAdapter(context_);
    }
    
    public void setProgressVisibility(boolean showProgress) {
    	this.showProgress = showProgress;
    }
    
    /**
     * Execute on main thread
     */
    @Override
    protected void onPostExecute(Boolean result) {
    	if(result) {
    		if(!feeds.isEmpty() && showProgress) {
    			Toast.makeText(context_, R.string.article_updated, Toast.LENGTH_SHORT).show();
    		}
    	}else if(showProgress){
    		Toast.makeText(context_, R.string.article_update_error, Toast.LENGTH_SHORT).show();
    	}
    	//Broadcast updating num of articles
		intent.setAction(MainActivity.UPDATE_NUM_OF_ARTICLES);
		context_.sendBroadcast(intent);
    	
		if(showProgress) {
			progress_.dismiss();
		}
    	
    	//initialize task because task can execute only one time
    	updateTask = new UpdateAllFeedsTask();
    }
    /**
     * Execute before doing task
     */
    @Override
    protected void onPreExecute() {
        //Display a dialog
    	if(showProgress) {
	        progress_ = new ProgressDialog(context_);
	        progress_.setMessage("Now loading");
	        progress_.show();
    	}
    }
  
    @Override
    protected void onProgressUpdate(String... values) {
        // TODO Auto-generated method stub
        super.onProgressUpdate(values);
    }
      
    /**
     * Get articles from RSS Feed
     */
    @SuppressLint("DefaultLocale")
	@Override
    protected Boolean doInBackground(ArrayList<Feed>... feedsValue ) {
        try {
        	feeds = feedsValue[0];
        	for(Feed feed : feeds) {
	            //Set URL string and id
	            String urlString = feed.getUrl();
	            feedId = feed.getId();
	            
	        	//Parse XML
	            boolean parseResult = rssParser.parseXml(urlString, feedId);
	            
	            //Update articles "toRead" status to "read"
	            if(!dbAdapter.changeArticlesStatusToRead()) {
	            	return false;
	            }
	            
	            //Filter articles
	            if(parseResult) {
	                if(filterTask.applyFiltering(feedId)) {
	            		continue;
	                }else {
	                	return false;
	                }
	            }else {
	                return false;
	            }
        	}
        	return true;
        }catch(Exception e) {
    		e.printStackTrace();
        }
        return false;
    }

	@Override
	protected void onCancelled() {
		Log.i(LOG_TAG, "task is canceled complete");
		super.onCancelled();
	}
     

}
