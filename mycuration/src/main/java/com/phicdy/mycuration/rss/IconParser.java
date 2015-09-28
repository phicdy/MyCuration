package com.phicdy.mycuration.rss;
 
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
 
public class IconParser {
     
    private static final String LOG_TAG = "FilFeed.IconParser";
     
    public IconParser() {
    }
     
    public String parseHtml(String urlString){
    	if(urlString == null || urlString.equals("")) {
    		return null;
    	}
        Document doc;
		try {
			doc = Jsoup.connect(urlString).get();
			Elements links = doc.getElementsByTag("link");
			for (Element link : links) {
				if(link.attr("rel").equals("shortcut icon") || link.attr("rel").equals("apple-touch-icon")) {
					String href = link.attr("href");
					if(!href.startsWith("http:") && !href.startsWith("https:")) {
						if(urlString.startsWith("http:") ) {
							return "http:" + href; 
						}else if(urlString.startsWith("https:") ) {
							return "https:" + href; 
						}
					}
					return href;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        
        return null;
    }
        
        
     
}