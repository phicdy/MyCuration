package com.pleua.rssfilterreader.rss;
 
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.Xml;
import android.widget.Toast;

import com.example.rssfilterreader.R;
import com.pluea.rssfilterreader.db.DatabaseAdapter;
import com.pluea.rssfilterreader.util.DateParser;
 
public class RssParser {
     
    private DatabaseAdapter dbAdapter;
    private static final int CONNCT_TIMEOUT_MS = 20000;
    private static final int READ_TIMEOUT_MS = 60000;
    private static Context context_;
    private boolean isArticleFlag = false;
    private static final String LOG_TAG = "RSSReader.RssParser";
     
    public RssParser(Context context) {
        dbAdapter = new DatabaseAdapter(context);
        context_ = context;
    }
     
    public boolean parseXml(String urlString, int feedId) throws IOException{
    	InputStream is = setInputStream(urlString);
        if(is == null) {
        	return false;
        }
    	
        boolean result = true;
        ArrayList<Article> articles = new ArrayList<Article>();
         
        //TODO Get hatena bookmark(?) count
        Article article = new Article(0, null, null, null, "10",0, 0);
         
        //Initialize XmlPullParser
        XmlPullParser parser = Xml.newPullParser();
         
        //Flag for not getting "Site's" title and url
        boolean itemFlag = false;
         
        try {
            parser.setInput(is, "UTF-8");
             
            //Start parse to the END_DOCUMENT
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tag = null;
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();
                        //when new Item found, initialize currentItem
                        if(tag.equals("item")) {
                            article  = new  Article(0, null, null, null, "10", 0, 0);
                            itemFlag = true;
                        }
                         
                        //add Title and Link to currentItem
                        if(itemFlag && tag.equals("title") && (article.getTitle() == null)) {
                            article.setTitle(parser.nextText());
                        }
                        if(itemFlag && tag.equals("link") && (article.getUrl() == null)) {
                            article.setUrl(parser.nextText());
                        }
                        if(itemFlag && (tag.equals("date") || tag.equals("pubDate")) && (article.getPostedDate() == 0)) {
                            article.setPostedDate(DateParser.changeToJapaneseDate(parser.nextText()));
                        }
                        break;
                     
                    //When </Item> add currentItem to DB
                    case XmlPullParser.END_TAG:
                        tag = parser.getName();
                        if (tag.equals("item")) {
                        	if(dbAdapter.isArticle(article)) {
                        		isArticleFlag = true;
                        	}else {
                        		articles.add(article);
                        	}
                            itemFlag = false;
                        }
                        break;
                }
                if(!result) {
                    return false;
                }
                // If article is already saved, stop parse
                if(isArticleFlag) {
                	break;
                }
                eventType = parser.next();
                tag = parser.getName();
                if(eventType == XmlPullParser.END_TAG && (tag.equals("rss") || tag.equals("rdf"))) {
                	break;
                }
            }
            //Save new articles
            dbAdapter.saveNewArticles(articles, feedId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    private InputStream setInputStream(String urlString) throws IOException {
    	//Set URLConnection
        URL url = new URL(urlString);
        try {
	        URLConnection con = url.openConnection();
	        //Set Timeout 1min
	        con.setConnectTimeout(CONNCT_TIMEOUT_MS);
	        con.setReadTimeout(READ_TIMEOUT_MS);
	          
	        //Get InputStream
	        return con.getInputStream();
        }catch(IOException e) {
        	e.printStackTrace();
        }
        return null;
    }
     
}