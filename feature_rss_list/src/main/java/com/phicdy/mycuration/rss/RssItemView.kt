package com.phicdy.mycuration.rss

interface RssItemView {
    interface Content {
        fun showDefaultIcon()
        fun showIcon(iconPath: String)
        fun updateTitle(title: String)
        fun updateUnreadCount(count: String)
    }

    interface Footer {
        fun showAllView()
        fun showHideView()
    }

}