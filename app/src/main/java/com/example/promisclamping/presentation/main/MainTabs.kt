package com.example.promisclamping.presentation.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.promisclamping.DaftarKompaunScreen
import com.example.promisclamping.R
import com.example.promisclamping.presentation.kompaun.KompaunHistoryScreen
import com.example.promisclamping.presentation.kompaun.KompaunListingScreen
import com.example.promisclamping.ui.common.PromisFooter
import com.example.promisclamping.ui.theme.NavyHeader
import com.example.promisclamping.ui.theme.NavyHeaderDark
import androidx.compose.foundation.layout.statusBarsPadding

enum class MainTab(val title: String) {
    KOMPAUN("Daftar"),
    KOMPAUN_LISTING("Senarai"),
    KOMPAUN_HISTORY("Sejarah")
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

                Surface(
                    color = NavyHeader,
                    shadowElevation = 6.dp
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = NavyHeader,
                        contentColor = Color.White,
                        indicator = {},
                        divider = {}
                    ) {
                        MainTab.values().forEach { tab ->
                            val selected = tab == selectedTab

                            Tab(
                                selected = selected,
                                onClick = { selectedTab = tab },
                                text = {
                                    Surface(
                                        color = if (selected) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(999.dp),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = tab.title,
                                            color = if (selected) NavyHeader else Color(0xFFDDE7FF),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            )
                        }
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
fun PromisTopBar(
    title: String,
    onLogoutClick: () -> Unit
) {
    Surface(
        color = NavyHeaderDark,
        shadowElevation = 4.dp
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .padding(top = 32.dp)
                .padding(bottom = 12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_promis_home),
                contentDescription = "Logo",
                tint = Color.Unspecified,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(32.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            TextButton(
                onClick = onLogoutClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Log Keluar")
            }
        }
    }
}
