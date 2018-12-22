package com.phicdy.mycuration.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DateParserTest {
    @Test
    fun testTimeZone() {
        assertThat(DateParser.changeToJapaneseDate("Mon, 01 Jan 2018 01:23:45 +0900")).isEqualTo((1514737425000L))
    }

    @Test
    fun testNoTimeZone() {
        assertThat(DateParser.changeToJapaneseDate("2018-01-01 01:23:45")).isEqualTo((1514737425000L))
    }

    @Test
    fun testWithTandTimeZone() {
        //2014-07-27T14:38:34+09:00
        assertThat(DateParser.changeToJapaneseDate("2018-01-01T01:23:45+0900")).isEqualTo((1514737425000L))
    }

    @Test
    fun testWithTandTimeZoneWithColon() {
        //2014-07-27T14:38:34+09:00
        assertThat(DateParser.changeToJapaneseDate("2018-01-01T01:23:45+09:00")).isEqualTo((1514737425000L))
    }

    @Test
    fun testWithTandTimeZoneWithColonAndMillisecond() {
        //2014-07-27T14:38:34+09:00
        assertThat(DateParser.changeToJapaneseDate("2018-01-01T01:23:45.000+09:00")).isEqualTo((1514737425000L))
    }

    @Test
    fun testWithTandZ() {
        assertThat(DateParser.changeToJapaneseDate("2018-01-01T01:23:45Z")).isEqualTo((1514737425000L))
    }

    @Test
    fun testInvalid() {
        assertThat(DateParser.changeToJapaneseDate("gjrajgh@")).isEqualTo((0L))
    }

    @Test
    fun testEmpty() {
        assertThat(DateParser.changeToJapaneseDate("")).isEqualTo((0L))
    }
}