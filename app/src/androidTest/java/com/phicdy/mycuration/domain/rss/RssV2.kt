package com.phicdy.mycuration.domain.rss

class RssV2 {
    fun text(): String {
        return "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<rss xmlns:blogChannel=\"http://backend.userland.com/blogChannelModule\" version=\"2.0\">\n" +
                "<channel>\n" +
                "<title>Yahoo!ニュース・トピックス - 主要</title>\n" +
                "<link>https://news.yahoo.co.jp/</link>\n" +
                "<description>Yahoo! JAPANのニュース・トピックスで取り上げている最新の見出しを提供しています。</description>\n" +
                "<language>ja</language>\n" +
                "<pubDate>Fri, 13 Apr 2018 19:27:17 +0900</pubDate>\n" +
                "<item>\n" +
                "<title>内閣支持率が続落し38% 時事</title>\n" +
                "<link>https://news.yahoo.co.jp/pickup/6278905</link>\n" +
                "<pubDate>Fri, 13 Apr 2018 17:14:37 +0900</pubDate>\n" +
                "<guid isPermaLink=\"false\">yahoo/news/topics/6278905</guid>\n" +
                "</item>\n" +
                "<item>\n" +
                "<title>ハム球場で迷惑行為 県警警戒</title>\n" +
                "<link>https://news.yahoo.co.jp/pickup/6278920</link>\n" +
                "<pubDate>Fri, 13 Apr 2018 18:45:50 +0900</pubDate>\n" +
                "<enclosure length=\"133\" url=\"https://s.yimg.jp/images/icon/photo.gif\" type=\"image/gif\">\n" +
                "</enclosure>\n" +
                "<guid isPermaLink=\"false\">yahoo/news/topics/6278920</guid>\n" +
                "</item>\n" +
                "</channel>\n" +
                "</rss>\n"
    }
}