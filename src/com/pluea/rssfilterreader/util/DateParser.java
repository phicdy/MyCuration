package com.pluea.rssfilterreader.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class DateParser {

	private static final String LOG_TAG = "DateParser";

	private static Date getPubDate(String pubDate) {
		DateFormat input = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",
				Locale.US);
		try {
			return input.parse(pubDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static long changeToJapaneseDate(String dateBeforeChange) {
		Log.d(LOG_TAG, "date before change:" + dateBeforeChange);
		Calendar cal = Calendar.getInstance();
		cal.setTime(getPubDate(dateBeforeChange));

		return cal.getTimeInMillis();
	}
}
