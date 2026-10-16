package com.phicdy.mycuration.articlelist

import android.content.Intent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExternalBrowserIntentTest {

    @Test
    fun `large windows launch the browser in the adjacent window as a new task`() {
        assertThat(externalBrowserIntentFlags(isLargeWindow = true))
            .isEqualTo(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @Test
    fun `other windows add no flags`() {
        assertThat(externalBrowserIntentFlags(isLargeWindow = false)).isEqualTo(0)
    }
}
