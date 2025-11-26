package com.example.promisclamping.presentation.kompaun

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.promisclamping.models.KompaunItem

data class KompaunListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val items: List<KompaunItem> = emptyList()
)

@Composable
fun KompaunListContent(
    title: String,
    isLoading: Boolean,
    error: String?,
    items: List<KompaunItem>,
    modifier: Modifier = Modifier,
    onItemClick: (KompaunItem) -> Unit,
    onLoadMore: (() -> Unit)? = null,
    canLoadMore: Boolean = false
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        when {
            isLoading && items.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null && items.isEmpty() -> {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            items.isEmpty() -> {
                Text("Tiada rekod.")
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items) { item ->
                        KompaunListCard(
                            item = item,
                            onClick = { onItemClick(item) }
                        )
                    }

                    // 👇 "Muat Lagi" as the last list item
                    if (canLoadMore && onLoadMore != null) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = onLoadMore,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading
                            ) {
                                Text(if (isLoading) "Memuat..." else "Muat Lagi")
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                if (error != null && items.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * Single card – 2-column layout as per your sample screenshot.
 */
@Composable
fun KompaunListCard(
    item: KompaunItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT COLUMN
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.noKompaun ?: "-",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("No Kenderaan: ${item.noKenderaan ?: "-"}")
                Text("No KP: ${item.idPemilik ?: "-"}")
                Text("Nama: ${item.namaPemilik ?: "-"}")
            }

            // RIGHT COLUMN
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tarikh: ${item.tarikhKompaunStr ?: "-"} ${item.masaKompaunStr ?: ""}".trim(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text("Status: ${item.status ?: "-"}")
                Text("Kadar Kompaun: ${item.kadarKompaun?.toString() ?: "-"}")
            }
        }
    }
}