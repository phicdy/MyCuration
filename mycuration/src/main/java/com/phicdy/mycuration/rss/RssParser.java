package com.phicdy.mycuration.rss;

import android.util.Log;
import android.util.Xml;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.domain.task.GetHatenaBookmark;
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
import java.util.Date;

public class RssParser {

	private final DatabaseAdapter dbAdapter;
	private boolean isArticleFlag = false;
    private boolean isCanonical = false;

	private static final String LOG_TAG = "FilFeed.RssParser";

	public RssParser() {
		dbAdapter = DatabaseAdapter.getInstance();
	}

	private RssParseResult parse(String canonicalUrl) {
        if (isCanonical) {
            return new RssParseResult(RssParseResult.NOT_FOUND);
        }
        isCanonical = true;
        return parseRssXml(canonicalUrl, false);
    }

    /**
     * Try to parse RSS (RSS 1.0/2.0 and ATOM) from the URL.
     * If the URL is HTML URL, search RSS URL from link tag like below.
     * <link rel="alternate" type="application/rss+xml" title="TechCrunch Japan &raquo; フィード" href="http://jp.techcrunch.com/feed/" />
     *
     * @param baseUrl URL to parse. RSS URL or HTML URL.
     * @param checkCanonical If true, check canonical setting in the page like below
     * <link rel="canonical" href="http://xxxxxxxx">
     * @return RssParseResult. If not found or error occurs, feed instance in the result is null.
     */
	RssParseResult parseRssXml(final String baseUrl, final boolean checkCanonical) {
		Log.d(LOG_TAG, "Start to parse RSS XML, url:" + baseUrl);
        try {
            final URL url = new URL(baseUrl);
            if (!"http".equalsIgnoreCase(url.getProtocol())
                    && !"https".equalsIgnoreCase(url.getProtocol())) {
                Log.d(LOG_TAG, "URL does not start with http or https");
				return new RssParseResult(RssParseResult.INVALID_URL);
            }
            String pcUserAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.63 Safari/537.36";
            Document document = Jsoup.connect(baseUrl).userAgent(pcUserAgent).get();
            if (!document.getElementsByTag("rdf").isEmpty() || !document.getElementsByTag("rdf:rdf").isEmpty()) {
                Log.d(LOG_TAG, "RSS 1.0");
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
                Feed feed = new Feed(title, baseUrl, Feed.RSS_1, siteUrl);
                return new RssParseResult(feed);
            }else if (!document.getElementsByTag("rss").isEmpty()) {
                Log.d(LOG_TAG, "RSS 2.0");
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
                Feed feed = new Feed(title, baseUrl, Feed.RSS_2, siteUrl);
                return new RssParseResult(feed);
            }else if (!document.getElementsByTag("feed").isEmpty()) {
                Log.d(LOG_TAG, "ATOM");
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
                Feed feed = new Feed(title, baseUrl, Feed.ATOM, siteUrl);
                return new RssParseResult(feed);
            }else if (!document.getElementsByTag("html").isEmpty()) {
                Log.d(LOG_TAG, "html, try to get RSS URL");
                if (checkCanonical) {
                    // <link rel="canonical" href="http://xxxxxxxx">
                    Elements canonicalelements = document.getElementsByAttributeValue("rel", "canonical");
                    if (!canonicalelements.isEmpty()) {
                        // Canonical setting sets the actual site URL for google search
                        String pcUrl = canonicalelements.get(0).attr("href");
                        if (!pcUrl.startsWith("http://") && !pcUrl.startsWith("https")) {
                            // Path only, add protocol and host
                            pcUrl = new URL(url.getProtocol(), url.getHost(), pcUrl).toString();
                        }
                        Log.d(LOG_TAG, "canonical setting is found, try to parse " + pcUrl);
                        return parse(pcUrl);
                    }
                }
                //<link rel="alternate" type="application/rss+xml" title="TechCrunch Japan &raquo; フィード" href="http://jp.techcrunch.com/feed/" />
                Elements elements = document.getElementsByAttributeValue("type", "application/rss+xml");
                if (elements.isEmpty()) {
                    Log.d(LOG_TAG, "RSS URL was not found");
                    return new RssParseResult(RssParseResult.NOT_FOUND);
                }
                String feedUrl = elements.get(0).attr("href");
                if (feedUrl.startsWith("//")) {
                    // In http://smhn.info, feedUrl is "//smhn.info/feed"
                    // "//smhn.info" is not needed, get path from after URL host
                    String path = feedUrl.substring(2 + url.getHost().length());
                    feedUrl = new URL(url.getProtocol(), url.getHost(), path).toString();
                }else if (!feedUrl.startsWith("http://") && !feedUrl.startsWith("https")) {
                    // Path only, add protocol and host
                    feedUrl = new URL(url.getProtocol(), url.getHost(), feedUrl).toString();
                }
                Log.d(LOG_TAG, "RSS URL was found, " + feedUrl);
                return parseRssXml(feedUrl, false);
            } else {
                Log.d(LOG_TAG, "Fail, not RSS");
            }
        } catch (MalformedURLException e) {
            Log.d(LOG_TAG, "Fail, MalformedURLException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(LOG_TAG, "Fail, IOException");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Fail, Exception");
            e.printStackTrace();
        }
        return new RssParseResult(RssParseResult.NOT_FOUND);
	}

	public void parseXml(InputStream is, int feedId) {
		boolean result = true;
		ArrayList<Article> articles = new ArrayList<>();

		// TODO Get hatena bookmark(?) count
		Article article = new Article(0, null, null, Article.UNREAD, Article.DEDAULT_HATENA_POINT, 0, 0, null, null);

		// Initialize XmlPullParser
		XmlPullParser parser = Xml.newPullParser();

		// Flag for not getting "Site's" title and url
		boolean itemFlag = false;

		long latestDate = dbAdapter.getLatestArticleDate(feedId);
		Log.d(LOG_TAG, "Latest date:" + new Date(latestDate).toString());
		try {
			parser.setInput(is, "UTF-8");

			// Start parse to the END_DOCUMENT
			int eventType = parser.getEventType();
			String tag = parser.getName();
			long itemTime = 0;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_TAG:
					// when new Item found, initialize currentItem
					if (tag.equals("item") || tag.equals("entry")) {
						article = new Article(0, null, null, Article.UNREAD, Article.DEDAULT_HATENA_POINT, 0, 0, null, null);
						itemFlag = true;
						itemTime = System.currentTimeMillis();
					}

					// add Title and Link to currentItem
					if (itemFlag && tag.equals("title")
							&& (article.getTitle() == null)) {
						String title = TextUtil.INSTANCE.removeLineFeed(parser.nextText());
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
						article.setPostedDate(DateParser.INSTANCE.changeToJapaneseDate(date));
					}
					break;

				// When </Item> add currentItem to DB
				case XmlPullParser.END_TAG:
					tag = parser.getName();
					if (tag.equals("item") || tag.equals("entry")) {
						// RSS starts from latest article.
						// So, if latest article date in DB is after parsing article date,
						// it is already saved in DB
						if (latestDate >= article.getPostedDate()) {
							isArticleFlag = true;
						} else {
							articles.add(article);
						}
						itemFlag = false;
						Log.d(LOG_TAG, "One item finished:" + (System.currentTimeMillis() - itemTime));
					}
					break;
				}
				if (!result) {
					return;
				}
				// If article is already saved, stop parse
				if (isArticleFlag) {
					 break;
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
			long now = System.currentTimeMillis();
			dbAdapter.saveNewArticles(articles, feedId);
			Log.d(LOG_TAG, "Finish save, time:" + (System.currentTimeMillis() - now));
            GetHatenaBookmark getHatenaBookmark = new GetHatenaBookmark(dbAdapter);
            int delaySec = 0;
			for (int i = 0; i < articles.size(); i++) {
				getHatenaBookmark.request(articles.get(i).getUrl(), delaySec);
                if (i % 10 == 0) delaySec += 2;
			}
		} catch (XmlPullParserException | IOException e) {
			e.printStackTrace();
		}
	}
}