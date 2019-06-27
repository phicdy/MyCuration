package com.phicdy.mycuration.util

import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.phicdy.mycuration.data.preference.PreferenceHelper

fun AppCompatActivity.getThemeColor(@AttrRes res: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(res, typedValue, true)
    return typedValue.data
}

fun AppCompatActivity.changeTheme() {
    delegate.setLocalNightMode(when (PreferenceHelper.theme) {
        PreferenceHelper.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        PreferenceHelper.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
        else -> AppCompatDelegate.MODE_NIGHT_NO
    })
}
