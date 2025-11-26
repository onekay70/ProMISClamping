package com.example.promisclamping.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.promisclamping.DaftarKompaunScreen
import com.example.promisclamping.R
import com.example.promisclamping.presentation.kompaun.KompaunHistoryScreen
import com.example.promisclamping.presentation.kompaun.KompaunListingScreen
import com.example.promisclamping.ui.common.PromisFooter
import com.example.promisclamping.ui.theme.NavyHeader

enum class MainTab(val title: String) {
    KOMPAUN("Daftar Kompaun"),
    KOMPAUN_LISTING("Senarai Kompaun"),
    KOMPAUN_HISTORY("Sejarah Kompaun")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabs(
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.KOMPAUN) }

    Scaffold(
        topBar = {
            Column {
                PromisTopBar(
                    title = "ProMIS Clamping",
                    onLogoutClick = onLogout
                )
                TabRow(selectedTabIndex = selectedTab.ordinal) {
                    MainTab.values().forEach { tab ->
                        Tab(
                            selected = tab == selectedTab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab.title) }
                        )
                    }
                }
            }
        },
        bottomBar = {
            PromisFooter()
        }
    ) { padding ->
        when (selectedTab) {
            MainTab.KOMPAUN -> DaftarKompaunScreen()

            MainTab.KOMPAUN_LISTING -> KompaunListingScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )

            MainTab.KOMPAUN_HISTORY -> KompaunHistoryScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }
}

@Composable
private fun HistoryPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text("Sejarah (akan diisi dengan data offline / senarai kompaun)")
    }
}

@Composable
private fun SettingsPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text("Tetapan (contoh: pilihan server, info pengguna)")
    }
}

@Composable
fun PromisTopBar(
    title: String,
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Surface(
        color = NavyHeader,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(WindowInsets.statusBars.asPaddingValues())   // 👈 FIX HERE
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: small app icon or menu
            IconButton(onClick = onMenuClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_promis_home), // export from favicon or your SVG
                    contentDescription = "Utama",
                    tint = Color.White
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            // Right: simple logout / profile
            TextButton(onClick = onLogoutClick) {
                Text(
                    text = "Log Keluar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFFCDD2) // slightly red to match web
                )
            }
        }
    }
}