package com.phicdy.mycuration.util.test;

import android.test.AndroidTestCase;

import com.phicdy.mycuration.util.TextUtil;

public class TextUtilTest extends AndroidTestCase {

	public TextUtilTest() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testRemoveLineFeed() {
		assertEquals("aaa", TextUtil.removeLineFeed("aaa\r"));
		assertEquals("aaa", TextUtil.removeLineFeed("aaa\n"));
		assertEquals("aaa", TextUtil.removeLineFeed("aaa\t"));
		assertEquals("aaa", TextUtil.removeLineFeed("aaa\r\n"));

		assertEquals("aaa", TextUtil.removeLineFeed("a\ra\na"));
		assertEquals("aaa", TextUtil.removeLineFeed("a\ra\ta"));
		assertEquals("aaa", TextUtil.removeLineFeed("a\ra\r\na"));
		assertEquals("aaa", TextUtil.removeLineFeed("a\na\ta"));
		assertEquals("aaa", TextUtil.removeLineFeed("a\na\r\na"));
		assertEquals("aaa", TextUtil.removeLineFeed("a\ta\r\na"));
	}
}
