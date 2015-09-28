package com.phicdy.mycuration.rss.test;

import android.test.AndroidTestCase;

import com.phicdy.mycuration.rss.IconParser;

public class IconParserTest extends AndroidTestCase {

	public IconParserTest() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testParseXml() {
		IconParser parser = new IconParser();
		assertEquals("http://gigazine.net/favicon.ico", parser.parseHtml("http://gigazine.net"));
		assertEquals("https://s0.wp.com/wp-content/themes/vip/techcrunch-jp-2015/assets/images/favicon.ico", parser.parseHtml("http://jp.techcrunch.com"));
		assertEquals((Object)"http://b.hatena.ne.jp/favicon.ico", (Object)parser.parseHtml("http://b.hatena.ne.jp"));
		assertEquals(null, parser.parseHtml("http://hogehoge"));
	}

}
