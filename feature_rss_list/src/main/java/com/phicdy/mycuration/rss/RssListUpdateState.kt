package com.phicdy.mycuration.rss

import com.phicdy.mycuration.entity.Feed

sealed class RssListUpdateState {
    object Started : RssListUpdateState()
    class Updating(val updated: Feed) : RssListUpdateState()
    object Finished : RssListUpdateState()
    object Failed : RssListUpdateState()
}
