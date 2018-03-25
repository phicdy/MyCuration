package com.phicdy.mycuration.domain.filter;

import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.data.filter.Filter;

import java.util.ArrayList;
  
public class FilterTask {

    private final DatabaseAdapter dbAdapter;
      
    public FilterTask() {
        dbAdapter = DatabaseAdapter.getInstance();
    }
  
      
    public void applyFiltering(int feedId) {
        //Get Filters of Feed ID
        ArrayList<Filter> filterList = dbAdapter.getEnabledFiltersOfFeed(feedId);
        if (filterList == null || filterList.size() == 0) return;
        dbAdapter.applyFiltersOfFeed(filterList, feedId);
    }
      
 
      
      
}
