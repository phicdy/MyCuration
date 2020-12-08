package com.phicdy.mycuration.rss

import com.phicdy.mycuration.entity.Feed

sealed class RssListUpdateState {
    object Started : RssListUpdateState()
    data class Finished(val updated: List<Feed>) : RssListUpdateState()
    object Failed : RssListUpdateState()
}
