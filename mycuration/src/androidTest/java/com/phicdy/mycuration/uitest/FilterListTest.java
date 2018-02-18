package com.phicdy.mycuration.uitest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.phicdy.mycuration.BuildConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class FilterListTest extends UiTest {

    @Before
    public void setup() {
        super.setup();
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void addFilterForYahooNews() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        String testTitle = "Test";
        String testKeyword = "TestKeyword";
        String testUrl = "http://www.google.com";
        addTestFeedsAndFilter(testTitle, testKeyword, testUrl);

        // Assert first item
        UiObject2 filterList = device.wait(Until.findObject(
                By.clazz(ListView.class)), 5000);
        if (filterList == null) fail("Filter list was not found");
        List<UiObject2> filters = filterList.findObjects(
                By.clazz(LinearLayout.class).depth(2));
        if (filters == null) fail("Filter item was not found");
        assertThat(filters.size(), is(1));
        UiObject2 title = filters.get(0).findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterTitle"));
        assertNotNull(title);
        assertThat(title.getText(), is(testTitle));
        UiObject2 target = filters.get(0).findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterTargetFeed"));
        assertNotNull(target);
        assertThat(target.getText(), is("Yahoo!ニュース・トピックス - 主要 "));
        UiObject2 keyword = filters.get(0).findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterKeyword"));
        assertNotNull(keyword);
        assertThat(keyword.getText(), is("キーワード: " + testKeyword));
        UiObject2 url = filters.get(0).findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterUrl"));
        assertNotNull(url);
        assertThat(url.getText(), is("URL: " + testUrl));
    }

    @Test
    public void deleteFilter() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        String testTitle = "Test";
        String testKeyword = "TestKeyword";
        String testUrl = "http://www.google.com";
        addTestFeedsAndFilter(testTitle, testKeyword, testUrl);
        longClickFirstFilter();

        // Click delete menu
        UiObject2 edit = device.wait(Until.findObject(By.text("フィルター削除")), 5000);
        if (edit == null) fail("Edit filter menu was not found");
        edit.click();

        // Assert filter was deleted
        UiObject2 emptyView = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "filter_emptyView")), 5000);
        assertNotNull(emptyView);
    }

    @Test
    public void editFilter() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        String testTitle = "Test";
        String testKeyword = "TestKeyword";
        String testUrl = "http://www.google.com";
        addTestFeedsAndFilter(testTitle, testKeyword, testUrl);
        longClickFirstFilter();

        // Click edit menu
        UiObject2 edit = device.wait(Until.findObject(By.text("フィルターの編集")), 5000);
        if (edit == null) fail("Edit filter menu was not found");
        edit.click();

        // Assert filter title
        UiObject2 filterTitleEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterTitle")), 5000);
        if (filterTitleEditText == null) fail("Filter title edit text was not found");
        assertThat(filterTitleEditText.getText(), is(testTitle));

        // Assert target RSS
        UiObject2 targetRss = device.findObject(
                By.res(BuildConfig.APPLICATION_ID, "tv_target_rss"));
        if (targetRss == null) fail("Target RSS was not found");
        assertThat(targetRss.getText(), is("Yahoo!ニュース・トピックス - 主要"));

        // Assert filter keyword
        UiObject2 filterKeywordEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterKeyword")), 5000);
        if (filterKeywordEditText == null) fail("Filter keyword edit text was not found");
        assertThat(filterKeywordEditText.getText(), is(testKeyword));

        // Assert filter URL
        UiObject2 filterUrlEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterUrl")), 5000);
        if (filterUrlEditText == null) fail("Filter URL edit text was not found");
        assertThat(filterUrlEditText.getText(), is(testUrl));

        // Edit filter info
        String editTitle = "EditTitle";
        String editKeyword = "EditKeyword";
        String editUrl = "http://yahoo.co.jp";
        filterTitleEditText.clear();
        filterTitleEditText.setText(editTitle);
        filterKeywordEditText.clear();
        filterKeywordEditText.setText(editKeyword);
        filterUrlEditText.clear();
        filterUrlEditText.setText(editUrl);

        // Delete first RSS and add second RSS
        targetRss.click();
        UiObject2 targetList = device.wait(Until.findObject(
                By.clazz(ListView.class)), 5000);
        if (targetList == null) fail("Target list was not found");
        List<UiObject2> listItems = targetList.findObjects(
                By.clazz(LinearLayout.class));
        if (listItems == null) fail("Target RSS item was not found");
        listItems.get(0).click();
        listItems.get(1).click();
        UiObject2 doneButton = device.findObject(
                By.res(BuildConfig.APPLICATION_ID, "done_select_target_rss"));
        if (doneButton == null) fail("Done button was not found");
        doneButton.click();

        // Click add button
        UiObject2 addButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "add_filter")), 5000);
        if (addButton == null) fail("Filter add button was not found");
        addButton.click();

        // Assert first item
        UiObject2 filterList = device.wait(Until.findObject(
                By.clazz(ListView.class)), 5000);
        if (filterList == null) fail("Filter list was not found");
        List<UiObject2> filters = filterList.findObjects(
                By.clazz(LinearLayout.class).depth(2));
        if (filters == null) fail("Filter item was not found");
        assertThat(filters.size(), is(1));
        UiObject2 title = filters.get(0).findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterTitle"));
        assertNotNull(title);
        assertThat(title.getText(), is(editTitle));
        UiObject2 target = filters.get(0).findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterTargetFeed"));
        assertNotNull(target);
        assertThat(target.getText(), is("Yahoo!ニュース・トピックス - 国際 "));
        UiObject2 keyword = filters.get(0).findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterKeyword"));
        assertNotNull(keyword);
        assertThat(keyword.getText(), is("キーワード: " + editKeyword));
        UiObject2 url = filters.get(0).findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterUrl"));
        assertNotNull(url);
        assertThat(url.getText(), is("URL: " + editUrl));
    }

    @SuppressWarnings("deprecation")
    private void addTestFeedsAndFilter(String testTitle, String testKeyword, String testUrl) {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        // Launch MainActivity
        Context context = InstrumentationRegistry.getContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        device.wait(Until.findObject(By.res(BuildConfig.APPLICATION_ID, "add")), 5000);

        // Go to feed tab
        List<UiObject2> tabs = device.findObjects(
                By.clazz(android.support.v7.app.ActionBar.Tab.class));
        if (tabs == null) fail("Tab was not found");
        if (tabs.size() != 3) fail("Tab size was invalid, size: " + tabs.size());
        takeScreenshot(device, "before_click_RSS_tab" + System.currentTimeMillis());
        tabs.get(1).click();

        // Click plus button
        UiObject2 plusButton = device.findObject(By.res(BuildConfig.APPLICATION_ID, "add_new_rss"));
        if (plusButton == null) fail("Plus button was not found");
        plusButton.click();

        // Show edit text for URL if needed
        UiObject2 searchButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_button")), 5000);
        if (searchButton != null) searchButton.click();

        // Open yahoo RSS URL
        UiObject2 urlEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_src_text")), 5000);
        if (urlEditText == null) fail("URL edit text was not found");
        urlEditText.setText("http://news.yahoo.co.jp/pickup/rss.xml");
        device.pressEnter();

        // Click plus button
        plusButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "add_new_rss")), 5000);
        if (plusButton == null) fail("Plus button was not found");
        plusButton.click();

        // Show edit text for URL if needed
        searchButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_button")), 5000);
        if (searchButton != null) searchButton.click();

        // Open second yahoo RSS URL
        urlEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_src_text")), 5000);
        if (urlEditText == null) fail("URL edit text was not found");
        urlEditText.setText("http://news.yahoo.co.jp/pickup/world/rss.xml");
        device.pressEnter();

        // Go to filter tab
        tabs = device.wait(Until.findObjects(
                By.clazz(android.support.v7.app.ActionBar.Tab.class)), 5000);
        if (tabs == null) fail("Tab was not found");
        if (tabs.size() != 3) fail("Tab size was invalid, size: " + tabs.size());
        tabs.get(2).click();

        // Click plus button
        plusButton = device.findObject(By.res(BuildConfig.APPLICATION_ID, "add_new_rss"));
        if (plusButton == null) fail("Plus button was not found");
        plusButton.click();

        // Input filter title
        UiObject2 filterTitleEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterTitle")), 5000);
        if (filterTitleEditText == null) fail("Filter title edit text was not found");
        filterTitleEditText.setText(testTitle);

        // Click target RSS
        UiObject2 targetRss = device.findObject(By.res(BuildConfig.APPLICATION_ID, "tv_target_rss"));
        if (targetRss == null) fail("Target RSS was not found");
        targetRss.click();

        // Select first item
        UiObject2 targetList = device.wait(Until.findObject(
                By.clazz(ListView.class)), 5000);
        if (targetList == null) fail("Target list was not found");
        List<UiObject2> listItems = targetList.findObjects(
                By.clazz(LinearLayout.class));
        if (listItems == null) fail("Target RSS item was not found");
        listItems.get(0).click();
        UiObject2 doneButton = device.findObject(
                By.res(BuildConfig.APPLICATION_ID, "done_select_target_rss"));
        if (doneButton == null) fail("Done button was not found");
        doneButton.click();

        // Input filter keyword
        UiObject2 filterKeywordEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterKeyword")), 5000);
        if (filterKeywordEditText == null) fail("Filter keyword edit text was not found");
        filterKeywordEditText.setText(testKeyword);

        // Input filter URL
        UiObject2 filterUrlEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "filterUrl")), 5000);
        if (filterUrlEditText == null) fail("Filter URL edit text was not found");
        filterUrlEditText.setText(testUrl);

        // Click add button
        UiObject2 addButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "add_filter")), 5000);
        if (addButton == null) fail("Filter add button was not found");
        addButton.click();
    }

    private void longClickFirstFilter() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        // Long click first filter
        UiObject2 filterList = device.wait(Until.findObject(
                By.clazz(ListView.class)), 5000);
        if (filterList == null) fail("Filter list was not found");
        List<UiObject2> filters = filterList.findObjects(
                By.clazz(LinearLayout.class).depth(2));
        if (filters == null) fail("Filter item was not found");
        assertThat(filters.size(), is(1));
        Rect filterRect = filters.get(0).getVisibleBounds();
        device.swipe(filterRect.centerX(), filterRect.centerY(),
                filterRect.centerX(), filterRect.centerY(), 100);
    }
}
