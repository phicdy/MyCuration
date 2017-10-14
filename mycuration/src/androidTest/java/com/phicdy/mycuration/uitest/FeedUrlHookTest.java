package com.phicdy.mycuration.uitest;

import android.content.Context;
import android.content.Intent;
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

import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class FeedUrlHookTest extends UiTest {

    @Before
    public void setup() {
        super.setup();
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void addYahooNewsFromDefaultBrowser() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        // Launch default browser
        Context context = InstrumentationRegistry.getContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.android.browser");
        if (intent == null) {
            // Default browser is not existed
            return;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Open feed url
        UiObject2 urlBar = device.wait(Until.findObject(
                By.res("com.android.browser", "url")), 5000);
        urlBar.click();
        urlBar.setText("http://news.yahoo.co.jp/pickup/rss.xml");
        device.pressEnter();

        // Open share menu
        UiObject2 settingButton = device.wait(
                Until.findObject(By.res("com.android.browser", "more")), 5000);
        if (settingButton == null) fail("Setting button was not found");
        settingButton.click();
        UiObject2 shareMenu = device.wait(
                Until.findObject(By.text("ページを共有")), 5000);
        if (shareMenu == null) fail("Share menu was not found");
        shareMenu.click();

        // Click app icon
        UiObject2 shareList = device.wait(
                Until.findObject(By.clazz(ListView.class)), 5000);
        if (shareList == null) fail("Share app list was not found");
        List<UiObject2> shareApps = shareList.wait(
                Until.findObjects(By.clazz(LinearLayout.class).depth(2)), 5000);
        for (UiObject2 shareApp : shareApps) {
            UiObject2 label = shareApp.findObject(By.res("android", "text1"));
            if (label.getText().equals("RSS登録")) {
                shareApp.click();
                break;
            }
        }

        // Wait for adding feed and go back to the browser
        device.wait(Until.findObject(
                By.res("com.android.browser", "url")), 5000);

        // Close the browser
        device.pressBack();
        device.pressBack();

        // Launch MainActivity
        intent = context.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Go to feed tab
        @SuppressWarnings("deprecation")
        List<UiObject2> tabs = device.wait(Until.findObjects(
                By.clazz(android.support.v7.app.ActionBar.Tab.class)), 5000);
        if (tabs == null) fail("Tab was not found");
        if (tabs.size() != 3) fail("Tab size was invalid, size: " + tabs.size());
        tabs.get(1).click();

        // Assert yahoo RSS was added
        List<UiObject2> feedTitles = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedTitle")), 5000);
        if (feedTitles == null) fail("Feed was not found");
        // Feed title list includes show/hide option row, the size is 2
        if (feedTitles.size() != 2) fail("Feed was not added");
        assertThat(feedTitles.get(0).getText(), is("Yahoo!ニュース・トピックス - 主要"));
        assertThat(feedTitles.get(1).getText(), is("全てのRSSを表示"));

        // Assert articles of yahoo RSS were added
        List<UiObject2> feedUnreadCountList = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedCount")), 5000);
        if (feedUnreadCountList == null) fail("Feed count was not found");
        // Feed count list does not include show/hide option row, the size is 1
        if (feedUnreadCountList.size() != 1) fail("Feed count was not added");
        assertThat(Integer.valueOf(feedUnreadCountList.get(0).getText()), greaterThan(0));
    }
}
