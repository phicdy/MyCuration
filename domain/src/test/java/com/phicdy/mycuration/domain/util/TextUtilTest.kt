package com.phicdy.mycuration.domain.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TextUtilTest {

    @Test
    fun testRemoveLineFeed() {
        assertThat(TextUtil.removeLineFeed("aaa\r")).isEqualTo("aaa")
        assertThat(TextUtil.removeLineFeed("aaa\n")).isEqualTo("aaa")
        assertThat(TextUtil.removeLineFeed("aaa\t")).isEqualTo("aaa")
        assertThat(TextUtil.removeLineFeed("aaa\r\n")).isEqualTo("aaa")

        assertThat(TextUtil.removeLineFeed("a\ra\na")).isEqualTo("aaa")
        assertThat(TextUtil.removeLineFeed("a\ra\ta")).isEqualTo("aaa")
        assertThat(TextUtil.removeLineFeed("a\ra\r\na")).isEqualTo("aaa")
        assertThat(TextUtil.removeLineFeed("a\na\ta")).isEqualTo("aaa")
        assertThat(TextUtil.removeLineFeed("a\na\r\na")).isEqualTo("aaa")
        assertThat(TextUtil.removeLineFeed("a\ta\r\na")).isEqualTo("aaa")
    }
}
