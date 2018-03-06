package com.phicdy.mycuration.rss;
 
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;

public class IconParser {
     
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
						URL url = new URL(urlString);
						// The link is //<Host>/<path>
						if (href.startsWith("//")) {
							return url.getProtocol() + ":" + href;
						}
						return new URL(url.getProtocol(), url.getHost(), href).toString();
					}
					return href;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

        return null;
    }
        
        
     
}