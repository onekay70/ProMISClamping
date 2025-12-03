package com.example.promisclamping.presentation.kompaun

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.promisclamping.data.local.TokenStore
import com.example.promisclamping.models.KompaunItem
import com.example.promisclamping.models.KompaunListResponse
import com.example.promisclamping.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun KompaunListingScreen(
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val tokenStore = remember { TokenStore(ctx) }
    val scope = rememberCoroutineScope()

    val pageSize = 10

    var items by remember { mutableStateOf<List<KompaunItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var pageNo by remember { mutableStateOf(1) }
    var hasMore by remember { mutableStateOf(true) }

    var selectedKompaun by remember { mutableStateOf<KompaunItem?>(null) }

    suspend fun loadPage(page: Int, append: Boolean) {
        val authId = tokenStore.userId ?: ""
        if (authId.isBlank()) {
            error = "Sila log masuk semula."
            hasMore = false
            return
        }

        isLoading = true
        error = null

        try {
            val resp = ApiClient.kompaun(ctx).getKompaunList(
                status = "BARU,BAYAR",
                pageNo = page,
                pageSize = pageSize,
                authId = authId
            )

            if (resp.isSuccessful) {
                val body = resp.body()
                val newItems = body?.data ?: emptyList()

                items = if (append) items + newItems else newItems

                // 👇 if we received less than pageSize, assume no more pages
                hasMore = newItems.size >= pageSize
            } else {
                error = "Gagal memuat data (${resp.code()})"
                hasMore = false
            }
        } catch (t: Throwable) {
            error = t.message ?: "Ralat rangkaian"
            hasMore = false
        } finally {
            isLoading = false
        }
    }

    fun reloadFromStart() {
        scope.launch {
            isLoading = true
            error = null
            pageNo = 1
            hasMore = true
            loadPage(page = 1, append = false)
        }
    }

    LaunchedEffect(Unit) {
//        loadPage(page = 1, append = false)
        reloadFromStart()
    }

    if (selectedKompaun != null) {
        KompaunDetailScreen(
            kompaun = selectedKompaun!!,
            onBack = {
                selectedKompaun = null
                reloadFromStart()   // 🔄 refresh list when detail closes
            }
        )
    } else {
        KompaunListContent(
            title = "Senarai Kompaun",
            isLoading = isLoading,
            error = error,
            items = items,
            modifier = modifier.fillMaxSize(),
            onItemClick = { selectedKompaun = it },
            canLoadMore = hasMore,
            onLoadMore = {
                if (!isLoading && hasMore) {
                    scope.launch {
                        pageNo += 1
                        loadPage(page = pageNo, append = true)
                    }
                }
            }
        )
    }
}