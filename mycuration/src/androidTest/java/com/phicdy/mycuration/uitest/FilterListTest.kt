package com.phicdy.mycuration.uitest

import android.support.test.InstrumentationRegistry
import android.support.test.filters.SdkSuppress
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.By
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiObject2
import android.support.test.uiautomator.Until
import android.widget.LinearLayout
import android.widget.ListView
import com.phicdy.mycuration.BuildConfig
import com.phicdy.mycuration.presentation.view.activity.TopActivity
import junit.framework.Assert.assertNotNull
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class FilterListTest : UiTest() {

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
    fun addFilterForYahooNews() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val testTitle = "Test"
        val testKeyword = "TestKeyword"
        val testUrl = "http://www.google.com"
        addTestFeedsAndFilter(testTitle, testKeyword, testUrl)

        // Assert first item
        val filterList = device.wait(Until.findObject(
                By.clazz(ListView::class.java)), 5000)
        assertNotNull("Filter list was not found", filterList)
        val filters = filterList.findObjects(
                By.clazz(LinearLayout::class.java).depth(2))
        assertNotNull("Filter item was not found", filters)
        assertThat(filters.size, `is`(1))
        val title = filters[0].findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterTitle"))
        assertNotNull(title)
        assertThat(title.text, `is`(testTitle))
        val target = filters[0].findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterTargetFeed"))
        assertNotNull(target)
        assertThat(target.text, `is`("Yahoo!ニュース・トピックス - 主要"))
        val keyword = filters[0].findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterKeyword"))
        assertNotNull(keyword)
        assertThat(keyword.text, `is`("キーワード: $testKeyword"))
        val url = filters[0].findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterUrl"))
        assertNotNull(url)
        assertThat(url.text, `is`("URL: $testUrl"))
    }

    @Test
    fun deleteFilter() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val testTitle = "Test"
        val testKeyword = "TestKeyword"
        val testUrl = "http://www.google.com"
        addTestFeedsAndFilter(testTitle, testKeyword, testUrl)
        longClickFirstFilter()

        // Click delete menu
        val edit = device.wait(Until.findObject(By.text("フィルター削除")), 5000)
        assertNotNull("Edit filter menu was not found", edit)
        edit.click()

        // Assert filter was deleted
        val emptyView = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "filter_emptyView")), 5000)
        assertNotNull(emptyView)
    }

    @Test
    fun editFilter() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val testTitle = "Test"
        val testKeyword = "TestKeyword"
        val testUrl = "http://www.google.com"
        addTestFeedsAndFilter(testTitle, testKeyword, testUrl)
        longClickFirstFilter()

        // Click edit menu
        val edit = device.wait(Until.findObject(By.text("フィルターの編集")), 5000)
        assertNotNull("Edit filter menu was not found", edit)
        edit.click()

        // Assert filter title
        val filterTitleEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterTitle")), 5000)
        assertNotNull("Filter title edit text was not found", filterTitleEditText)
        assertThat(filterTitleEditText.text, `is`(testTitle))

        // Assert target RSS
        val targetRss = device.findObject(
                By.res(BuildConfig.APPLICATION_ID, "tv_target_rss"))
        assertNotNull("Target RSS was not found", targetRss)
        assertThat(targetRss.text, `is`("Yahoo!ニュース・トピックス - 主要"))

        // Assert filter keyword
        val filterKeywordEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterKeyword")), 5000)
        assertNotNull("Filter keyword edit text was not found", filterKeywordEditText)
        assertThat(filterKeywordEditText.text, `is`(testKeyword))

        // Assert filter URL
        val filterUrlEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterUrl")), 5000)
        assertNotNull("Filter URL edit text was not found", filterUrlEditText)
        assertThat(filterUrlEditText.text, `is`(testUrl))

        // Edit filter info
        val editTitle = "EditTitle"
        val editKeyword = "EditKeyword"
        val editUrl = "http://yahoo.co.jp"
        filterTitleEditText.clear()
        filterTitleEditText.text = editTitle
        filterKeywordEditText.clear()
        filterKeywordEditText.text = editKeyword
        filterUrlEditText.clear()
        filterUrlEditText.text = editUrl

        // Delete first RSS and add second RSS
        targetRss.click()
        val targetList = device.wait(Until.findObject(By.clazz(ListView::class.java)), 5000)
        assertNotNull("Target list was not found", targetList)
        val listItems = targetList.findObjects(By.clazz(LinearLayout::class.java))
        assertNotNull("Target RSS item was not found", listItems)
        listItems[0].click()
        listItems[1].click()
        val doneButton = device.findObject(By.res(BuildConfig.APPLICATION_ID, "done_select_target_rss"))
        assertNotNull("Done button was not found", doneButton)
        doneButton.click()

        // Click add button
        val addButton = device.wait(Until.findObject(By.res(BuildConfig.APPLICATION_ID, "add_filter")), 5000)
        assertNotNull("Filter add button was not found", addButton)
        addButton.click()

        // Assert first item
        val filterList = device.wait(Until.findObject(By.clazz(ListView::class.java)), 5000)
        assertNotNull("Filter list was not found", filterList)
        val filters = filterList.findObjects(By.clazz(LinearLayout::class.java).depth(2))
        assertNotNull("Filter item was not found", filters)
        assertThat(filters.size, `is`(1))
        val title = filters[0].findObject(By.res(BuildConfig.APPLICATION_ID, "filterTitle"))
        assertNotNull(title)
        assertThat(title.text, `is`(editTitle))
        val target = filters[0].findObject(By.res(BuildConfig.APPLICATION_ID, "filterTargetFeed"))
        assertNotNull(target)
        assertThat(target.text, `is`("Yahoo!ニュース・トピックス - 国際"))
        val keyword = filters[0].findObject(By.res(BuildConfig.APPLICATION_ID, "filterKeyword"))
        assertNotNull(keyword)
        assertThat(keyword.text, `is`("キーワード: $editKeyword"))
        val url = filters[0].findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterUrl"))
        assertNotNull(url)
        assertThat(url.text, `is`("URL: $editUrl"))
    }

    private fun addTestFeedsAndFilter(testTitle: String, testKeyword: String, testUrl: String) {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        TopActivityControl.goToRssTab()
        TopActivityControl.clickAddRssButton()

        // Show edit text for URL if needed
        var searchButton: UiObject2? = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_button")), 5000)
        searchButton?.click()

        // Open yahoo RSS URL
        var urlEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_src_text")), 5000)
        assertNotNull("URL edit text was not found", urlEditText)
        urlEditText.text = "http://news.yahoo.co.jp/pickup/rss.xml"
        device.pressEnter()

        TopActivityControl.clickAddRssButton()

        // Show edit text for URL if needed
        searchButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_button")), 5000)
        searchButton?.click()

        // Open second yahoo RSS URL
        urlEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_src_text")), 5000)
        assertNotNull("URL edit text was not found", urlEditText)
        urlEditText.text = "http://news.yahoo.co.jp/pickup/world/rss.xml"
        device.pressEnter()

        TopActivityControl.goToFilterTab()
        TopActivityControl.clickAddFilterButton()

        // Input filter title
        val filterTitleEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterTitle")), 5000)
        assertNotNull("Filter title edit text was not found", filterTitleEditText)
        filterTitleEditText.text = testTitle

        // Click target RSS
        val targetRss = device.findObject(By.res(BuildConfig.APPLICATION_ID, "tv_target_rss"))
        assertNotNull("Target RSS was not found", targetRss)
        targetRss.click()

        // Select first item
        val targetList = device.wait(Until.findObject(
                By.clazz(ListView::class.java)), 5000)
        assertNotNull("Target list was not found", targetList)
        val listItems = targetList.findObjects(
                By.clazz(LinearLayout::class.java))
        assertNotNull("Target RSS item was not found", listItems)
        listItems[0].click()
        val doneButton = device.findObject(
                By.res(BuildConfig.APPLICATION_ID, "done_select_target_rss"))
        assertNotNull("Done button was not found", doneButton)
        doneButton.click()

        // Input filter keyword
        val filterKeywordEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterKeyword")), 5000)
        assertNotNull("Filter keyword edit text was not found", filterKeywordEditText)
        filterKeywordEditText.text = testKeyword

        // Input filter URL
        val filterUrlEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterUrl")), 5000)
        assertNotNull("Filter URL edit text was not found", filterUrlEditText)
        filterUrlEditText.text = testUrl

        // Click add button
        val addButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "add_filter")), 5000)
        assertNotNull("Filter add button was not found", addButton)
        addButton.click()
    }

    private fun longClickFirstFilter() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        // Long click first filter
        val filterList = device.wait(Until.findObject(
                By.clazz(ListView::class.java)), 5000)
        assertNotNull("Filter list was not found", filterList)
        val filters = filterList.findObjects(
                By.clazz(LinearLayout::class.java).depth(2))
        assertNotNull("Filter item was not found", filters)
        assertThat(filters.size, `is`(1))
        val filterRect = filters[0].visibleBounds
        device.swipe(filterRect.centerX(), filterRect.centerY(),
                filterRect.centerX(), filterRect.centerY(), 100)
    }
}
