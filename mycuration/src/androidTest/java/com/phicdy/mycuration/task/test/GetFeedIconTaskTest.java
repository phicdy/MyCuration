package com.phicdy.mycuration.task.test;

import android.support.test.runner.AndroidJUnit4;

import com.phicdy.mycuration.task.GetFeedIconTask;
import com.phicdy.mycuration.util.FileUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class GetFeedIconTaskTest {

    @Test
	public void testGetFeedIcon() {
		GetFeedIconTask task = new GetFeedIconTask(getTargetContext());
		task.execute("http://kindou.info");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		File file = new File(FileUtil.INSTANCE.iconSaveFolder(getTargetContext()) + "/kindou.info.png");
		assertTrue(file.exists());
			
		GetFeedIconTask greeBlogIconTask = new GetFeedIconTask(getTargetContext());
		greeBlogIconTask.execute("http://labs.gree.jp/blog");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		File greeBlogIcon = new File(FileUtil.INSTANCE.iconSaveFolder(getTargetContext()) + "/labs.gree.jp.png");
		assertEquals(false, greeBlogIcon.exists());
	}
}
