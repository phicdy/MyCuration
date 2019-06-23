package com.phicdy.mycuration.util.test

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.phicdy.mycuration.util.FileUtil
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileUtilTest {

    @Before
    fun setup() {
        FileUtil.setUpAppPath(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testGetIconSavePath() {
        assertEquals(FileUtil.getAppPath(ApplicationProvider.getApplicationContext()) + "icons/", FileUtil.iconSaveFolder())
    }
}
