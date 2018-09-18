package com.phicdy.mycuration.uitest

import android.support.test.InstrumentationRegistry
import android.support.test.uiautomator.By
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiObject2
import android.support.test.uiautomator.Until

import com.phicdy.mycuration.BuildConfig
import junit.framework.Assert.assertNotNull

import junit.framework.Assert.fail

internal object TopActivityControl {
    fun clickAddRssButton() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val plusButton = device.wait(Until.findObject(
                By.res(BuildConfig.APPLICATION_ID, "fab_top")), 5000)
        assertNotNull("Plus button was not found", plusButton)
        plusButton.click()
        val fabAddRss = device.findObject(By.res(BuildConfig.APPLICATION_ID, "ll_add_rss"))
        assertNotNull("Fab RSS was not found", fabAddRss)
        fabAddRss.click()
    }

    fun clickAddFilterButton() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val plusButton = device.findObject(By.res(BuildConfig.APPLICATION_ID, "fab_top"))
        assertNotNull("Plus button was not found", plusButton)
        plusButton.click()
        val fabAddFilter = device.findObject(By.res(BuildConfig.APPLICATION_ID, "ll_add_filter"))
        assertNotNull("Fab RSS was not found", fabAddFilter)
        fabAddFilter.click()
    }
}
