package com.phicdy.mycuration.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import android.util.Log

object DateParser {

	private val LOG_TAG = "DateParser"

	private fun parseDate(pubDate: String): Date? {
		val input = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",
				Locale.US)
		var formatWithPubDate: Date? = null
		try {
			formatWithPubDate = input.parse(pubDate)
		} catch (e: ParseException) {
			e.printStackTrace()
		}

		if (formatWithPubDate == null) {
			//2014-06-25 17:24:07
			// TODO: set device locale
			val noTimezone = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					Locale.JAPAN)
			var formatWithNoTimeZone: Date? = null
			try {
				formatWithNoTimeZone = noTimezone.parse(pubDate)
			} catch (e2: ParseException) {
				e2.printStackTrace()
			}

			if (formatWithNoTimeZone == null) {
				//2014-07-27T14:38:34+09:00
				if (pubDate.contains("T")) {
					var replaced = pubDate.replace("T", " ")
					if (pubDate.contains("Z")) {
						//2014-07-27T14:38:34Z
						replaced = replaced.replace("Z", "+09:00")
					}

					val w3cdtf = SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ",
							Locale.US)
					var formatWithW3cdtf: Date? = null
					try {
						formatWithW3cdtf = w3cdtf.parse(replaced)
					} catch (e: ParseException) {
						e.printStackTrace()
					}

					return if (formatWithW3cdtf == null) {
						null
					} else {
						formatWithW3cdtf
					}
				}
			} else {
				return formatWithNoTimeZone
			}
		} else {
			return formatWithPubDate
		}
		return null
	}

	fun changeToJapaneseDate(dateBeforeChange: String): Long {
		Log.d(LOG_TAG, "date before change:" + dateBeforeChange)
		val cal = Calendar.getInstance()
		val date = parseDate(dateBeforeChange) ?: return 0
		Log.d(LOG_TAG, date.toString())
		cal.time = date

		return cal.timeInMillis
	}
}
