package com.phicdy.mycuration.articlelist

import android.content.Intent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExternalBrowserIntentTest {

    @Test
    fun `the browser is launched adjacent only when the setting is on and the window is large`() {
        val adjacentFlags = Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_TASK
        // isOpenAdjacentEnabled, isLargeWindow, expected
        val cases = listOf(
            Case(true, true, adjacentFlags),
            Case(true, false, 0),
            Case(false, true, 0),
            Case(false, false, 0),
        )
        cases.forEach { case ->
            assertThat(externalBrowserIntentFlags(case.isOpenAdjacentEnabled, case.isLargeWindow))
                .describedAs(case.toString())
                .isEqualTo(case.expected)
        }
    }

    private data class Case(
        val isOpenAdjacentEnabled: Boolean,
        val isLargeWindow: Boolean,
        val expected: Int,
    )
}
