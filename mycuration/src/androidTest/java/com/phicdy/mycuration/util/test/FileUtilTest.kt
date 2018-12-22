package com.phicdy.mycuration.util.test

import androidx.test.runner.AndroidJUnit4

import com.phicdy.mycuration.util.FileUtil

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import androidx.test.InstrumentationRegistry.getTargetContext
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class FileUtilTest {

    @Before
    fun setup() {
        FileUtil.setUpAppPath(getTargetContext())
    }

    @Test
    fun testGetIconSavePath() {
        assertEquals(FileUtil.getAppPath(getTargetContext()) + "icons/", FileUtil.iconSaveFolder())
    }
}
