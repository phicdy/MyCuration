package com.phicdy.mycuration.util.test;

import android.support.test.runner.AndroidJUnit4;

import com.phicdy.mycuration.util.FileUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class FileUtilTest {

	@Test
	public void testGetIconSavePath() {
		assertEquals(FileUtil.getAppPath(getTargetContext()) + "icons/",
				FileUtil.iconSaveFolder(getTargetContext()));
	}

	@Test
	public void testGenerateIconFileName() {
		assertEquals(
				FileUtil.getAppPath(getTargetContext()) + "icons/gigazine.net.png",
				FileUtil.generateIconFilePath(getTargetContext(), "http://gigazine.net/"));
	}
}
