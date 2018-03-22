package com.phicdy.mycuration.domain.task

import android.os.AsyncTask

import com.phicdy.mycuration.db.DatabaseAdapter
import com.phicdy.mycuration.rss.IconParser
import com.phicdy.mycuration.util.FileUtil

import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class GetFeedIconTask internal constructor(private val iconSaveDir: String) : AsyncTask<String, Void, Void>() {

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
        val iconUrlStr = parser.parseHtml(siteUrl) ?: return

        var dataInStream: DataInputStream? = null
        var dataOutStream: DataOutputStream? = null
        try {
            val conn = URL(iconUrlStr).openConnection() as HttpURLConnection
            conn.allowUserInteraction = false
            conn.instanceFollowRedirects = true
            conn.requestMethod = "GET"
            conn.connect()

            val httpStatusCode = conn.responseCode
            if (httpStatusCode != HttpURLConnection.HTTP_OK) return
            dataInStream = DataInputStream(conn.inputStream)
            val iconSaveFolder = File(iconSaveDir)
            if (!iconSaveFolder.exists()) {
                if (!iconSaveFolder.mkdir()) {
                    return
                }
            }

            val iconPath = FileUtil.generateIconFilePath(iconSaveDir, siteUrl) ?: return
            dataOutStream = DataOutputStream(BufferedOutputStream(FileOutputStream(iconPath)))
            dataInStream.use {
                it.copyTo(dataOutStream)
            }

            val dbAdapter = DatabaseAdapter.getInstance()
            dbAdapter.saveIconPath(siteUrl, iconPath)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (dataInStream != null) {
                try {
                    dataInStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (dataOutStream != null) {
                try {
                    dataOutStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }
}
