package com.phicdy.mycuration.domain.task

import android.support.test.InstrumentationRegistry.getTargetContext
import android.support.test.runner.AndroidJUnit4
import com.phicdy.mycuration.util.FileUtil
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class GetFeedIconTaskTest {

    @Before
    fun setup() {
        FileUtil.setUpIconSaveFolder(getTargetContext())
    }

    @After
    fun tearDown() {
        val file = File(FileUtil.iconSaveFolder() + "/kindou.info.png")
        if (file.exists()) file.delete()
        val greeBlogIcon = File(FileUtil.iconSaveFolder() + "/labs.gree.jp.png")
        if (greeBlogIcon.exists()) greeBlogIcon.delete()
    }

    @Test
    fun iconExistsWhenGetKindouIcon() {
        var file = File(FileUtil.iconSaveFolder() + "/kindou.info.png")
        if (file.exists()) assertTrue(file.delete())
        val iconSaveFolderStr = FileUtil.iconSaveFolder()
        val task = GetFeedIconTask(iconSaveFolderStr)
        task.execute("http://kindou.info")
        Thread.sleep(3000)

        file = File(FileUtil.iconSaveFolder() + "/kindou.info.png")
        assertTrue(file.exists())
    }

    @Test
    fun iconDoesNotExistWhenGetGreeBlogIcon() {
        var greeBlogIcon = File(FileUtil.iconSaveFolder() + "/labs.gree.jp.png")
        if (greeBlogIcon.exists()) assertTrue(greeBlogIcon.delete())
        val iconSaveFolderStr = FileUtil.iconSaveFolder()
        val greeBlogIconTask = GetFeedIconTask(iconSaveFolderStr)
        greeBlogIconTask.execute("http://labs.gree.jp/blog")
        Thread.sleep(3000)

        greeBlogIcon = File(FileUtil.iconSaveFolder() + "/labs.gree.jp.png")
        assertFalse(greeBlogIcon.exists())
    }
}
