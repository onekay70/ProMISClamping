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

enum class MainTab(val title: String) {
    KOMPAUN("Daftar Kompaun"),
    HISTORY("Sejarah"),
    SETTINGS("Tetapan")
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
        // ✅ use the padding so the warning disappears
        when (selectedTab) {
            MainTab.KOMPAUN -> DaftarKompaunScreen()
            MainTab.HISTORY -> HistoryPlaceholder(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
            MainTab.SETTINGS -> SettingsPlaceholder(
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
