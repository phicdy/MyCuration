package com.pluea.filfeed.rss;

import com.pluea.filfeed.R;

import android.content.Context;
import android.widget.Toast;

public class ParseError {

	public static final int NOT_ERROR = -1;
	public static final int ERROR_XML_PARSE = 0;
	public static final int ERROR_IO = 1;
	public static final int ERROR_FEED_IS_NOT_FOUND = 2;
	public static final int ERROR_INVALID_RSS_URL = 3;
	public static final int ERROR_INVALID_HTML = 4;
	
	public static void showErrorToast(Context context, int errorCode) {
		switch (errorCode) {
		case ERROR_XML_PARSE:
			Toast.makeText(context, R.string.error_xml_parse, Toast.LENGTH_SHORT).show();
			break;
		case ERROR_IO:
			Toast.makeText(context, R.string.error_io, Toast.LENGTH_SHORT).show();
			break;
		case ERROR_FEED_IS_NOT_FOUND:
			Toast.makeText(context, R.string.error_rss_is_not_found, Toast.LENGTH_SHORT).show();
			break;
		case ERROR_INVALID_RSS_URL:
			Toast.makeText(context, R.string.error_invalid_rss_url, Toast.LENGTH_SHORT).show();
			break;
		case ERROR_INVALID_HTML:
			Toast.makeText(context, R.string.error_invalid_html, Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
	}
}
