package com.phicdy.filfeed.task.test;

import android.test.AndroidTestCase;

import com.phicdy.filfeed.task.GetFeedIconTask;

import java.io.File;

public class GetFeedIconTaskTest extends AndroidTestCase {

	public GetFeedIconTaskTest() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetFeedIcon() {
		GetFeedIconTask task = new GetFeedIconTask(getContext());
		task.execute("http://kindou.info");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		File file = new File("/data/data/com.phicdy.filfeed/icons/kindou.info.png");
		assertEquals(true, file.exists());
			
		GetFeedIconTask greeBlogIconTask = new GetFeedIconTask(getContext());
		greeBlogIconTask.execute("http://labs.gree.jp/blog");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		File greeBlogIcon = new File("/data/data/com.phicdy.filfeed/icons/labs.gree.jp.png");
		assertEquals(false, greeBlogIcon.exists());
	}
}
