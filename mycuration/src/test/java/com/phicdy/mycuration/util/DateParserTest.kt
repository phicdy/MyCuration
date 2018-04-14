package com.phicdy.mycuration.util

import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class DateParserTest {
    @Test
    fun testTimeZone() {
        assertThat(DateParser.changeToJapaneseDate("Mon, 01 Jan 2018 01:23:45 +0900"),
                `is`(1514737425000L))
    }

    @Test
    fun testNoTimeZone() {
        assertThat(DateParser.changeToJapaneseDate("2018-01-01 01:23:45"),
                `is`(1514737425000L))
    }

    @Test
    fun testWithTandTimeZone() {
        //2014-07-27T14:38:34+09:00
        assertThat(DateParser.changeToJapaneseDate("2018-01-01T01:23:45+0900"),
                `is`(1514737425000L))
    }

    @Test
    fun testWithTandTimeZoneWithColon() {
        //2014-07-27T14:38:34+09:00
        assertThat(DateParser.changeToJapaneseDate("2018-01-01T01:23:45+09:00"),
                `is`(1514737425000L))
    }

    @Test
    fun testWithTandZ() {
        assertThat(DateParser.changeToJapaneseDate("2018-01-01T01:23:45Z"),
                `is`(1514737425000L))
    }

    @Test
    fun testInvalid() {
        assertThat(DateParser.changeToJapaneseDate("gjrajgh@"),
                `is`(0L))
    }

    @Test
    fun testEmpty() {
        assertThat(DateParser.changeToJapaneseDate(""),
                `is`(0L))
    }
}