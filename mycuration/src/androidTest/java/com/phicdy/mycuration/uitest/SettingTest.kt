package com.phicdy.mycuration.uitest

import androidx.test.InstrumentationRegistry
import androidx.test.filters.SdkSuppress
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import androidx.recyclerview.widget.RecyclerView
import android.webkit.WebView
import android.widget.LinearLayout
import com.phicdy.mycuration.BuildConfig
import com.phicdy.mycuration.presentation.view.activity.TopActivity
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class SettingTest : UiTest() {

    @JvmField
    @Rule
    var activityTestRule = ActivityTestRule(TopActivity::class.java)

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

        // Click first feed
        val feedTitles = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedTitle")), 5000)
        assertNotNull("Feed was not found", feedTitles)
        feedTitles[0].clickAndWait(Until.newWindow(), 5000)

        // Click first article
        val articleList = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "rv_article")), 5000)
        val firstArticle = articleList.findObject(By.clazz(LinearLayout::class.java))
        firstArticle.click()

        // Assert share button in internal browser exist
        val shareButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "menu_item_share")), 5000)
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

        // Click first feed
        val feedTitles = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedTitle")), 5000)
        assertNotNull("Feed was not found", feedTitles)
        feedTitles[0].clickAndWait(Until.newWindow(), 5000)

        val articleList = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "rv_article")), 5000)
        val firstArticle = articleList.findObject(
                By.clazz(LinearLayout::class.java))
        firstArticle.click()

        // Assert share button in internal browser does not exist
        val shareButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "menu_item_share")), 5000)
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

        // Click first feed
        val feedTitles = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedTitle")), 5000)
        if (feedTitles == null) {
            takeScreenshot(device)
            fail("Feed was not found")
        }
        feedTitles[0].click()

        // Click fab
        val fab = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "fab_article_list")), 5000)
        fab.click()
        Thread.sleep(5000)

        fab.click()

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

        // Click first feed
        val feedTitles = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedTitle")), 5000)
        assertNotNull("Feed was not found", feedTitles)
        feedTitles[0].click()

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
        urlEditText.text = "http://news.yahoo.co.jp/pickup/rss.xml"
        device.pressEnter()
    }
}
