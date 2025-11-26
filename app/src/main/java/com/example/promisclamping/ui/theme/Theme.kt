package com.example.promisclamping.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PromisColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = PrimaryGreen.copy(alpha = 0.12f),
    onPrimaryContainer = PrimaryGreen,

    secondary = SecondaryBlue,
    onSecondary = Color.White,

    background = PageBackground,
    onBackground = TextDark,

    surface = CardBackground,
    onSurface = TextDark,

    error = DangerOrange,
    onError = Color.White
)

@Composable
fun ProMISClampingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PromisColorScheme,
        typography = PromisTypography,
        content = content
    )
}
