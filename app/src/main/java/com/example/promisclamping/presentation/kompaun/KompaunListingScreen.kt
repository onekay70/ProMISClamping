package com.example.promisclamping.presentation.kompaun

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.promisclamping.data.local.TokenStore
import com.example.promisclamping.models.KompaunItem
import com.example.promisclamping.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun KompaunListingScreen(
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val tokenStore = remember { TokenStore(ctx) }
    val scope = rememberCoroutineScope()

    val defaultPageSize = 5
    val searchPageSize = 10

    var items by remember { mutableStateOf<List<KompaunItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var pageNo by remember { mutableStateOf(1) }
    var hasMore by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    var searchPlate by remember { mutableStateOf("") }
    var searchNoKompaun by remember { mutableStateOf("") }

    var selectedKompaun by remember { mutableStateOf<KompaunItem?>(null) }

    suspend fun loadPage(page: Int, append: Boolean, isSearch: Boolean) {
        val authId = tokenStore.userId ?: ""
        if (authId.isBlank()) {
            error = "Sila log masuk semula."
            hasMore = false
            return
        }

        val plate = searchPlate.trim().uppercase()
        val noKompaun = searchNoKompaun.trim().uppercase()

        if (isSearch && plate.isBlank() && noKompaun.isBlank()) {
            items = emptyList()
            error = "Sila masukkan No. Kenderaan atau No. Kompaun."
            hasMore = false
            return
        }

        isLoading = true
        error = null

        try {
            val pageSize = if (isSearch) searchPageSize else defaultPageSize

            val resp = ApiClient.kompaun(ctx).getKompaunList(
                status = "BARU,BAYAR",
                pageNo = page,
                pageSize = pageSize,
                authId = authId,
                noKenderaan = if (isSearch) plate.ifBlank { null } else null,
                noKompaun = if (isSearch) noKompaun.ifBlank { null } else null,
                sortBy = "tarikhKompaun",
                sortOrder = "desc"
            )

            if (resp.isSuccessful) {
                val newItems = resp.body()?.data ?: emptyList()

                items = if (append) items + newItems else newItems
                hasMore = if (isSearch) newItems.size >= pageSize else false
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

    fun searchFromStart() {
        scope.launch {
            pageNo = 1
            hasSearched = true
            hasMore = true
            loadPage(page = 1, append = false, isSearch = true)
        }
    }

    fun clearSearch() {
        scope.launch {
            searchPlate = ""
            searchNoKompaun = ""
            error = null
            pageNo = 1
            hasSearched = false
            hasMore = false
            loadPage(page = 1, append = false, isSearch = false)
        }
    }

    fun reloadCurrentList() {
        scope.launch {
            pageNo = 1
            loadPage(page = 1, append = false, isSearch = hasSearched)
        }
    }

    LaunchedEffect(Unit) {
        loadPage(page = 1, append = false, isSearch = false)
    }

    if (selectedKompaun != null) {
        KompaunDetailScreen(
            kompaun = selectedKompaun!!,
            onBack = {
                selectedKompaun = null
                reloadCurrentList()
            }
        )
    } else {
        KompaunListContent(
            title = if (hasSearched) "Keputusan Carian Kompaun" else "Senarai Kompaun Belum Selesai",
            isLoading = isLoading,
            error = error,
            items = items,
            modifier = modifier.fillMaxSize(),
            searchPlate = searchPlate,
            onSearchPlateChange = { searchPlate = it },
            searchNoKompaun = searchNoKompaun,
            onSearchNoKompaunChange = { searchNoKompaun = it },
            onSearchClick = { searchFromStart() },
            onClearSearch = { clearSearch() },
            showSearch = true,
            onItemClick = { selectedKompaun = it },
            canLoadMore = hasMore,
            onLoadMore = {
                if (!isLoading && hasMore) {
                    scope.launch {
                        pageNo += 1
                        loadPage(page = pageNo, append = true, isSearch = hasSearched)
                    }
                }
            }
        )
    }
}
