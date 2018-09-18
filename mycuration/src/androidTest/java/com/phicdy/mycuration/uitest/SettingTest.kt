package com.phicdy.mycuration.uitest

import android.support.test.InstrumentationRegistry
import android.support.test.filters.SdkSuppress
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.By
import android.support.test.uiautomator.StaleObjectException
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiObject2
import android.support.test.uiautomator.Until
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.phicdy.mycuration.BuildConfig
import com.phicdy.mycuration.presentation.view.activity.TopActivity
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import junit.framework.Assert.fail
import org.junit.After
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

        // Go to feed tab
        val tabs = device.findObjects(
                By.clazz(android.support.v7.app.ActionBar.Tab::class.java))
        assertNotNull("Tab was not found", tabs)
        if (tabs.size != 3) fail("Tab size was invalid, size: " + tabs.size)
        takeScreenshot(device)
        tabs[1].click()

        TopActivityControl.clickAddRssButton()

        // Show edit text for URL if needed
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

    @After
    public override fun tearDown() {
        super.tearDown()
    }

    @Test
    fun openWithInternalBrowser() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Click setting button
        val settingButton = device.wait(
                Until.findObject(By.res(BuildConfig.APPLICATION_ID, "setting_top_activity")), 5000)
        if (settingButton == null) fail("Setting button was not found")
        settingButton.clickAndWait(Until.newWindow(), 5000)

        // Enable internal browser
        val settingsList = device.findObject(By.clazz(ListView::class.java))
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
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Click setting button
        val settingButton = device.wait(
                Until.findObject(By.res(BuildConfig.APPLICATION_ID, "setting_top_activity")), 5000)
        assertNotNull("Setting button was not found", settingButton)
        settingButton.clickAndWait(Until.newWindow(), 5000)

        // Disable internal browser
        val settingsList = device.findObject(By.clazz(ListView::class.java))
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
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Click setting button
        val settingButton = device.wait(
                Until.findObject(By.res(BuildConfig.APPLICATION_ID, "setting_top_activity")), 5000)
        assertNotNull("Setting button was not found", settingButton)
        settingButton.clickAndWait(Until.newWindow(), 5000)

        // Enable option to go back to top
        val settingsList = device.findObject(By.clazz(ListView::class.java))
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
        val tabs = device.wait(Until.findObjects(
                By.clazz(android.support.v7.app.ActionBar.Tab::class.java)), 5000)
        assertNotNull(tabs)
    }

    @Test
    fun notGoBackToTopWhenFinishAllOfTheArticles() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Click setting button
        val settingButton = device.wait(
                Until.findObject(By.res(BuildConfig.APPLICATION_ID, "setting_top_activity")), 5000)
        if (settingButton == null) {
            takeScreenshot(device)
            fail("Setting button was not found")
        }
        settingButton.clickAndWait(Until.newWindow(), 5000)

        // Disable option to go back to top
        val settingsList = device.findObject(By.clazz(ListView::class.java))
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

        // Click setting button
        val settingButton = device.wait(
                Until.findObject(By.res(BuildConfig.APPLICATION_ID, "setting_top_activity")), 5000)
        assertNotNull("Setting button was not found", settingButton)
        settingButton.clickAndWait(Until.newWindow(), 5000)

        // Click license info
        val settingsList = device.findObject(By.clazz(ListView::class.java))
        val settings = settingsList.findObjects(By.clazz(LinearLayout::class.java).depth(1))
        for (setting in settings) {
            val text = setting.findObject(By.res("android:id/title"))
            if (text.text == "ライセンス情報") {
                setting.click()
                break
            }
        }

        // Assert title
        val title = device.wait(Until.findObject(
                By.clazz(TextView::class.java).text("ライセンス")), 5000)
        assertNotNull(title)
    }
}
