package com.phicdy.mycuration.util.test;

import com.phicdy.mycuration.util.UrlUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class UrlUtilTest {

	public UrlUtilTest() {
		super();
	}

	@Test
	public void testRemoveUrlParameter() {
		String removedUrl = UrlUtil.INSTANCE.removeUrlParameter("http://harofree.blog.fc2.com/?ps");
		assertEquals("http://harofree.blog.fc2.com/", removedUrl);
	}

	@Test
	public void testHasParameterUrl() {
		assertEquals(true, UrlUtil.INSTANCE.hasParameterUrl("http://www.xxx.com/?aaa"));
		assertEquals(true, UrlUtil.INSTANCE.hasParameterUrl("https://www.xxx.com/?aaa"));
		assertEquals(true, UrlUtil.INSTANCE.hasParameterUrl("http://www.xxx.com/aaa/?bbb"));
		assertEquals(false, UrlUtil.INSTANCE.hasParameterUrl("http://www.xxx.com/aaa"));
		assertEquals(false, UrlUtil.INSTANCE.hasParameterUrl("http://www.xxx.com?/aaa"));
	}
}
