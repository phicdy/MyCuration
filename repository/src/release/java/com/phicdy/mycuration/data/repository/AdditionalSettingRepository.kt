package com.phicdy.mycuration.data.repository

import java.io.File
import java.io.InputStream

class AdditionalSettingRepository(
    private val rssRepository: RssRepository,
    private val articleRepository: ArticleRepository
) : AdditionalSettingApi {
    override suspend fun exportDb(currentDb: File): Boolean = false

    override suspend fun importDb(currentDb: File, importDb: InputStream) {
    }

    override suspend fun addDebugRss() {
    }

    override suspend fun deleteAllArticles() {
    }
}