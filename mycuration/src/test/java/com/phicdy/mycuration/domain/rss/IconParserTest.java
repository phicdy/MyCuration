package com.phicdy.mycuration.domain.rss;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class IconParserTest {

	public IconParserTest() {
		super();
	}

	@Test
	public void testParseXml() {
		IconParser parser = new IconParser();
		assertEquals("https://gigazine.net/favicon.ico", parser.parseHtml("https://gigazine.net"));
		assertEquals("https://s0.wp.com/wp-content/themes/vip/techcrunch-jp-2015/assets/images/favicon.ico", parser.parseHtml("http://jp.techcrunch.com"));
		assertEquals("http://b.hatena.ne.jp/favicon.ico", parser.parseHtml("http://b.hatena.ne.jp"));
	}

}
