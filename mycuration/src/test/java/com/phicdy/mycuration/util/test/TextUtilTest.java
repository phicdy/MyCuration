package com.phicdy.mycuration.util.test;

import com.phicdy.mycuration.util.TextUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class TextUtilTest  {

	public TextUtilTest() {
		super();
	}

	@Test
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
