package com.example.promisclamping.ui.common

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

import com.example.promisclamping.ui.theme.TextMuted


@Composable
fun PromisFooter(
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Transparent,              // or PageBackground
        modifier = modifier
    ) {
        Text(
            text = "2024 – 2025 © Bahagian Pengurusan Hartanah.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = WindowInsets.navigationBars
                    .asPaddingValues().calculateBottomPadding()) // avoid nav bar
        )
    }
}
