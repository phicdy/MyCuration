package com.phicdy.mycuration.feature.util

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

private val systemBarInsetTypes: Int =
    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()

/**
 * Adds the system bar and display cutout insets to the padding of this view, so that its content
 * stays clear of the system bars while its background is drawn behind them.
 *
 * The left and right insets are always applied, for the navigation bar on the side in landscape.
 * Set [top] for a view at the top of the screen such as an app bar, and [bottom] for a view at the
 * bottom of the screen such as a list. A list should also set `clipToPadding` to false, so that
 * it scrolls behind the navigation bar.
 */
fun View.applySystemBarInsetsAsPadding(top: Boolean = false, bottom: Boolean = false) {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(systemBarInsetTypes)
        view.updatePadding(
            left = initialLeft + insets.left,
            top = initialTop + if (top) insets.top else 0,
            right = initialRight + insets.right,
            bottom = initialBottom + if (bottom) insets.bottom else 0,
        )
        windowInsets
    }
    doOnAttach { it.requestApplyInsets() }
}

/**
 * Adds the system bar and display cutout insets to the margins of this view, for a floating view
 * such as a floating action button that must not be covered by the navigation bar.
 *
 * The left and right insets are always applied. Set [bottom] for a view at the bottom of the screen.
 */
fun View.applySystemBarInsetsAsMargin(bottom: Boolean = false) {
    val initialMargins = layoutParams as ViewGroup.MarginLayoutParams
    val initialLeft = initialMargins.leftMargin
    val initialRight = initialMargins.rightMargin
    val initialBottom = initialMargins.bottomMargin
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(systemBarInsetTypes)
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            leftMargin = initialLeft + insets.left
            rightMargin = initialRight + insets.right
            bottomMargin = initialBottom + if (bottom) insets.bottom else 0
        }
        windowInsets
    }
    doOnAttach { it.requestApplyInsets() }
}
