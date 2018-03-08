package com.phicdy.mycuration.util.test;

import android.support.test.runner.AndroidJUnit4;

import com.phicdy.mycuration.util.FileUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class FileUtilTest {

    @Before
    public void setup() {
        FileUtil.INSTANCE.setUpIconSaveFolder(getTargetContext());
    }

	@Test
	public void testGetIconSavePath() {
		assertEquals(FileUtil.INSTANCE.getAppPath(getTargetContext()) + "icons/",
				FileUtil.INSTANCE.iconSaveFolder());
	}

	@Test
	public void testGenerateIconFileName() {
		String iconSaveFolderStr = FileUtil.INSTANCE.iconSaveFolder();
		assertEquals(
				FileUtil.INSTANCE.getAppPath(getTargetContext()) + "icons/gigazine.net.png",
				FileUtil.INSTANCE.generateIconFilePath(iconSaveFolderStr, "http://gigazine.net/"));
	}
}
