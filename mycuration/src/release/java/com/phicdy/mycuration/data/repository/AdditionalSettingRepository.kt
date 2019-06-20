package com.phicdy.mycuration.data.repository

import java.io.File

class AdditionalSettingRepository(private val rssRepository: RssRepository) : AdditionalSettingApi {
    override suspend fun exportDb(currentDb: File) {
    }

    override suspend fun importDb(currentDb: File) {
    }

    override suspend fun addDebugRss() {
    }

    override suspend fun fixUnreadCount() {
    }
}