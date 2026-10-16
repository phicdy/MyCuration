package com.phicdy.mycuration.articlelist

import android.content.Intent
import android.net.Uri

/**
 * How to open an article in the external browser.
 *
 * @param packageName the browser to target, or null for the default browser
 * @param flags flags added to the ACTION_VIEW intent
 */
internal data class ExternalBrowserLaunch(
    val packageName: String?,
    val flags: Int,
) {
    fun toIntent(uri: Uri): Intent = Intent(Intent.ACTION_VIEW, uri)
        .setPackage(packageName)
        .addFlags(flags)
}

internal const val CHROME_PACKAGE_NAME: String = "com.android.chrome"

/** Opens the default browser as before, without the adjacent flags. */
internal val defaultBrowserLaunch: ExternalBrowserLaunch = ExternalBrowserLaunch(packageName = null, flags = 0)

/**
 * Decides how to open an article in the external browser. When the user enabled the setting and
 * the window is large, Chrome is launched in the adjacent window (split screen) on a best-effort
 * basis: Android 12L and later can enter split screen from fullscreen, older versions only when
 * already in split screen. Chrome is targeted because some browsers, e.g. Firefox, route the
 * intent through an activity that breaks the adjacent launch. Otherwise the default browser opens
 * as before.
 */
internal fun externalBrowserLaunch(isOpenAdjacentEnabled: Boolean, isLargeWindow: Boolean): ExternalBrowserLaunch =
    if (isOpenAdjacentEnabled && isLargeWindow) {
        ExternalBrowserLaunch(
            packageName = CHROME_PACKAGE_NAME,
            flags = Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_TASK,
        )
    } else {
        defaultBrowserLaunch
    }
