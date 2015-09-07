package com.phicdy.filfeed.ui;
  
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.phicdy.filfeed.R;
import com.phicdy.filfeed.alarm.AlarmManagerTaskManager;
import com.phicdy.filfeed.util.PreferenceHelper;

public class SettingActivity extends PreferenceActivity {
  
	private Spinner spUpdateInterval;
	private CheckBox cbSortNewArticleTop;
	private CheckBox cbAllReadBack;
	private CheckBox cbOpenInternal;
	private Spinner spSwipeDirection;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
//        initView();
//        setLitener();
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingFragment())
				.commit();

	}
    
    private void initView() {
        setTitle(getString(R.string.setting));
    	
    	PreferenceHelper mgr = PreferenceHelper.getInstance(getApplicationContext());
    	spUpdateInterval = (Spinner)findViewById(R.id.sp_update_interval);
    	// Set stored position of value
    	int autoUpdateIntervalSecond = mgr.getAutoUpdateIntervalSecond();
    	int autoUpdateIntervalHour = autoUpdateIntervalSecond / (60 * 60);
    	String updateIntervalHourItems[] = getResources().getStringArray(R.array.update_interval_items);
    	for (int indexOfSpinner = 0; indexOfSpinner < updateIntervalHourItems.length; indexOfSpinner++) {
    		if (Integer.valueOf(updateIntervalHourItems[indexOfSpinner]) == autoUpdateIntervalHour) {
    			spUpdateInterval.setSelection(indexOfSpinner);
    			break;
    		}
		}
    	
    	cbSortNewArticleTop = (CheckBox)findViewById(R.id.cb_article_sort);
    	cbSortNewArticleTop.setChecked(mgr.getSortNewArticleTop());
    	
    	cbAllReadBack = (CheckBox)findViewById(R.id.cb_all_read_back);
    	cbAllReadBack.setChecked(mgr.getAllReadBack());
    	
    	cbOpenInternal = (CheckBox)findViewById(R.id.cb_open_internal);
    	cbOpenInternal.setChecked(mgr.isOpenInternal());
    	
    	spSwipeDirection = (Spinner)findViewById(R.id.sp_swipe_direction);
    	spSwipeDirection.setSelection(mgr.getSwipeDirection());
    }
    
    private void setLitener() {
    	Button saveButton = (Button)findViewById(R.id.saveButton);
    	saveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				PreferenceHelper mgr = PreferenceHelper.getInstance(getApplicationContext());
				
				// Save update interval
				int intervalHour = Integer.valueOf((String)spUpdateInterval.getSelectedItem());
				int intervalSecond = intervalHour * 60 * 60;
				mgr.setAutoUpdateIntervalSecond(intervalSecond);
				
				mgr.setSortNewArticleTop(cbSortNewArticleTop.isChecked());
				mgr.setAllReadBack(cbAllReadBack.isChecked());
				mgr.setOpenInternal(cbOpenInternal.isChecked());
				
				// Save swipe direction
				mgr.setSwipeDirection(spSwipeDirection.getSelectedItemPosition());
				
				AlarmManagerTaskManager.setNewAlarm(getApplicationContext());
				
				Toast.makeText(getApplicationContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show();
			}
		});
    }

}
