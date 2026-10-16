package com.phicdy.mycuration.articlelist

import android.content.Intent

/**
 * Flags for the intent that opens an article in the external browser. On large windows the
 * browser is launched in the adjacent window (split screen) on a best-effort basis: Android 12L
 * and later can enter split screen from fullscreen, older versions only when already in split
 * screen. Other windows open the browser as before.
 */
internal fun externalBrowserIntentFlags(isLargeWindow: Boolean): Int =
    if (isLargeWindow) Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_TASK else 0
