package com.phicdy.mycuration.util

import java.util.regex.Pattern

object TextUtil {

    fun removeLineFeed(string: String): String {
        return Pattern.compile("\t|\r|\n|\r\n").matcher(string).replaceAll("")
    }

    fun isEmpty(text: String?): Boolean {
        return text == null || text == ""
    }
}
