package com.phicdy.mycuration.uitest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.StaleObjectException;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;

import com.phicdy.mycuration.BuildConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class EditFeedTitleTest extends UiTest {

    @Before
    public void setup() {
        super.setup();
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void editYahooNewsTitle() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        // Launch MainActivity
        Context context = InstrumentationRegistry.getContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);

        // Go to feed tab
        List<UiObject2> tabs = device.wait(Until.findObjects(
                By.clazz(android.support.v7.app.ActionBar.Tab.class)), 15000);
        if (tabs == null) fail("Tab was not found");
        if (tabs.size() != 3) fail("Tab size was invalid, size: " + tabs.size());
        try {
            tabs.get(1).click();
        } catch (StaleObjectException e) {
            tabs = device.findObjects(By.clazz(android.support.v7.app.ActionBar.Tab.class));
            if (tabs == null) fail("Tab was not found");
            if (tabs.size() != 3) fail("Tab size was invalid, size: " + tabs.size());
            tabs.get(1).click();
        }

        // Click plus button
        String url = "http://news.yahoo.co.jp/pickup/rss.xml";
        UiObject2 plusButton = device.findObject(By.res(BuildConfig.APPLICATION_ID, "add"));
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
        urlEditText.setText(url);
        device.pressEnter();
        device.wait(Until.gone(By.text("RSSを追加しています。")), 5000);

        // Assert yahoo RSS was added
        List<UiObject2> feedTitles = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedTitle")), 5000);
        if (feedTitles == null) fail("Feed was not found");
        // Feed title list includes show/hide option row, the size is 2
        if (feedTitles.size() != 2) fail("Feed was not added");
        assertThat(feedTitles.get(0).getText(), is("Yahoo!ニュース・トピックス - 主要"));
        assertThat(feedTitles.get(1).getText(), is("全てのRSSを表示"));

        // Edit title
        Rect filterRect = feedTitles.get(0).getVisibleBounds();
        device.swipe(filterRect.centerX(), filterRect.centerY(),
                filterRect.centerX(), filterRect.centerY(), 100);
        UiObject2 edit = device.wait(Until.findObject(
                By.text("RSSのタイトルを編集")), 5000);
        if (edit == null) fail("Edit menu was not found");
        edit.click();
        UiObject2 currentTitle = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "editFeedTitle")), 3000);
        if (currentTitle == null) fail("Current title was not found");
        assertThat(currentTitle.getText(), is("Yahoo!ニュース・トピックス - 主要"));
        currentTitle.setText("test");
        UiObject2 saveButton = device.findObject(By.res("android:id/button1"));
        if (saveButton == null) fail("Save button was not found");
        saveButton.click();

        // Check edited title
        feedTitles = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedTitle")), 5000);
        if (feedTitles == null) fail("Feed was not found");
        if (feedTitles.size() != 2) fail("Feed was not added");
        assertThat(feedTitles.get(0).getText(), is("test"));
    }
}
