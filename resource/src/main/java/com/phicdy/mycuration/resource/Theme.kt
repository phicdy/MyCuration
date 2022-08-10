package com.phicdy.mycuration.resource

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightThemeColors = lightColors(
    // TopAppBar background, TextButton textColor
    primary = Black900,
    primaryVariant = Black,
    secondary = Green700,
    background = White,
    // DropdownMenu and AlertDialog background
    surface = White,
    // TopAppBar, DropdownMenu and AlertDialog textColor
    onPrimary = Black,
    onSecondary = White,
    onSurface = Black,
    onBackground = Black
)

private val DarkThemeColors = darkColors(
    primary = White,
    primaryVariant = White,
    secondary = Green700,
    background = Black,
    // DropdownMenu and AlertDialog background, TopAppBar background
    surface = Black900,
    onPrimary = White,
    onSecondary = Black,
    // TopAppBar, DropdownMenu and AlertDialog textColor
    onSurface = White,
    onBackground = White
)

@Composable
fun MyCurationTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable () -> Unit
) {
    MaterialTheme(
            colors = if (darkTheme) DarkThemeColors else LightThemeColors,
            content = content
    )
}