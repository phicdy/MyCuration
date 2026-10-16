package com.phicdy.mycuration.articlelist

import android.content.Intent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExternalBrowserLaunchTest {

    @Test
    fun `chrome is launched adjacent only when the setting is on and the window is large`() {
        val adjacentChrome = ExternalBrowserLaunch(
            packageName = "com.android.chrome",
            flags = Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_TASK,
        )
        val defaultBrowser = ExternalBrowserLaunch(packageName = null, flags = 0)
        // isOpenAdjacentEnabled, isLargeWindow, expected
        val cases = listOf(
            Case(true, true, adjacentChrome),
            Case(true, false, defaultBrowser),
            Case(false, true, defaultBrowser),
            Case(false, false, defaultBrowser),
        )
        cases.forEach { case ->
            assertThat(externalBrowserLaunch(case.isOpenAdjacentEnabled, case.isLargeWindow))
                .describedAs(case.toString())
                .isEqualTo(case.expected)
        }
    }

    private data class Case(
        val isOpenAdjacentEnabled: Boolean,
        val isLargeWindow: Boolean,
        val expected: ExternalBrowserLaunch,
    )
}
