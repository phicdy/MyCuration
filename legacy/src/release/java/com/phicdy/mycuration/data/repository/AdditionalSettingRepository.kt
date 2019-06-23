package com.phicdy.mycuration.data.repository

import com.phicdy.mycuration.domain.alarm.AlarmManagerTaskManager
import java.io.File

class AdditionalSettingRepository(
        private val rssRepository: RssRepository,
        private val alarmManagerTaskManager: AlarmManagerTaskManager
) : AdditionalSettingApi {
    override suspend fun exportDb(currentDb: File) {
    }

    override suspend fun importDb(currentDb: File) {
    }

    override suspend fun addDebugRss() {
    }

    override suspend fun fixUnreadCount() {
    }
}