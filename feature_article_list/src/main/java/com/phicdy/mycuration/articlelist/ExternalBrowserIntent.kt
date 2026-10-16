package com.phicdy.mycuration.articlelist

import android.content.Intent

/**
 * Flags for the intent that opens an article in the external browser. When the user enabled the
 * setting and the window is large, the browser is launched in the adjacent window (split screen)
 * on a best-effort basis: Android 12L and later can enter split screen from fullscreen, older
 * versions only when already in split screen. Otherwise the browser opens as before.
 */
internal fun externalBrowserIntentFlags(isOpenAdjacentEnabled: Boolean, isLargeWindow: Boolean): Int =
    if (isOpenAdjacentEnabled && isLargeWindow) {
        Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_TASK
    } else {
        0
    }
