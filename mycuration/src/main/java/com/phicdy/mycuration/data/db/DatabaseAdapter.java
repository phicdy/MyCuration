package com.phicdy.mycuration.data.db;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

public class DatabaseAdapter {
	
    private static DatabaseAdapter sharedDbAdapter;

	private DatabaseAdapter() {
	}

	public static void setUp() {
		if (sharedDbAdapter == null) {
			synchronized (DatabaseAdapter.class) {
				if (sharedDbAdapter == null) {
					sharedDbAdapter = new DatabaseAdapter();
				}
			}
		}
	}

	@VisibleForTesting
	public static void inject(@NonNull DatabaseAdapter adapter) {
	    sharedDbAdapter = adapter;
    }

	public static DatabaseAdapter getInstance() {
		if (sharedDbAdapter == null) {
		    throw new IllegalStateException("Not setup yet");
		}
		return sharedDbAdapter;
	}


}
