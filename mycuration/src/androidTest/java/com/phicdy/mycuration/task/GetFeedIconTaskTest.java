package com.phicdy.mycuration.task;

import android.support.test.runner.AndroidJUnit4;

import com.phicdy.mycuration.task.GetFeedIconTask;
import com.phicdy.mycuration.util.FileUtil;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class GetFeedIconTaskTest {

    @After
    public void tearDown() {
        File file = new File(FileUtil.INSTANCE.iconSaveFolder(getTargetContext()) + "/kindou.info.png");
        if (file.exists()) file.delete();
        File greeBlogIcon = new File(FileUtil.INSTANCE.iconSaveFolder(getTargetContext()) + "/labs.gree.jp.png");
        if (greeBlogIcon.exists()) greeBlogIcon.delete();
    }

    @Test
	public void iconExistsWhenGetKindouIcon() {
        File file = new File(FileUtil.INSTANCE.iconSaveFolder(getTargetContext()) + "/kindou.info.png");
        if (file.exists()) assertTrue(file.delete());
		String iconSaveFolderStr = FileUtil.INSTANCE.iconSaveFolder(getTargetContext());
		GetFeedIconTask task = new GetFeedIconTask(iconSaveFolderStr);
		task.execute("http://kindou.info");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		file = new File(FileUtil.INSTANCE.iconSaveFolder(getTargetContext()) + "/kindou.info.png");
		assertTrue(file.exists());
	}

	@Test
	public void iconDoesNotExistWhenGetGreeBlogIcon() {
        File greeBlogIcon = new File(FileUtil.INSTANCE.iconSaveFolder(getTargetContext()) + "/labs.gree.jp.png");
        if (greeBlogIcon.exists()) assertTrue(greeBlogIcon.delete());
		String iconSaveFolderStr = FileUtil.INSTANCE.iconSaveFolder(getTargetContext());
		GetFeedIconTask greeBlogIconTask = new GetFeedIconTask(iconSaveFolderStr);
		greeBlogIconTask.execute("http://labs.gree.jp/blog");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        greeBlogIcon = new File(FileUtil.INSTANCE.iconSaveFolder(getTargetContext()) + "/labs.gree.jp.png");
		assertFalse(greeBlogIcon.exists());
	}
}
