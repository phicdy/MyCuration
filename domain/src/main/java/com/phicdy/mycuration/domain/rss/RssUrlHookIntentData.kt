package com.phicdy.mycuration.domain.rss

data class RssUrlHookIntentData(
        val action: String,
        val dataString: String,
        val extrasText: CharSequence
)