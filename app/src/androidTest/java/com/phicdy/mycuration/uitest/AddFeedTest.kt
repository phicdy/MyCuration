package com.phicdy.mycuration.uitest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.phicdy.mycuration.BuildConfig
import com.phicdy.mycuration.top.TopActivity
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class AddFeedTest : UiTest() {

    @get:Rule
    var activityTestRule = createAndroidComposeRule<TopActivity>()

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
        addAndCheckUrl(
            "https://news.yahoo.co.jp/rss/topics/top-picks.xml",
            "Yahoo!ニュース・トピックス - 主要"
        )
    }

    @Test
    fun addYamBlog() {
        // Atom
        addAndCheckUrl("http://y-anz-m.blogspot.com/feeds/comments/default", "Y.A.M の 雑記帳")
    }

    private fun addAndCheckUrl(url: String, title: String) {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        TopActivityControl.goToRssTab()
        TopActivityControl.clickAddRssButton()

        // Show edit text for URL if needed
        val searchButton = device.wait(
            Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_button")
            ), 5000
        )
        searchButton?.click()

        // Open RSS URL
        val urlEditText = device.wait(
            Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_src_text")
            ), 5000
        )
        assertNotNull("URL edit text was not found", urlEditText)
        urlEditText.text = url

        device.pressEnter()
        Thread.sleep(5000)

        // Assert RSS was added
        activityTestRule.onNodeWithText(title).assertIsDisplayed()
        activityTestRule.onNodeWithText("全てのRSSを表示").assertIsDisplayed()

        device.pressBack()
    }

    @Test
    fun tryInvalidUrl() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        takeScreenshot(device, "start_of_tryInvalidUrl_" + System.currentTimeMillis())

        device.wait(Until.findObject(By.res(BuildConfig.APPLICATION_ID, "add")), 5000)
        takeScreenshot(device, "after_startActivity_" + System.currentTimeMillis())

        TopActivityControl.goToRssTab()

        // Get current RSS size
        var rssList: UiObject2? = device.findObject(By.res(BuildConfig.APPLICATION_ID, "rv_rss"))
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
                By.res(BuildConfig.APPLICATION_ID, "rv_rss")), 5000)
        if (numOfRss == 0) {
            assertNull(rssList)
        } else {
            assertThat(rssList!!.childCount, `is`(numOfRss))
        }
    }

    @Test
    fun clickFabWithoutUrlOpen() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        TopActivityControl.goToRssTab()
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
