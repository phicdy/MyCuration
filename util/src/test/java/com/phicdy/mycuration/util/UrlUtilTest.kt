package com.phicdy.mycuration.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class UrlUtilTest {

    @Test
    fun testRemoveUrlParameter() {
        val removedUrl = UrlUtil.removeUrlParameter("http://harofree.blog.fc2.com/?ps")
        assertThat(removedUrl).isEqualTo("http://harofree.blog.fc2.com/")
    }

    @Test
    fun testHasParameterUrl() {
        assertThat(UrlUtil.hasParameterUrl("http://www.xxx.com/?aaa")).isTrue()
        assertThat(UrlUtil.hasParameterUrl("https://www.xxx.com/?aaa")).isTrue()
        assertThat(UrlUtil.hasParameterUrl("http://www.xxx.com/aaa/?bbb")).isTrue()
        assertThat(UrlUtil.hasParameterUrl("http://www.xxx.com/aaa")).isFalse()
        assertThat(UrlUtil.hasParameterUrl("http://www.xxx.com?/aaa")).isFalse()
    }
}
