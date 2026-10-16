package com.phicdy.mycuration.articlelist.ui

/**
 * Decides whether the article list can show the tapped article next to the list.
 */
internal object TwoPanePolicy {
    /** Narrower windows, e.g. phones in portrait, show the list only. */
    const val MIN_WIDTH_DP: Int = 600

    /** Lower windows, e.g. phones in landscape, show the list only. */
    const val MIN_HEIGHT_DP: Int = 480

    /**
     * Two panes are used only when the window is large enough and articles are opened in the
     * app ("open internal" setting). Otherwise articles keep opening in Custom Tabs or the
     * external browser.
     */
    fun canShowTwoPanes(widthDp: Int, heightDp: Int, isOpenInternal: Boolean): Boolean =
        isOpenInternal && widthDp >= MIN_WIDTH_DP && heightDp >= MIN_HEIGHT_DP
}
