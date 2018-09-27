package com.phicdy.mycuration.presentation.view

interface RssItemView {
    interface Content {
        fun showDefaultIcon()
        fun showIcon(iconPath: String): Boolean
        fun updateTitle(title: String)
        fun updateUnreadCount(count: String)
    }

    interface Footer {
        fun showAllView()
        fun showHideView()
    }

}
