package com.phicdy.mycuration.data.repository

import java.io.File
import java.io.InputStream

interface AdditionalSettingApi {

    suspend fun exportDb(currentDb: File): Boolean
    suspend fun importDb(currentDb: File, importDb: InputStream)
    suspend fun addDebugRss()

    suspend fun deleteAllArticles()
}