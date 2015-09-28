package com.phicdy.mycuration.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UrlUtil {

	public static boolean hasParameterUrl(String url) {
		String regex = "^(http|https):\\/\\/.+\\/\\?.+$";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(url);
		if ((url != null) && m.find()) {
			return true;
		}
		return false;
	}
	
	public static String removeUrlParameter(String url) {
		if (!isCorrectUrl(url)) {
			return url;
		}
		if (!url.contains("?")) {
			return url;
		}
		return url.substring(0, url.indexOf("?"));
	}
	
	public static boolean isCorrectUrl(String url) {
		if(url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
			return true;
		}
		return false;
	}
}
