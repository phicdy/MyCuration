package com.phicdy.mycuration.rss;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Xml;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.task.NetworkTaskManager;
import com.phicdy.mycuration.util.DateParser;
import com.phicdy.mycuration.util.TextUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class RssParser {

	private DatabaseAdapter dbAdapter;
	private UnreadCountManager unreadCountManager;
	private boolean isArticleFlag = false;
	private Context context;

	private static final String LOG_TAG = "FilFeed.RssParser";

	public RssParser(Context context) {
		dbAdapter = DatabaseAdapter.getInstance(context);
		unreadCountManager = UnreadCountManager.getInstance(context);
		this.context = context;
	}

	public void parseRssXml(final String baseUrl) {
		new Thread() {
			@Override
			public void run() {
				try {
					final URL url = new URL(baseUrl);
					if (!"http".equalsIgnoreCase(url.getProtocol())
							&& !"https".equalsIgnoreCase(url.getProtocol())) {
						sendFailAddFeedUrlBroadcast(NetworkTaskManager.ERROR_INVALID_URL);
						return;
					}
					Document document = Jsoup.connect(baseUrl).get();
					if (!document.getElementsByTag("rdf").isEmpty()) {
						// RSS 1.0
						Elements links = document.getElementsByTag("link");
						String siteUrl = null;
						for (Element element : links) {
							if (element.parent().tag().toString().equals("channel")) {
								siteUrl = element.text();
								break;
							}
						}
						if (siteUrl == null || siteUrl.equals("")) {
							siteUrl = url.getProtocol() + "://" + url.getHost();
						}
						String title = document.title();
						dbAdapter.saveNewFeed(title, baseUrl, Feed.RSS_1, siteUrl);
						sendAddUrlSuccessBroadcast(baseUrl);
					}else if (!document.getElementsByTag("rss").isEmpty()) {
						// RSS 2.0
						Elements links = document.getElementsByTag("link");
						String siteUrl = null;
						for (Element element : links) {
							if (element.parent().tag().toString().equals("channel")) {
								siteUrl = element.text();
								break;
							}
						}
						if (siteUrl == null || siteUrl.equals("")) {
							siteUrl = url.getProtocol() + "://" + url.getHost();
						}
						String title = document.title();
						dbAdapter.saveNewFeed(title, baseUrl, Feed.RSS_2, siteUrl);
						sendAddUrlSuccessBroadcast(baseUrl);
					}else if (!document.getElementsByTag("feed").isEmpty()) {
						// ATOM:
						//<?xml version="1.0" encoding="utf-8"?>
						//<feed xmlns="http://www.w3.org/2005/Atom">
						//   <title>Example Feed</title>
						//   <link href="http://example.org/"/>
						//    <updated>2003-12-13T18:30:02Z</updated>
						//    <author>
						//        <name>John Doe</name>
						//    </author>
						//    <id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>
						//    <entry>
						//        <title>Atom-Powered Robots Run Amok</title>
						//        <link href="http://example.org/2003/12/13/atom03"/>
						//        <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>
						//        <updated>2003-12-13T18:30:02Z</updated>
						//        <summary>Some text.</summary>
						//    </entry>
						//</feed>
						Elements links = document.getElementsByTag("link");
						String siteUrl;
						if (links.isEmpty()) {
							siteUrl = url.getProtocol() + "://" + url.getHost();
						}else {
							siteUrl = links.get(0).attr("href");
						}
						String title = document.title();
						dbAdapter.saveNewFeed(title, baseUrl, Feed.ATOM, siteUrl);
						sendAddUrlSuccessBroadcast(baseUrl);
					}else if (!document.getElementsByTag("html").isEmpty()) {
						//<link rel="alternate" type="application/rss+xml" title="TechCrunch Japan &raquo; フィード" href="http://jp.techcrunch.com/feed/" />
						Elements elements = document.getElementsByAttributeValue("type", "application/rss+xml");
						if (elements.isEmpty()) {
							return;
						}
						String feedUrl = elements.get(0).attr("href");
						parseRssXml(feedUrl);
						return;
					} else {
						sendFailAddFeedUrlBroadcast(NetworkTaskManager.ERROR_NON_RSS_HTML_CONTENT);
					}
				} catch (MalformedURLException e) {
					sendFailAddFeedUrlBroadcast(NetworkTaskManager.ERROR_INVALID_URL);
				} catch (IOException e) {
					e.printStackTrace();
					sendFailAddFeedUrlBroadcast(NetworkTaskManager.ERROR_UNKNOWN);
				}
			}
		}.start();
	}

	public boolean parseXml(InputStream is, int feedId) {
//		Log.d(LOG_TAG, "Parse start");

		boolean result = true;
		ArrayList<Article> articles = new ArrayList<Article>();

		// TODO Get hatena bookmark(?) count
		Article article = new Article(0, null, null, Article.UNREAD, Article.DEDAULT_HATENA_POINT, 0, 0, null, null);

		// Initialize XmlPullParser
		XmlPullParser parser = Xml.newPullParser();

		// Flag for not getting "Site's" title and url
		boolean itemFlag = false;

		try {
			parser.setInput(is, "UTF-8");

			// Start parse to the END_DOCUMENT
			int eventType = parser.getEventType();
			String tag = parser.getName();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_TAG:
					// when new Item found, initialize currentItem
					if (tag.equals("item") || tag.equals("entry")) {
						article = new Article(0, null, null, Article.UNREAD, Article.DEDAULT_HATENA_POINT, 0, 0, null, null);
						itemFlag = true;
					}

					// add Title and Link to currentItem
					if (itemFlag && tag.equals("title")
							&& (article.getTitle() == null)) {
						String title = TextUtil.removeLineFeed(parser.nextText());
						Log.d(LOG_TAG, "set article title:" + title);
						article.setTitle(title);
					}
					if (itemFlag && tag.equals("link")
							&& (article.getUrl() == null)) {
						String articleURL = parser.nextText();
						if (articleURL == null || articleURL.equals("")) {
							String attributeName;
							String attributeValue;
							boolean isAlternate = false;
							boolean isTextHtml = false;
							boolean isHref = false;
							for (int i = 0; i < parser.getAttributeCount(); i++) {
								attributeName = parser.getAttributeName(i);
								attributeValue = parser.getAttributeValue(i);
								if (attributeName == null
										|| attributeValue == null) {
									continue;
								}

								if (attributeName.equals("rel")
										&& attributeValue.equals("alternate")) {
									isAlternate = true;
									continue;
								}
								if (attributeName.equals("type")
										&& attributeValue.equals("text/html")) {
									isTextHtml = true;
									continue;
								}
								if (attributeName.equals("href")) {
									isHref = true;
								}

								if (isAlternate && isTextHtml && isHref) {
									articleURL = attributeValue;
									if (articleURL.startsWith("http://")
											|| articleURL
													.startsWith("https://")) {
										break;
									}
								}
							}
						}
						Log.d(LOG_TAG, "set article URL:" + articleURL);
						article.setUrl(articleURL);
					}
					if (itemFlag
							&& (tag.equals("date") || tag.equals("pubDate") || tag
									.equals("published"))
							&& (article.getPostedDate() == 0)) {
						String date = parser.nextText();
						Log.d(LOG_TAG, "set article date:" + date);
						article.setPostedDate(DateParser
								.changeToJapaneseDate(date));
					}
					break;

				// When </Item> add currentItem to DB
				case XmlPullParser.END_TAG:
					tag = parser.getName();
					if (tag.equals("item") || tag.equals("entry")) {
						if (dbAdapter.isArticle(article)) {
							isArticleFlag = true;
						} else {
							articles.add(article);
						}
						itemFlag = false;
					}
					break;
				}
				if (!result) {
					return false;
				}
				// If article is already saved, stop parse
				if (isArticleFlag) {
					// break;
				}
				eventType = parser.next();
				tag = parser.getName();
				if (eventType == XmlPullParser.END_TAG
						&& (tag.equals("rss") || tag.equals("rdf") || tag
								.equals("feed"))) {
					break;
				}
			}
			// Save new articles
			dbAdapter.saveNewArticles(articles, feedId);
			for (Article addedArticle : articles) {
				NetworkTaskManager.getInstance(context).getHatenaPoint(addedArticle);
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private void sendFailAddFeedUrlBroadcast(@NetworkTaskManager.AddFeedUrlError int error) {
		Intent intent = new Intent(NetworkTaskManager.FINISH_ADD_FEED);
		intent.putExtra(NetworkTaskManager.ADD_FEED_ERROR_REASON, error);
		context.sendBroadcast(intent);
	}

	private void sendAddUrlSuccessBroadcast(String url) {
		Intent intent = new Intent(NetworkTaskManager.FINISH_ADD_FEED);
		intent.putExtra(NetworkTaskManager.ADDED_FEED_URL, url);
		context.sendBroadcast(intent);
	}
}