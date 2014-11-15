package com.pleua.rssfilterreader.rss.test;

import android.test.AndroidTestCase;

import com.pluea.filfeed.rss.IconParser;

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
		assertEquals("http://s0.wp.com/wp-content/themes/vip/tctechcrunch2/images/favicon.ico?m=1357660109g", parser.parseHtml("http://jp.techcrunch.com"));
		assertEquals((Object)"http://b.hatena.ne.jp/favicon.ico", (Object)parser.parseHtml("http://b.hatena.ne.jp"));
		assertEquals(null, parser.parseHtml("http://hogehoge"));
	}

}
