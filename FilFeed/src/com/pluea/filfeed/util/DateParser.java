package com.pluea.filfeed.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class DateParser {

	private static final String LOG_TAG = "DateParser";

	private static Date parseDate(String pubDate) {
		DateFormat input = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",
				Locale.US);
		Date formatWithPubDate = null;
		try {
			formatWithPubDate = input.parse(pubDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(formatWithPubDate == null) {
			//2014-06-25 17:24:07
			// TODO: set device locale
			DateFormat noTimezone = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					Locale.JAPAN);
			Date formatWithNoTimeZone = null;
			try {
				formatWithNoTimeZone = noTimezone.parse(pubDate);
			} catch (ParseException e2) {
				e2.printStackTrace();
			}
			if(formatWithNoTimeZone == null) {
				//2014-07-27T14:38:34+09:00
				if(pubDate.contains("T")) {
					String replaced = pubDate.replace("T", " ");
					if(pubDate.contains("Z")) {
						//2014-07-27T14:38:34Z
						replaced = replaced.replace("Z", "+09:00");
					}
					
					DateFormat w3cdtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ",
							Locale.US);
					Date formatWithW3cdtf = null;
					try {
						formatWithW3cdtf = w3cdtf.parse(replaced);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					if(formatWithW3cdtf == null) {
						return null;
					}else {
						return formatWithW3cdtf;
					}
				}
			}else {
				return formatWithNoTimeZone;
			}
		}else {
			return formatWithPubDate;
		}
		return null;
	}

	public static long changeToJapaneseDate(String dateBeforeChange) {
		Log.d(LOG_TAG, "date before change:" + dateBeforeChange);
		Calendar cal = Calendar.getInstance();
		Date date = parseDate(dateBeforeChange);
		if(date == null) {
			return 0;
		}
		Log.d(LOG_TAG, date.toString());
		cal.setTime(date);

		return cal.getTimeInMillis();
	}
}
