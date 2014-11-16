package com.pluea.filfeed.rss;
 
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.pluea.filfeed.db.DatabaseAdapter;
import com.pluea.filfeed.util.DateParser;

public class RssParser {

	private DatabaseAdapter dbAdapter;
	private static final int CONNCT_TIMEOUT_MS = 20000;
	private static final int READ_TIMEOUT_MS = 60000;
	private boolean isArticleFlag = false;
	private static final String LOG_TAG = "RSSREADER.RssParser";

	public RssParser(Context context) {
		dbAdapter = new DatabaseAdapter(context);
	}

	public boolean parseXml(String urlString, int feedId) throws IOException {
		InputStream is = setInputStream(urlString);
		if (is == null) {
			return false;
		}

		boolean result = true;
		ArrayList<Article> articles = new ArrayList<Article>();

		// TODO Get hatena bookmark(?) count
		Article article = new Article(0, null, null, null, "10", 0, 0);

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
						article = new Article(0, null, null, null, "10", 0, 0);
						itemFlag = true;
					}

					// add Title and Link to currentItem
					if (itemFlag && tag.equals("title")
							&& (article.getTitle() == null)) {
						String title = parser.nextText();
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private InputStream setInputStream(String urlString) throws IOException {
		// Set URLConnection
		URL url = new URL(urlString);
		try {
			URLConnection con = url.openConnection();
			// Set Timeout 1min
			con.setConnectTimeout(CONNCT_TIMEOUT_MS);
			con.setReadTimeout(READ_TIMEOUT_MS);

			// Get InputStream
			return con.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Feed parseFeedInfo(String feedUrl) throws IOException {
		if (feedUrl == null || feedUrl.equals("")) {
			return null;
		}
		if (!(feedUrl.startsWith("http://") || feedUrl.startsWith("https://"))) {
			return null;
		}

		// Get InputStream
		InputStream is = setInputStream(feedUrl);

		String format = null;
		String feedTitle = null;
		String siteURL = null;

		try {
			// Initialize XmlPullParser
			XmlPullParser parser = Xml.newPullParser();

			// Whether FileType is RSS
			boolean rssFlag = false;
			// RSS Format

			// RSS Format is
			// <feed><title> or <rdf(or rss)><channel><title> ,
			// so check start tag <feed> or <rdf> or <rss> and then <title>
			parser.setInput(is, "UTF-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tag = parser.getName();
				Log.d(LOG_TAG, "tag: " + tag);
				if (eventType == XmlPullParser.START_TAG) {

					// Atom
					/*
					 * if(tag.toLowerCase().equals("feed")) { rssFlag = true;
					 * format = "Atom"; }else
					 */if (tag.toLowerCase().equals("rdf")) {
						// RSS 1.0,2.0
						rssFlag = true;
						format = "RSS1.0";
					} else if (tag.toLowerCase().equals("rss")) {
						rssFlag = true;
						format = "RSS2.0";
					} else if (tag.toLowerCase().equals("feed")) {
						rssFlag = true;
						format = "ATOM";
					} else if (tag.toLowerCase().equals("html")) {
						return parseTopHtml(parser, is);
					}

					if (rssFlag && tag.equals("title")) {
						// Feed title
						feedTitle = parser.nextText();
					}

					// Site title exist first "link" tag before item
					if (rssFlag && tag.equals("link")) {
						siteURL = parser.nextText();
						if (siteURL == null || siteURL.equals("")) {
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
									siteURL = attributeValue;
									if (siteURL.startsWith("http://")
											|| siteURL.startsWith("https://")) {
										break;
									}
								}
							}
						}
						Log.d(LOG_TAG, siteURL);
					}

				}
				eventType = parser.next();
				if (format != null && feedTitle != null
						&& !feedTitle.equals("") && siteURL != null
						&& !siteURL.equals("")) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		is.close();
		return dbAdapter.saveNewFeed(feedTitle, feedUrl, format, siteURL);
	}

	private String translateEventType(int eventType) {
		String eventTypeString = "";
		switch (eventType) {
		case XmlPullParser.START_TAG:
			eventTypeString = "START_TAG";
			break;
		case XmlPullParser.END_TAG:
			eventTypeString = "END_TAG";
			break;
		case XmlPullParser.START_DOCUMENT:
			eventTypeString = "START_DOCUMENT";
			break;
		case XmlPullParser.END_DOCUMENT:
			eventTypeString = "END_DOCUMENT";
			break;
		case XmlPullParser.ENTITY_REF:
			eventTypeString = "ENTITY_REF";
			break;
		case XmlPullParser.TEXT:
			eventTypeString = "TEXT";
			break;
		default:
			eventTypeString = "Others:" + eventType;
			break;
		}

		return eventTypeString;
	}

	private Feed parseTopHtml(XmlPullParser parser, InputStream is) {
		String rssURL = "";
		String tag = "";
		try {
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				tag = parser.getName();
				Log.d(LOG_TAG, "tag: " + tag);
				if (eventType == XmlPullParser.START_TAG) {
					if (tag.equals("link")) {
						String attributeName;
						String attributeValue;
						boolean isRssHtml = false;
						boolean isHref = false;
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							attributeName = parser.getAttributeName(i);
							attributeValue = parser.getAttributeValue(i);
							if (attributeName == null || attributeValue == null) {
								continue;
							}
							if (attributeName.equals("type")) {
								if(attributeValue.equals("application/rss+xml")) {
									isRssHtml = true;
									continue;
								}else {
									break;
								}
							}
							if (attributeName.equals("href")) {
								isHref = true;
							}
	
							if (isRssHtml && isHref) {
								rssURL = attributeValue;
								if (isCorrectUrl(rssURL)) {
									is.close();
									return parseFeedInfo(rssURL);
								}
							}
						}
					}
				}
				eventType = parser.next();
			}
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean isCorrectUrl(String url) {
		if(url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
			return true;
		}
		return false;
	}
}