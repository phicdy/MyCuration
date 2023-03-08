package com.phicdy.mycuration.uitest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasImeAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.input.ImeAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.phicdy.mycuration.BuildConfig
import com.phicdy.mycuration.top.TopActivity
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class EditFeedTitleTest : UiTest() {

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

    @Ignore("Skip until it is faxed on CI")
    @Test
    fun editYahooNewsTitle() {
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

        // Open yahoo RSS URL
        val urlEditText = device.wait(
            Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_src_text")
            ), 5000
        )
        assertNotNull("URL edit text was not found", urlEditText)
        val url = "https://news.yahoo.co.jp/rss/topics/top-picks.xml"
        urlEditText.text = url
        device.pressEnter()
        device.wait(Until.gone(By.text("RSSを追加しています。")), 5000)

        // Assert yahoo RSS was added
        val titleNode = activityTestRule.onNodeWithText("Yahoo!ニュース・トピックス - 主要")
        titleNode.assertIsDisplayed()
        activityTestRule.onNodeWithText("全てのRSSを表示").assertIsDisplayed()

        // Edit title
        titleNode.performTouchInput { longClick() }
        activityTestRule.onNodeWithText("RSSのタイトルを編集").performClick()
        val editTextNode = activityTestRule.onNode(hasImeAction(ImeAction.Default))
        editTextNode.assertIsDisplayed()
        editTextNode.performTextClearance()
        editTextNode.performTextInput("test")

        activityTestRule.onNodeWithText("保存", ignoreCase = true).performClick()

        activityTestRule.onNode(isDialog()).assertDoesNotExist()

        // Check edited title
        activityTestRule.onNodeWithText("test").assertIsDisplayed()
    }
}
