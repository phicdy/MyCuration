package com.phicdy.mycuration.data.repository

import android.os.Build
import android.os.Environment
import com.phicdy.mycuration.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption


class AdditionalSettingRepository(
    private val rssRepository: RssRepository,
    private val articleRepository: ArticleRepository
) : AdditionalSettingApi {

    /**
     * @return true if succeeded
     */
    override suspend fun exportDb(currentDb: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val sdcardRootPath = FileUtil.sdCardRootPath
            Timber.d("SD Card path: %s", sdcardRootPath)
            val backupStrage =
                if (sdcardRootPath != null && FileUtil.isSDCardMouted(sdcardRootPath)) {
                    Timber.d("SD card is mounted")
                    File(sdcardRootPath)
                } else {
                    Timber.d("not mounted")
                    Environment.getExternalStorageDirectory()
                }
            if (backupStrage.canWrite()) {
                Timber.d("Backup storage is writable")

                val backupDbFolderPath = "$BACKUP_FOLDER/"
                val backupDbFolder = File(backupStrage, backupDbFolderPath)
                if (backupDbFolder.exists()) {
                    if (backupDbFolder.delete()) {
                        Timber.d("Succeeded to delete backup directory")
                    } else {
                        Timber.d("Failed to delete backup directory")
                    }
                }
                if (backupDbFolder.mkdir()) {
                    Timber.d("Succeeded to make directory")
                } else {
                    Timber.d("Failed to make directory")
                }
                val backupDb = File(backupStrage, backupDbFolderPath + DATABASE_NAME)

                // Copy database
                val src = FileInputStream(currentDb).channel
                val dst = FileOutputStream(backupDb).channel
                dst.transferFrom(src, 0, src.size())
                src.close()
                dst.close()

                val walFile = File(currentDb.path + "-wal")
                val backupWal = File(backupStrage, backupDbFolderPath + DATABASE_NAME + "-wal")
                if (walFile.exists()) {
                    val walSrc = FileInputStream(walFile).channel
                    val walDst = FileOutputStream(backupWal).channel
                    walDst.transferFrom(walSrc, 0, walSrc.size())
                    walSrc.close()
                    walDst.close()
                }

                val shmFile = File(currentDb.path + "-shm")
                val backupShm = File(backupStrage, backupDbFolderPath + DATABASE_NAME + "-shm")
                if (shmFile.exists()) {
                    val shmSrc = FileInputStream(shmFile).channel
                    val shmDst = FileOutputStream(backupShm).channel
                    shmDst.transferFrom(shmSrc, 0, shmSrc.size())
                    shmSrc.close()
                    shmDst.close()
                }
                true
            } else {
                // TODO Runtime Permission
                Timber.d("SD Card is not writabble, enable storage permission in Android setting")
                false
            }
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    override suspend fun importDb(currentDb: File, importDb: InputStream) {
        try {
            val backupStrage: File
            val sdcardRootPath = FileUtil.sdCardRootPath
            if (FileUtil.isSDCardMouted(sdcardRootPath!!)) {
                Timber.d("SD card is mounted")
                backupStrage = File(FileUtil.sdCardRootPath!!)
            } else {
                Timber.d("not mounted")
                backupStrage = Environment.getExternalStorageDirectory()
                Timber.d("path:%s", backupStrage.absolutePath)
            }
            if (backupStrage.canRead()) {
                Timber.d("Backup storage is readable")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Files.copy(importDb, currentDb.toPath(), StandardCopyOption.REPLACE_EXISTING)
                } else {
                    return
                }
            } else {
                // TODO Runtime Permission
                Timber.d("SD Card is not readabble, enable storage permission in Android setting")
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override suspend fun addDebugRss() {
        rssRepository.store(
            "Yahoo!ニュース・トピックス - 主要",
            "https://news.yahoo.co.jp/rss/topics/top-picks.xml",
            "RSS2.0",
            "https://news.yahoo.co.jp"
        )
        rssRepository.store(
            "Yahoo!ニュース・トピックス - 国際",
            "https://news.yahoo.co.jp/rss/topics/world.xml",
            "RSS2.0",
            "https://news.yahoo.co.jp"
        )
        rssRepository.store(
            "Yahoo!ニュース・トピックス - エンタメ",
            "https://news.yahoo.co.jp/rss/topics/entertainment.xml",
            "RSS2.0",
            "https://news.yahoo.co.jp"
        )
        rssRepository.store(
            "Yahoo!ニュース・トピックス - IT",
            "https://news.yahoo.co.jp/rss/topics/it.xml",
            "RSS2.0",
            "https://news.yahoo.co.jp"
        )
        rssRepository.store(
            "Yahoo!ニュース・トピックス - 地域",
            "https://news.yahoo.co.jp/rss/topics/local.xml",
            "RSS2.0",
            "https://news.yahoo.co.jp"
        )
    }

    override suspend fun deleteAllArticles() {
        articleRepository.deleteAll()
        rssRepository.getAllFeeds().forEach {
            rssRepository.updateUnreadArticleCount(it.id, 0)
        }
    }

    companion object {
        private const val BACKUP_FOLDER = "filfeed_backup"
        private const val DATABASE_NAME = "rss_manage"
    }
}
