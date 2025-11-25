package com.example.promisclamping.presentation.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.promisclamping.DaftarKompaunScreen
import com.example.promisclamping.presentation.kompaun.KompaunHistoryScreen
import com.example.promisclamping.presentation.kompaun.KompaunListingScreen

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
                TopAppBar(
                    title = { Text("ProMIS Clamping") },
                    actions = {
                        TextButton(onClick = onLogout) {
                            Text("Log Keluar")
                        }
                    }
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
