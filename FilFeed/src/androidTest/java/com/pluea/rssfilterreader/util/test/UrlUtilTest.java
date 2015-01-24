package com.pluea.rssfilterreader.util.test;

import android.test.AndroidTestCase;

import com.pluea.filfeed.util.UrlUtil;

public class UrlUtilTest extends AndroidTestCase {

	public UrlUtilTest() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testRemoveUrlParameter() {
		String removedUrl = UrlUtil.removeUrlParameter("http://harofree.blog.fc2.com/?ps");
		assertEquals("http://harofree.blog.fc2.com/", removedUrl);
	}
	
	public void testHasParameterUrl() {
		assertEquals(true, UrlUtil.hasParameterUrl("http://www.xxx.com/?aaa"));
		assertEquals(true, UrlUtil.hasParameterUrl("https://www.xxx.com/?aaa"));
		assertEquals(true, UrlUtil.hasParameterUrl("http://www.xxx.com/aaa/?bbb"));
		assertEquals(false, UrlUtil.hasParameterUrl("http://www.xxx.com/aaa"));
		assertEquals(false, UrlUtil.hasParameterUrl("http://www.xxx.com?/aaa"));
	}
}
