package com.phicdy.mycuration.articlelist.ui

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TwoPanePolicyTest {

    @Test
    fun `two panes are shown only for wide and tall windows with the internal setting`() {
        // widthDp, heightDp, isOpenInternal, expected
        val cases = listOf(
            Case(600, 480, true, true),
            Case(599, 480, true, false),
            Case(600, 479, true, false),
            Case(599, 479, true, false),
            Case(600, 480, false, false),
            Case(599, 480, false, false),
            Case(600, 479, false, false),
            Case(599, 479, false, false),
            Case(1280, 800, true, true),
        )
        cases.forEach { case ->
            assertThat(TwoPanePolicy.canShowTwoPanes(case.widthDp, case.heightDp, case.isOpenInternal))
                .describedAs(case.toString())
                .isEqualTo(case.expected)
        }
    }

    @Test
    fun `large windows are at least 600dp wide and 480dp tall`() {
        // widthDp, heightDp, expected
        val cases = listOf(
            SizeCase(600, 480, true),
            SizeCase(599, 480, false),
            SizeCase(600, 479, false),
            SizeCase(599, 479, false),
            SizeCase(1280, 800, true),
        )
        cases.forEach { case ->
            assertThat(TwoPanePolicy.isLargeWindow(case.widthDp, case.heightDp))
                .describedAs(case.toString())
                .isEqualTo(case.expected)
        }
    }

    private data class SizeCase(
        val widthDp: Int,
        val heightDp: Int,
        val expected: Boolean,
    )

    private data class Case(
        val widthDp: Int,
        val heightDp: Int,
        val isOpenInternal: Boolean,
        val expected: Boolean,
    )
}
