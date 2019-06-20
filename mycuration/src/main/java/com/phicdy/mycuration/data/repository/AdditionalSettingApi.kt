package com.phicdy.mycuration.data.repository

import java.io.File

interface AdditionalSettingApi {

    suspend fun exportDb(currentDb: File)
    suspend fun importDb(currentDb: File)
    suspend fun addDebugRss()
    suspend fun fixUnreadCount()
}