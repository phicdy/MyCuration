package com.phicdy.mycuration.util

import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateParser {

	private const val LOG_TAG = "DateParser"

	private fun parseDate(pubDate: String): Date? {
		val input = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)
		var formatWithPubDate: Date? = null
		try {
			formatWithPubDate = input.parse(pubDate)
            return formatWithPubDate
		} catch (e: ParseException) {
		}

        //2014-06-25 17:24:07
        // TODO: set device locale
        val noTimezone = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN)
        var formatWithNoTimeZone: Date? = null
        try {
            formatWithNoTimeZone = noTimezone.parse(pubDate)
            return formatWithNoTimeZone
        } catch (e2: ParseException) {
        }

        //2014-07-27T14:38:34+0900
        if (!pubDate.contains("T")) {
            Log.d(LOG_TAG, "Invalid format, return null")
            return null
        }
        var replaced = pubDate.replace("T", " ")
        if (pubDate.contains("Z")) {
            //2014-07-27T14:38:34Z
            replaced = replaced.replace("Z", "+0900")
        }

        // Delete millisecond, 2018-04-12T22:38:00.000+09:00 to 2018-04-12T22:38:00+09:00
        val regexMillisecond = "([0-9][0-9]:[0-9][0-9]:[0-9][0-9])\\.[0-9][0-9][0-9]".toRegex()
        if (regexMillisecond.containsMatchIn(replaced)) {
            replaced = replaced.replace(regexMillisecond, "$1")
        }

        // Delete timezone with colon, 2014-07-27T14:38:34+09:00 to 2014-07-27T14:38:34+0900
        val regexTimezoneWithColon = "\\+([0-9][0-9]):([0-9][0-9])".toRegex()
        if (regexTimezoneWithColon.containsMatchIn(replaced)) {
            replaced = replaced.replace(regexTimezoneWithColon, "+$1$2")
        }

        val w3cdtf = SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.US)
        var formatWithW3cdtf: Date? = null
        try {
            formatWithW3cdtf = w3cdtf.parse(replaced)
            return formatWithW3cdtf
        } catch (e: ParseException) {
        }

        Log.d(LOG_TAG, "Contains T, but failed to parse")
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
