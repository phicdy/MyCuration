package com.phicdy.mycuration.resource

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightThemeColors = lightColors(
    primary = White,
    primaryVariant = White,
    secondary = Green700,
    background = White,
    surface = White,
    onPrimary = Black,
    onSecondary = White,
    onSurface = Black,
    onBackground = Black
)

private val DarkThemeColors = darkColors(
    primary = Black900,
    primaryVariant = Black,
    secondary = Green700,
    background = Black,
    surface = Black,
    onPrimary = White,
    onSecondary = Black,
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