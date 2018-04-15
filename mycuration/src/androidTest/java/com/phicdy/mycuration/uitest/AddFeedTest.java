package com.phicdy.mycuration.uitest;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;

import com.phicdy.mycuration.BuildConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class AddFeedTest extends UiTest {

    @Before
    public void setup() {
        super.setup();
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void addYahooNews() {
        // RSS 2.0
        addAndCheckUrl("http://news.yahoo.co.jp/pickup/rss.xml", "Yahoo!ニュース・トピックス - 主要");
    }

    @Test
    public void addYamBlog() {
        // Atom
        addAndCheckUrl("http://y-anz-m.blogspot.com/feeds/comments/default", "Y.A.M の 雑記帳");
    }

    private void addAndCheckUrl(@NonNull String url, @NonNull String title) {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        // Launch MainActivity
        Context context = InstrumentationRegistry.getContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
        assertNotNull(intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);

        // Go to feed tab
        @SuppressWarnings("deprecation")
        List<UiObject2> tabs = device.wait(Until.findObjects(
                By.clazz(android.support.v7.app.ActionBar.Tab.class)), 15000);
        if (tabs == null) fail("Tab was not found");
        if (tabs.size() != 3) fail("Tab size was invalid, size: " + tabs.size());
        tabs.get(1).click();

        TopActivityControl.clickAddRssButton();

        // Show edit text for URL if needed
        UiObject2 searchButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_button")), 5000);
        if (searchButton != null) searchButton.click();

        // Open RSS URL
        UiObject2 urlEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_src_text")), 5000);
        if (urlEditText == null) fail("URL edit text was not found");
        urlEditText.setText(url);
        device.pressEnter();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Assert RSS was added
        List<UiObject2> feedTitles = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedTitle")), 5000);
        if (feedTitles == null) fail("Feed was not found");
        // Feed title list includes show/hide option row, the size is 2
        if (feedTitles.size() != 2) fail("Feed was not added");
        assertThat(feedTitles.get(0).getText(), is(title));
        assertThat(feedTitles.get(1).getText(), is("全てのRSSを表示"));

        // Assert articles of RSS were added
        List<UiObject2> feedUnreadCountList = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedCount")), 5000);
        if (feedUnreadCountList == null) fail("Feed count was not found");
        // Feed count list does not include show/hide option row, the size is 1
        if (feedUnreadCountList.size() != 1) fail("Feed count was not added");
        assertTrue(Integer.valueOf(feedUnreadCountList.get(0).getText()) >= -1);

        // Assert all article view shows
        UiObject2 allArticleView = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "ll_all_unread")), 5000);
        assertNotNull(allArticleView);
        device.pressBack();
    }

    @Test
    public void tryInvalidUrl() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        takeScreenshot(device, "start_of_tryInvalidUrl_" + System.currentTimeMillis());
        // Launch MainActivity
        Context context = InstrumentationRegistry.getContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        device.wait(Until.findObject(By.res(BuildConfig.APPLICATION_ID, "add")), 5000);
        takeScreenshot(device, "after_startActivity_" + System.currentTimeMillis());

        // Go to feed tab
        @SuppressWarnings("deprecation")
        List<UiObject2> tabs = device.findObjects(
                By.clazz(android.support.v7.app.ActionBar.Tab.class));
        takeScreenshot(device, "main_ui_before_click_feed_tab_" + System.currentTimeMillis());
        if (tabs == null) fail("Tab was not found");
        if (tabs.size() != 3) fail("Tab size was invalid, size: " + tabs.size());
        tabs.get(1).click();

        // Get current RSS size
        UiObject2 rssList = device.findObject(By.res(BuildConfig.APPLICATION_ID, "feedList"));
        int numOfRss = 0;
        if (rssList != null) {
            numOfRss = rssList.getChildCount();
        }

        TopActivityControl.clickAddRssButton();

        // Show edit text for URL if needed
        UiObject2 searchButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_button")), 5000);
        if (searchButton != null) searchButton.click();

        // Open invalid RSS URL
        UiObject2 urlEditText = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "search_src_text")), 5000);
        if (urlEditText == null) fail("URL edit text was not found");
        urlEditText.setText("http://ghaorgja.co.jp/rss.xml");
        device.pressEnter();

        rssList = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "feedList")), 5000);
        if (numOfRss == 0) {
            assertNull(rssList);
        } else {
            assertThat(rssList.getChildCount(), is(numOfRss));
        }
    }

    @Test
    public void clickFabWithoutUrlOpen() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        // Launch MainActivity
        Context context = InstrumentationRegistry.getContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Go to feed tab
        @SuppressWarnings("deprecation")
        List<UiObject2> tabs = device.wait(Until.findObjects(
                By.clazz(android.support.v7.app.ActionBar.Tab.class)), 15000);
        if (tabs == null) fail("Tab was not found");
        if (tabs.size() != 3) fail("Tab size was invalid, size: " + tabs.size());
        tabs.get(1).click();

        TopActivityControl.clickAddRssButton();

        // Open invalid RSS URL
        UiObject2 fab = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "fab")), 5000);
        if (fab == null) fail("Fab was not found");
        fab.click();

        // Fab still exists
        fab = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "fab")), 5000);
        assertNotNull(fab);
    }

}
