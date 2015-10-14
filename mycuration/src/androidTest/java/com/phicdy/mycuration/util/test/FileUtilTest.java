package com.phicdy.mycuration.util.test;

import android.test.AndroidTestCase;

import com.phicdy.mycuration.util.FileUtil;

public class FileUtilTest extends AndroidTestCase {

	public FileUtilTest() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAppPath() {
		assertEquals("/data/data/com.phicdy.mycuration/",
				FileUtil.getAppPath(getContext()));
	}

	public void testGetIconSavePath() {
		assertEquals("/data/data/com.phicdy.mycuration/icons/",
				FileUtil.iconSaveFolder(getContext()));
	}

	public void testGenerateIconFileName() {
		assertEquals(
				"/data/data/com.phicdy.mycuration/icons/gigazine.net.png",
				FileUtil.generateIconFilePath(getContext(), "http://gigazine.net/"));
	}
}
