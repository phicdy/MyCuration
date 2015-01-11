package com.pluea.filfeed.util;


public class UrlUtil {

	public static boolean isCorrectUrl(String url) {
		if(url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
			return true;
		}
		return false;
	}
}
