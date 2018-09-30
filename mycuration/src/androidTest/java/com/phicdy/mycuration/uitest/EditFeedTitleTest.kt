package com.phicdy.mycuration.uitest

import android.support.test.InstrumentationRegistry
import android.support.test.filters.SdkSuppress
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.By
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.Until
import com.phicdy.mycuration.BuildConfig
import com.phicdy.mycuration.presentation.view.activity.TopActivity
import junit.framework.Assert.fail
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class EditFeedTitleTest : UiTest() {

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
    fun editYahooNewsTitle() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        TopActivityControl.goToRssTab()
        TopActivityControl.clickAddRssButton()

        // Show edit text for URL if needed
        val searchButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_button")), 5000)
        searchButton?.click()

        // Open yahoo RSS URL
        val urlEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_src_text")), 5000)
        assertNotNull("URL edit text was not found", urlEditText )
        val url = "http://news.yahoo.co.jp/pickup/rss.xml"
        urlEditText.text = url
        device.pressEnter()
        device.wait(Until.gone(By.text("RSSを追加しています。")), 5000)

        // Assert yahoo RSS was added
        var feedTitles = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedTitle")), 5000)
        assertNotNull("Feed was not found", feedTitles )
        if (feedTitles.size != 1) fail("Feed was not added")
        assertThat(feedTitles[0].text, `is`("Yahoo!ニュース・トピックス - 主要"))
        val footerTitle = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "tv_rss_footer_title")), 5000)
        assertThat(footerTitle.text, `is`("全てのRSSを表示"))

        // Edit title
        val filterRect = feedTitles[0].visibleBounds
        device.swipe(filterRect.centerX(), filterRect.centerY(),
                filterRect.centerX(), filterRect.centerY(), 100)
        val edit = device.wait(Until.findObject(By.text("RSSのタイトルを編集")), 5000)
        assertNotNull("Edit menu was not found", edit)
        edit.click()
        val currentTitle = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "editFeedTitle")), 3000)
        assertNotNull("Current title was not found", currentTitle)
        assertThat(currentTitle.text, `is`("Yahoo!ニュース・トピックス - 主要"))
        currentTitle.text = "test"
        val saveButton = device.findObject(By.res("android:id/button1"))
        assertNotNull("Save button was not found", saveButton)
        saveButton.click()

        // Check edited title
        feedTitles = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedTitle")), 5000)
        assertNotNull("Feed was not found", feedTitles)
        if (feedTitles.size != 1) fail("Feed was not added")
        assertThat(feedTitles[0].text, `is`("test"))
    }
}
