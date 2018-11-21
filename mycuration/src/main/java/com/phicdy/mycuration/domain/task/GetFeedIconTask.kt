package com.phicdy.mycuration.domain.task

import android.os.AsyncTask
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.domain.rss.IconParser

class GetFeedIconTask : AsyncTask<String, Void, Void>() {

    /**
     * Get articles from RSS Feed
     *
     */
    override fun doInBackground(vararg url: String): Void? {
        getFeedIcon(url[0])
        return null
    }

    private fun getFeedIcon(siteUrl: String) {
        val parser = IconParser()
        val iconUrlStr = parser.parseHtml(siteUrl)
        if (iconUrlStr.isBlank()) return
        val dbAdapter = DatabaseAdapter.getInstance()
        dbAdapter.saveIconPath(siteUrl, iconUrlStr)
    }
}
