package com.phicdy.mycuration.uitest

import android.content.Context
import android.content.Intent
import android.widget.LinearLayout
import android.widget.ListView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.phicdy.mycuration.BuildConfig
import com.phicdy.mycuration.top.TopActivity
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import junit.framework.Assert.fail
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class FeedUrlHookTest : UiTest() {

    @JvmField
    @Rule
    var activityTestRule = ActivityTestRule(TopActivity::class.java)

    @Before
    fun setup() {
        super.setup(activityTestRule.activity)
    }

    @After
    public override fun tearDown() {
        super.tearDown()
    }

    @Ignore("Skip until it is faxed on CI")
    @Test
    fun addYahooNewsFromDefaultBrowser() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        // Launch default browser
        val context = ApplicationProvider.getApplicationContext<Context>()
        var intent: Intent? = context.packageManager.getLaunchIntentForPackage("com.android.browser")
            ?: return // Default browser is not existed
        intent?.let {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(it)
        }

        // Open feed url
        val urlBar = device.wait(Until.findObject(
            By.res("com.android.browser", "url")), 5000)
        urlBar.click()
        urlBar.text = "https://news.yahoo.co.jp/rss/topics/top-picks.xml"
        device.pressEnter()

        // Open share menu
        val settingButton = device.wait(
            Until.findObject(By.res("com.android.browser", "more")), 5000)
        assertNotNull("Setting button was not found", settingButton)
        settingButton.click()
        val shareMenu = device.wait(
            Until.findObject(By.text("ページを共有")), 5000)
        assertNotNull("Share menu was not found", shareMenu)
        shareMenu.click()

        // Click app icon
        val shareList = device.wait(
            Until.findObject(By.clazz(ListView::class.java)), 5000)
        assertNotNull("Share app list was not found", shareList)
        val shareApps = shareList!!.wait(
            Until.findObjects(By.clazz(LinearLayout::class.java).depth(2)), 5000)
        for (shareApp in shareApps) {
            val label = shareApp.findObject(By.res("android", "text1"))
            if (label.text == "RSS登録") {
                shareApp.click()
                break
            }
        }

        // Wait for adding feed and go back to the browser
        device.wait(Until.findObject(
            By.res("com.android.browser", "url")), 5000)

        // Close the browser
        device.pressBack()
        device.pressBack()

        // Launch MainActivity
        intent = context.packageManager.getLaunchIntentForPackage(BuildConfig.APPLICATION_ID)
        intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)

        // Go to feed tab
        val tabs = device.wait(Until.findObjects(
            By.clazz(androidx.appcompat.app.ActionBar.Tab::class.java)), 5000)
        assertNotNull("Tab was not found", tabs)
        if (tabs.size != 3) fail("Tab size was invalid, size: " + tabs.size)
        tabs[1].click()

        // Assert yahoo RSS was added
        val feedTitles = device.wait(Until.findObjects(
            By.res(BuildConfig.APPLICATION_ID, "feedTitle")), 5000)
        assertNotNull("Feed was not found", feedTitles)
        // Feed title list includes show/hide option row, the size is 2
        if (feedTitles.size != 2) fail("Feed was not added")
        assertThat(feedTitles[0].text, `is`("Yahoo!ニュース・トピックス - 主要"))
        assertThat(feedTitles[1].text, `is`("全てのRSSを表示"))

        // Assert articles of yahoo RSS were added
        val feedUnreadCountList = device.wait(Until.findObjects(
            By.res(BuildConfig.APPLICATION_ID, "feedCount")), 5000)
        assertNotNull("Feed count was not found", feedUnreadCountList)
        // Feed count list does not include show/hide option row, the size is 1
        if (feedUnreadCountList.size != 1) fail("Feed count was not added")
        assertTrue(Integer.valueOf(feedUnreadCountList[0].text) >= 0)
    }
}
