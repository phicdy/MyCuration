package com.phicdy.mycuration.uitest;

import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;

import com.phicdy.mycuration.BuildConfig;

import static junit.framework.Assert.fail;

class TopActivityControl {
    static void clickAddRssButton() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject2 plusButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "fab_top")), 5000);
        if (plusButton == null) fail("Plus button was not found");
        plusButton.click();
        UiObject2 fabAddRss = device.findObject(By.res(BuildConfig.APPLICATION_ID, "ll_add_rss"));
        if (fabAddRss == null) fail("Fab RSS was not found");
        fabAddRss.click();
    }

    static void clickAddFilterButton() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject2 plusButton = device.findObject(By.res(BuildConfig.APPLICATION_ID, "fab_top"));
        if (plusButton == null) fail("Plus button was not found");
        plusButton.click();
        UiObject2 fabAddFilter = device.findObject(By.res(BuildConfig.APPLICATION_ID, "ll_add_filter"));
        if (fabAddFilter == null) fail("Fab RSS was not found");
        fabAddFilter.click();
    }
}
