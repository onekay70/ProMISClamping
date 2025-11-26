package com.example.promisclamping.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFFA86B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD6B5),
    onPrimaryContainer = Color(0xFF3B1B0B),

    secondary = Color(0xFF915F3F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE1C9),
    onSecondaryContainer = Color(0xFF3A1908),

    background = Color(0xFFFDF7F3),
    onBackground = Color(0xFF2B1207),

    surface = Color.White,
    onSurface = Color(0xFF2B1207),

    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun ProMISClampingTheme(
    darkTheme: Boolean = false, // ignore system setting
    content: @Composable () -> Unit
) {
    val colors = LightColorScheme // always light

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}