package com.example.promisclamping.presentation.kompaun

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.promisclamping.data.local.TokenStore
import com.example.promisclamping.models.KompaunItem
import com.example.promisclamping.network.ApiClient
import com.example.promisclamping.presentation.kompaun.KompaunListContent
import com.example.promisclamping.presentation.kompaun.KompaunListUiState

@Composable
fun KompaunHistoryScreen(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val tokenStore = remember { TokenStore(ctx) }

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<KompaunItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        val authId = tokenStore.userId ?: ""

        try {
            val resp = ApiClient.kompaun(ctx).getKompaunList(
                status = "BATAL,SELESAI",
                pageNo = 1,
                pageSize = 10,
                authId = authId
            )

            if (resp.isSuccessful) {
                val body = resp.body()
                items = body?.data ?: emptyList()
            } else {
                error = "Gagal (${resp.code()})"
            }
        } catch (t: Throwable) {
            error = t.message ?: "Ralat"
        }

        isLoading = false
    }

    KompaunListContent(
        title = "Sejarah Kompaun",
        isLoading = isLoading,
        error = error,
        items = items,
        modifier = modifier
    )
}
