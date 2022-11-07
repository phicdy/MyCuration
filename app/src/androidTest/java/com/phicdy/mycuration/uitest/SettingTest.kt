package com.phicdy.mycuration.uitest

import android.webkit.WebView
import android.widget.LinearLayout
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.phicdy.mycuration.BuildConfig
import com.phicdy.mycuration.top.TopActivity
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class SettingTest : UiTest() {

    @get:Rule
    var activityTestRule = createAndroidComposeRule<TopActivity>()


    @Before
    fun setup() {
        super.setup(activityTestRule.activity)

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.wait(Until.findObject(By.res(BuildConfig.APPLICATION_ID, "add")), 5000)

    }

    @After
    public override fun tearDown() {
        super.tearDown()
    }

    @Ignore("TODO: Initialize Chrome")
    @Test
    fun openWithInternalBrowser() {
        addYahoo()
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        TopActivityControl.goToSetting()

        // Enable internal browser
        val settingsList = device.findObject(By.clazz(androidx.recyclerview.widget.RecyclerView::class.java))
        val settings = settingsList.findObjects(By.clazz(LinearLayout::class.java).depth(1))
        for (setting in settings) {
            val text = setting.findObject(By.res("android:id/title"))
            if (text.text == "内蔵ブラウザで開く") {
                var browserSwitch: UiObject2? = setting.findObject(
                    By.res("android:id/switch_widget"))
                if (browserSwitch == null)
                    browserSwitch = setting.findObject(
                        By.res("android:id/switchWidget"))
                if (browserSwitch != null && !browserSwitch.isChecked) {
                    browserSwitch.click()
                    Thread.sleep(3000)
                }
                break
            }
        }
        device.pressBack()

        clickYahoo()

        clickFirstArticle(device)

        // Assert share button in internal browser exist
        val shareButton = device.wait(Until.findObject(By.res("com.android.chrome", "action_buttons")), 5000)
        assertNotNull(shareButton)
    }

    @Test
    fun openWithExternalBrowser() {
        addYahoo()
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        TopActivityControl.goToSetting()

        // Disable internal browser
        val settingsList = device.findObject(By.clazz(androidx.recyclerview.widget.RecyclerView::class.java))
        val settings = try {
            settingsList.wait(Until.findObjects(By.clazz(LinearLayout::class.java).depth(1)), 5000)
        } catch (e: StaleObjectException) {
            settingsList.findObjects(By.clazz(LinearLayout::class.java).depth(1))
        }

        for (setting in settings) {
            val text = setting.findObject(By.res("android:id/title"))
            if (text.text == "内蔵ブラウザで開く") {
                var browserSwitch: UiObject2? = setting.findObject(
                        By.res("android:id/switch_widget"))
                if (browserSwitch == null)
                    browserSwitch = setting.findObject(
                        By.res("android:id/switchWidget"))
                if (browserSwitch != null && browserSwitch.isChecked) {
                    browserSwitch.click()
                    Thread.sleep(3000)
                }
                break
            }
        }
        device.pressBack()

        clickYahoo()
        clickFirstArticle(device)

        // Assert share button in internal browser does not exist
        val shareButton = device.wait(Until.findObject(By.res("com.android.chrome", "action_buttons")), 5000)
        assertNull(shareButton)
    }

    @Test
    fun goBackToTopWhenFinishAllOfTheArticles() {
        addYahoo()
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        TopActivityControl.goToSetting()

        // Enable option to go back to top
        val settingsList = device.findObject(By.clazz(androidx.recyclerview.widget.RecyclerView::class.java))
        val settings = settingsList.findObjects(By.clazz(LinearLayout::class.java).depth(1))
        for (setting in settings) {
            val text = setting.findObject(By.res("android:id/title"))
            if (text.text == "全ての記事を既読にした時の動作") {
                val summary = setting.findObject(By.res("android:id/summary"))
                if (summary.text == "RSS一覧に戻らない") {
                    setting.clickAndWait(Until.newWindow(), 5000)
                    val check = device.findObject(
                            By.res("android:id/text1").text("RSS一覧に戻る"))
                    check.click()
                    Thread.sleep(3000)
                }
                break
            }
        }
        Thread.sleep(2000)

        device.pressBack()

        clickYahoo()

        // Click fab
        val fab = device.wait(Until.findObject(
            By.res(BuildConfig.APPLICATION_ID, "fab_article_list")), 5000)
        fab.click()
        try {
            val fab2 = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "fab_article_list")), 5000)
            fab2.click()
        } catch (ignored: Exception) {
        }


        // Assert top activity is foreground
        val fabTop = device.wait(Until.findObjects(
            By.res(BuildConfig.APPLICATION_ID, "fab_top")), 5000)
        assertNotNull(fabTop)
    }

    @Test
    fun notGoBackToTopWhenFinishAllOfTheArticles() {
        addYahoo()
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        TopActivityControl.goToSetting()

        // Disable option to go back to top
        val settingsList = device.findObject(By.clazz(androidx.recyclerview.widget.RecyclerView::class.java))
        val settings = settingsList.findObjects(By.clazz(LinearLayout::class.java).depth(1))
        for (setting in settings) {
            val text = setting.findObject(By.res("android:id/title"))
            if (text.text == "全ての記事を既読にした時の動作") {
                val summary = setting.findObject(By.res("android:id/summary"))
                if (summary.text == "RSS一覧に戻る") {
                    setting.clickAndWait(Until.newWindow(), 5000)
                    val check = device.findObject(
                            By.res("android:id/text1").text("RSS一覧に戻らない"))
                    check.click()
                    Thread.sleep(3000)
                }
                break
            }
        }
        device.pressBack()
        clickYahoo()

        // Click fab
        val fab = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "fab_article_list")), 5000)
        fab.click()
        Thread.sleep(2000)

        fab.click()

        // Assert article list is still foreground
        val allReadButton = device.findObject(
                By.res(BuildConfig.APPLICATION_ID, "all_read"))
        assertNotNull(allReadButton)
    }

    @Test
    fun goLicenseActivity() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        TopActivityControl.goToSetting()

        // Click license info
        val settingsList = device.findObject(By.clazz(androidx.recyclerview.widget.RecyclerView::class.java))
        settingsList.scroll(Direction.DOWN, 10f)
        val settings = settingsList.findObjects(By.clazz(LinearLayout::class.java).depth(1))
        for (setting in settings) {
            val text = setting.findObject(By.res("android:id/title"))
            if (text.text == "ライセンス情報") {
                setting.click()
                break
            }
        }

        val webview = device.wait(Until.findObject(By.clazz(WebView::class.java)), 5000)
        assertNotNull(webview)
    }

    private fun addYahoo() {
        TopActivityControl.goToRssTab()
        TopActivityControl.clickAddRssButton()

        // Show edit text for URL if needed
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val searchButton = device.wait(Until.findObject(
            By.res(BuildConfig.APPLICATION_ID, "search_button")), 5000)
        searchButton?.click()

        // Open yahoo RSS URL
        val urlEditText = device.wait(Until.findObject(
            By.res(BuildConfig.APPLICATION_ID, "search_src_text")), 5000)
        assertNotNull("URL edit text was not found", urlEditText)
        urlEditText.text = "https://news.yahoo.co.jp/rss/topics/top-picks.xml"
        device.pressEnter()
    }

    private fun clickYahoo() {
        activityTestRule.onNodeWithText("Yahoo!ニュース・トピックス - 主要", useUnmergedTree = true).performClick()
    }

    private fun clickFirstArticle(device: UiDevice) {
        val firstArticle = device.wait(Until.findObject(By.res(BuildConfig.APPLICATION_ID, "article")), 5000)
        firstArticle.click()
    }
}
