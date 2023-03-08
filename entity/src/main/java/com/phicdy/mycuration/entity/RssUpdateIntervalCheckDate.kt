package com.phicdy.mycuration.entity

import java.util.Date

data class RssUpdateIntervalCheckDate(
        private val now: Date
) {
    fun toTime() = now.time
}
