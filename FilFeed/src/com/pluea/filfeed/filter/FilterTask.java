package com.pluea.filfeed.filter;
  
import java.util.ArrayList;

import com.pluea.filfeed.db.DatabaseAdapter;

import android.content.Context;
  
/**
 * 
 * @author kyamaguchi
 * @param  String : data type for task(ex.download URL)
 * @param  String : data type for displaying the progress
 * @param  Boolean : submit data type when task will finish 
 */
public class FilterTask {
  
    private Context context_;
    private DatabaseAdapter dbAdapter;
      
    public FilterTask(Context context) {
        context_ = context;
        dbAdapter = DatabaseAdapter.getInstance(context);
    }
  
      
    public boolean applyFiltering(int feedId) {
        //Get Filters of Feed ID
        ArrayList<Filter> filterList = dbAdapter.getFiltersOfFeed(feedId);
          
        return dbAdapter.applyFiltersOfFeed(filterList, feedId);
    }
      
 
      
      
}
