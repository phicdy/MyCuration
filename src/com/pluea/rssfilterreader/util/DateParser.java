package com.pluea.rssfilterreader.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
			//2014-06-25 17:24:07
			DateFormat noTimezone = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					Locale.JAPAN);
			try {
				return noTimezone.parse(pubDate);
			} catch (ParseException e2) {
				e2.printStackTrace();
			}
		}
		return null;
	}

	public static long changeToJapaneseDate(String dateBeforeChange) {
		Log.d(LOG_TAG, "date before change:" + dateBeforeChange);
		Calendar cal = Calendar.getInstance();
		Date date = getPubDate(dateBeforeChange);
		Log.d(LOG_TAG, date.toString());
		cal.setTime(date);

		return cal.getTimeInMillis();
	}
}
