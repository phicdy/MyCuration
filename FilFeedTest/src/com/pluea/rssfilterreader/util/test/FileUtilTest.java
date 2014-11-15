package com.pluea.rssfilterreader.util.test;

import java.net.MalformedURLException;
import java.net.URL;

import android.test.AndroidTestCase;

import com.pluea.filfeed.util.FileUtil;

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
		assertEquals("/data/data/com.pluea.filfeed/",
				FileUtil.getAppPath(getContext()));
	}

	public void testGetIconSavePath() {
		assertEquals("/data/data/com.pluea.filfeed/icons/",
				FileUtil.iconSaveFolder(getContext()));
	}

	public void testGenerateIconFileName() {
		assertEquals(
				"/data/data/com.pluea.filfeed/icons/gigazine.net.png",
				FileUtil.generateIconFilePath(getContext(), "http://gigazine.net/"));
	}
}
