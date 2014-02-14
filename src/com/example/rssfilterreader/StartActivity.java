package com.example.rssfilterreader;
  
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
  
public class StartActivity extends Activity {
  
	private static final long SLEEP_TIME_MS = 1200;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Handler mHandler = new Handler();
	    Runnable mUpdateTimeTask = new Runnable() {
	    	public void run() {
	    		startActivity(new Intent(StartActivity.this, MainActivity.class));
	        }
		};

		mHandler.postDelayed(mUpdateTimeTask, SLEEP_TIME_MS);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
    
    
}
