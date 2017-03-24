package com.phicdy.mycuration.filter;
  
import java.util.ArrayList;

import com.phicdy.mycuration.db.DatabaseAdapter;

import android.content.Context;
  
public class FilterTask {

    private DatabaseAdapter dbAdapter;
      
    public FilterTask(Context context) {
        dbAdapter = DatabaseAdapter.getInstance(context);
    }
  
      
    public boolean applyFiltering(int feedId) {
        //Get Filters of Feed ID
        ArrayList<Filter> filterList = dbAdapter.getEnabledFiltersOfFeed(feedId);
          
        return dbAdapter.applyFiltersOfFeed(filterList, feedId);
    }
      
 
      
      
}
