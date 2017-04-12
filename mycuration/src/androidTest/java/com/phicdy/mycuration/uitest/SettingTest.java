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
import android.widget.TextView;

import com.phicdy.mycuration.BuildConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class SettingTest extends UiTest {

    @Before
    public void setup() {
        super.setup();

        Context context = InstrumentationRegistry.getTargetContext();
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Launch MainActivity
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        device.wait(Until.findObject(By.res(BuildConfig.APPLICATION_ID, "add")), 5000);

        // Go to feed tab
        @SuppressWarnings("deprecation")
        List<UiObject2> tabs = device.findObjects(
                By.clazz(android.support.v7.app.ActionBar.Tab.class));
        if (tabs == null) fail("Tab was not found");
        if (tabs.size() != 3) fail("Tab size was invalid, size: " + tabs.size());
        takeScreenshot(device);
        tabs.get(1).click();

        // Click plus button
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
        urlEditText.setText("http://news.yahoo.co.jp/pickup/rss.xml");
        device.pressEnter();
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void openWithInternalBrowser() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Click setting button
        UiObject2 settingButton = device.wait(
                Until.findObject(By.res(BuildConfig.APPLICATION_ID, "setting")), 5000);
        if (settingButton == null) fail("Setting button was not found");
        settingButton.click();

        // Enable internal browser
        UiObject2 settingsList = device.wait(
                Until.findObject(By.clazz(ListView.class)), 5000);
        List<UiObject2> settings = settingsList.wait(
                Until.findObjects(By.clazz(LinearLayout.class).depth(1)), 5000);
        for (UiObject2 setting : settings) {
            UiObject2 text = setting.findObject(
                    By.res("android:id/title"));
            if (text.getText().equals("内蔵ブラウザで開く")) {
                UiObject2 browserSwitch = setting.findObject(
                        By.res("android:id/switchWidget"));
                if (!browserSwitch.isChecked()) {
                    browserSwitch.click();
                }
                break;
            }
        }
        device.pressBack();

        // Click first feed
        List<UiObject2> feedTitles = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedTitle")), 5000);
        if (feedTitles == null) fail("Feed was not found");
        feedTitles.get(0).clickAndWait(Until.newWindow(), 5000);

        // Click first article
        UiObject2 articleList = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "lv_article")), 5000);
        UiObject2 firstArticle = articleList.findObject(By.clazz(LinearLayout.class));
        firstArticle.click();

        // Assert default browser is not opened
        UiObject2 defaultBrowserUrlBar = device.wait(Until.findObject(
                By.res("com.android.browser:id/taburlbar")), 5000);

        assertNull(defaultBrowserUrlBar);
    }

    @Test
    public void openWithExternalBrowser() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Click setting button
        UiObject2 settingButton = device.wait(
                Until.findObject(By.res(BuildConfig.APPLICATION_ID, "setting")), 5000);
        if (settingButton == null) fail("Setting button was not found");
        settingButton.click();

        // Disable internal browser
        UiObject2 settingsList = device.wait(
                Until.findObject(By.clazz(ListView.class)), 5000);
        List<UiObject2> settings = settingsList.wait(
                Until.findObjects(By.clazz(LinearLayout.class).depth(1)), 5000);
        for (UiObject2 setting : settings) {
            UiObject2 text = setting.findObject(
                    By.res("android:id/title"));
            if (text.getText().equals("内蔵ブラウザで開く")) {
                UiObject2 browserSwitch = setting.findObject(
                        By.res("android:id/switchWidget"));
                if (browserSwitch.isChecked()) {
                    browserSwitch.click();
                }
                break;
            }
        }
        device.pressBack();

        // Click first feed
        List<UiObject2> feedTitles = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedTitle")), 5000);
        if (feedTitles == null) fail("Feed was not found");
        feedTitles.get(0).clickAndWait(Until.newWindow(), 5000);

        UiObject2 articleList = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "lv_article")), 5000);
        UiObject2 firstArticle = articleList.findObject(
                By.clazz(LinearLayout.class));
        firstArticle.click();

        // Assert default browser is opened
        UiObject2 defaultBrowserUrlBar = device.wait(Until.findObject(
                By.res("com.android.browser:id/taburlbar")), 5000);
        assertNotNull(defaultBrowserUrlBar);
    }

    @Test
    public void goBackToTopWhenFinishAllOfTheArticles() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Click setting button
        UiObject2 settingButton = device.wait(
                Until.findObject(By.res(BuildConfig.APPLICATION_ID, "setting")), 5000);
        if (settingButton == null) fail("Setting button was not found");
        settingButton.click();

        // Enable option to go back to top
        UiObject2 settingsList = device.wait(
                Until.findObject(By.clazz(ListView.class)), 5000);
        List<UiObject2> settings = settingsList.wait(
                Until.findObjects(By.clazz(LinearLayout.class).depth(1)), 5000);
        for (UiObject2 setting : settings) {
            UiObject2 text = setting.findObject(
                    By.res("android:id/title"));
            if (text.getText().equals("全ての記事を既読にした時の動作")) {
                UiObject2 summary = setting.findObject(
                        By.res("android:id/summary"));
                if (summary.getText().equals("RSS一覧に戻らない")) {
                    setting.clickAndWait(Until.newWindow(), 5000);
                    UiObject2 check = device.findObject(
                            By.res("android:id/text1").text("RSS一覧に戻る"));
                    check.click();
                }
                break;
            }
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        device.pressBack();

        // Click first feed
        List<UiObject2> feedTitles = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedTitle")), 5000);
        if (feedTitles == null) {
            takeScreenshot(device);
            fail("Feed was not found");
        }
        feedTitles.get(0).click();

        // Click fab
        UiObject2 fab = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "fab")), 5000);
        fab.click();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fab.click();

        // Assert top activity is foreground
        @SuppressWarnings("deprecation")
        List<UiObject2> tabs = device.wait(Until.findObjects(
                By.clazz(android.support.v7.app.ActionBar.Tab.class)), 5000);
        assertNotNull(tabs);
    }

    @Test
    public void notGoBackToTopWhenFinishAllOfTheArticles() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Click setting button
        UiObject2 settingButton = device.wait(
                Until.findObject(By.res(BuildConfig.APPLICATION_ID, "setting")), 5000);
        if (settingButton == null) {
            takeScreenshot(device);
            fail("Setting button was not found");
        }
        settingButton.click();

        // Disable option to go back to top
        UiObject2 settingsList = device.wait(
                Until.findObject(By.clazz(ListView.class)), 5000);
        List<UiObject2> settings = settingsList.wait(
                Until.findObjects(By.clazz(LinearLayout.class).depth(1)), 5000);
        for (UiObject2 setting : settings) {
            UiObject2 text = setting.findObject(
                    By.res("android:id/title"));
            if (text.getText().equals("全ての記事を既読にした時の動作")) {
                UiObject2 summary = setting.findObject(
                        By.res("android:id/summary"));
                if (summary.getText().equals("RSS一覧に戻る")) {
                    setting.clickAndWait(Until.newWindow(), 5000);
                    UiObject2 check = device.findObject(
                            By.res("android:id/text1").text("RSS一覧に戻らない"));
                    check.click();
                }
                break;
            }
        }
        device.pressBack();

        // Click first feed
        List<UiObject2> feedTitles = device.wait(Until.findObjects(
                By.res(BuildConfig.APPLICATION_ID, "feedTitle")), 5000);
        if (feedTitles == null) fail("Feed was not found");
        feedTitles.get(0).click();

        // Click fab
        UiObject2 fab = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "fab")), 5000);
        fab.click();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fab.click();

        // Assert article list is still foreground
        UiObject2 allReadButton = device.findObject(
                By.res(BuildConfig.APPLICATION_ID, "all_read"));
        assertNotNull(allReadButton);
    }

    @Test
    public void goLicenseActivity() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Click setting button
        UiObject2 settingButton = device.wait(
                Until.findObject(By.res(BuildConfig.APPLICATION_ID, "setting")), 5000);
        if (settingButton == null) fail("Setting button was not found");
        settingButton.click();

        // Click license info
        UiObject2 settingsList = device.wait(
                Until.findObject(By.clazz(ListView.class)), 5000);
        List<UiObject2> settings = settingsList.wait(
                Until.findObjects(By.clazz(LinearLayout.class).depth(1)), 5000);
        for (UiObject2 setting : settings) {
            UiObject2 text = setting.findObject(
                    By.res("android:id/title"));
            if (text.getText().equals("ライセンス情報")) {
                setting.click();
                break;
            }
        }

        // Assert title
        UiObject2 title = device.wait(Until.findObject(
                By.clazz(TextView.class).text("ライセンス")), 5000);
        assertNotNull(title);
    }
}
