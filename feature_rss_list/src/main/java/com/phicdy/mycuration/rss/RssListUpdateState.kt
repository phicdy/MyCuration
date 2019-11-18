package com.phicdy.mycuration.rss

sealed class RssListUpdateState {
    object Started : RssListUpdateState()
    class Updating(val rss: List<RssListItem>) : RssListUpdateState()
    object Finished : RssListUpdateState()
    object Failed : RssListUpdateState()
}
