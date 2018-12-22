package com.phicdy.mycuration.uitest

import androidx.test.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.phicdy.mycuration.BuildConfig
import junit.framework.Assert.assertNotNull

internal object TopActivityControl {
    fun clickAddRssButton() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val plusButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "fab_top")), 10000)
        assertNotNull("Plus button was not found", plusButton)
        plusButton.click()
        val fabAddRss = device.wait(Until.findObject(By.res(BuildConfig.APPLICATION_ID, "ll_add_rss")), 3000)
        assertNotNull("Fab RSS was not found", fabAddRss)
        fabAddRss.click()
    }

    fun clickAddFilterButton() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val plusButton = device.findObject(By.res(BuildConfig.APPLICATION_ID, "fab_top"))
        assertNotNull("Plus button was not found", plusButton)
        plusButton.click()
        val fabAddFilter = device.wait(Until.findObject(By.res(BuildConfig.APPLICATION_ID, "ll_add_filter")), 3000)
        assertNotNull("Fab RSS was not found", fabAddFilter)
        fabAddFilter.click()
    }

    fun goToRssTab() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val tab = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "navigation_rss")), 15000)
        tab.click()
    }

    fun goToFilterTab() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val tab = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "navigation_filter")), 15000)
        tab.click()
    }

    fun goToSetting() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val settingButton = device.wait(
                Until.findObject(By.res(BuildConfig.APPLICATION_ID, "setting_top_activity")), 10000)
        assertNotNull("Setting button was not found", settingButton)
        settingButton.clickAndWait(Until.newWindow(), 5000)
    }
}
