package com.phicdy.mycuration.util

import java.util.regex.Pattern


object UrlUtil {

    fun hasParameterUrl(url: String): Boolean {
        val regex = "^(http|https):\\/\\/.+\\/\\?.+$"
        val p = Pattern.compile(regex)
        val m = p.matcher(url)
        return m.find()
    }

    fun removeUrlParameter(url: String): String? {
        if (!isCorrectUrl(url)) {
            return url
        }
        return if (!url.contains("?")) {
            url
        } else url.substring(0, url.indexOf("?"))
    }

    fun isCorrectUrl(url: String?): Boolean {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"))
    }
}
