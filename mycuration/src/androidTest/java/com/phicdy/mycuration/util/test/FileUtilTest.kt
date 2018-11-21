package com.phicdy.mycuration.util.test

import android.support.test.runner.AndroidJUnit4

import com.phicdy.mycuration.util.FileUtil

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import android.support.test.InstrumentationRegistry.getTargetContext
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class FileUtilTest {

    @Before
    fun setup() {
        FileUtil.setUpIconSaveFolder(getTargetContext())
    }

    @Test
    fun testGetIconSavePath() {
        assertEquals(FileUtil.getAppPath(getTargetContext()) + "icons/", FileUtil.iconSaveFolder())
    }
}
