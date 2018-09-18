package com.phicdy.mycuration.uitest

import android.support.test.InstrumentationRegistry
import android.support.test.filters.SdkSuppress
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.By
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiObject2
import android.support.test.uiautomator.Until

import com.phicdy.mycuration.BuildConfig
import com.phicdy.mycuration.presentation.view.activity.TopActivity

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import junit.framework.Assert.assertTrue
import junit.framework.Assert.fail
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class AddFeedTest : UiTest() {

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

    @Test
    fun addYahooNews() {
        // RSS 2.0
        addAndCheckUrl("http://news.yahoo.co.jp/pickup/rss.xml", "Yahoo!ニュース・トピックス - 主要")
    }

    @Test
    fun addYamBlog() {
        // Atom
        addAndCheckUrl("http://y-anz-m.blogspot.com/feeds/comments/default", "Y.A.M の 雑記帳")
    }

    private fun addAndCheckUrl(url: String, title: String) {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Go to feed tab
        val tabs = device.wait(Until.findObjects(
                By.clazz(android.support.v7.app.ActionBar.Tab::class.java)), 15000)
        assertNotNull("Tab was not found", tabs)
        if (tabs.size != 3) fail("Tab size was invalid, size: $tabs.size")
        tabs[1].click()

        TopActivityControl.clickAddRssButton()

        // Show edit text for URL if needed
        val searchButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_button")), 5000)
        searchButton?.click()

        // Open RSS URL
        val urlEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_src_text")), 5000)
        assertNotNull("URL edit text was not found", urlEditText)
        urlEditText.text = url

        device.pressEnter()
        Thread.sleep(5000)

        // Assert RSS was added
        val feedTitles = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedTitle")), 5000)
        assertNotNull("Feed was not found", feedTitles)
        // Feed title list includes show/hide option row, the size is 2
        if (feedTitles.size != 2) fail("Feed was not added")
        assertThat(feedTitles[0].text, `is`(title))
        assertThat(feedTitles[1].text, `is`("全てのRSSを表示"))

        // Assert articles of RSS were added
        val feedUnreadCountList = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedCount")), 5000)
        assertNotNull("Feed count was not found", feedUnreadCountList)
        // Feed count list does not include show/hide option row, the size is 1
        if (feedUnreadCountList.size != 1) fail("Feed count was not added")
        assertTrue(Integer.valueOf(feedUnreadCountList[0].text) >= -1)

        // Assert all article view shows
        val allArticleView = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "ll_all_unread")), 5000)
        assertNotNull(allArticleView)
        device.pressBack()
    }

    @Test
    fun tryInvalidUrl() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        takeScreenshot(device, "start_of_tryInvalidUrl_" + System.currentTimeMillis())

        device.wait(Until.findObject(By.res(BuildConfig.APPLICATION_ID, "add")), 5000)
        takeScreenshot(device, "after_startActivity_" + System.currentTimeMillis())

        // Go to feed tab
        val tabs = device.findObjects(
                By.clazz(android.support.v7.app.ActionBar.Tab::class.java))
        takeScreenshot(device, "main_ui_before_click_feed_tab_" + System.currentTimeMillis())
        assertNotNull("Tab was not found", tabs)
        if (tabs.size != 3) fail("Tab size was invalid, size: " + tabs.size)
        tabs[1].click()

        // Get current RSS size
        var rssList: UiObject2? = device.findObject(By.res(BuildConfig.APPLICATION_ID, "feedList"))
        var numOfRss = 0
        if (rssList != null) {
            numOfRss = rssList.childCount
        }

        TopActivityControl.clickAddRssButton()

        // Show edit text for URL if needed
        val searchButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_button")), 5000)
        searchButton?.click()

        // Open invalid RSS URL
        val urlEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_src_text")), 5000)
        assertNotNull("URL edit text was not found", urlEditText)
        urlEditText.text = "http://ghaorgja.co.jp/rss.xml"
        device.pressEnter()

        rssList = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "feedList")), 5000)
        if (numOfRss == 0) {
            assertNull(rssList)
        } else {
            assertThat(rssList!!.childCount, `is`(numOfRss))
        }
    }

    @Test
    fun clickFabWithoutUrlOpen() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Go to feed tab
        val tabs = device.wait(Until.findObjects(
                By.clazz(android.support.v7.app.ActionBar.Tab::class.java)), 15000)
        assertNotNull("Tab was not found", tabs)
        if (tabs.size != 3) fail("Tab size was invalid, size: " + tabs.size)
        tabs[1].click()

        TopActivityControl.clickAddRssButton()

        // Open invalid RSS URL
        var fab = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "fab")), 5000)
        assertNotNull("Fab was not found")
        fab.click()

        // Fab still exists
        fab = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "fab")), 5000)
        assertNotNull(fab)
    }

}
