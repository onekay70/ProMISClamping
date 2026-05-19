package com.example.promisclamping.presentation.kompaun

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.promisclamping.models.KompaunItem
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color

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

    searchPlate: String = "",
    onSearchPlateChange: (String) -> Unit = {},
    searchNoKompaun: String = "",
    onSearchNoKompaunChange: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onClearSearch: () -> Unit = {},
    showSearch: Boolean = false,

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

        if (showSearch) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = searchPlate,
                onValueChange = { value ->
                    onSearchPlateChange(value.uppercase())
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Cari No. Kenderaan") },
                placeholder = { Text("Contoh: VAB1234") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (searchPlate.isNotBlank()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Kosongkan carian"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchClick()
                    }
                )
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = searchNoKompaun,
                onValueChange = { value ->
                    onSearchNoKompaunChange(value.uppercase())
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Cari No. Kompaun") },
                placeholder = { Text("Contoh: KMP202600001") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (searchNoKompaun.isNotBlank()) {
                        IconButton(onClick = { onSearchNoKompaunChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Kosongkan no kompaun"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchClick()
                    }
                )
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onSearchClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Mencari..." else "Cari Kompaun Belum Bayar")
            }

            Spacer(Modifier.height(12.dp))
        }

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
                Text("Tiada rekod kompaun belum bayar.")
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

@Composable
fun KompaunListCard(
    item: KompaunItem,
    onClick: () -> Unit
) {
    val statusText = item.status ?: "-"
    val statusColor = when (statusText.uppercase()) {
        "BARU" -> Color(0xFFFFA000)
        "BAYAR" -> Color(0xFF2E7D32)
        "SELESAI" -> Color(0xFF1565C0)
        "BATAL" -> Color(0xFFC62828)
        else -> Color(0xFF607D8B)
    }

    val statusBg = when (statusText.uppercase()) {
        "BARU" -> Color(0xFFFFF3E0)
        "BAYAR" -> Color(0xFFE8F5E9)
        "SELESAI" -> Color(0xFFE3F2FD)
        "BATAL" -> Color(0xFFFFEBEE)
        else -> Color(0xFFECEFF1)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.noKenderaan ?: "-",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = item.noKompaun ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            HorizontalDivider(color = Color(0xFFE5E7EB))

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoMiniBox(
                    title = "Tarikh",
                    value = "${item.tarikhKompaunStr ?: "-"} ${item.masaKompaunStr ?: ""}".trim(),
                    modifier = Modifier.weight(1f)
                )

                InfoMiniBox(
                    title = "Caj",
                    value = "RM ${item.kadarKompaun?.toString() ?: "-"}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = item.namaPemilik ?: "-",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF374151),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "No. KP: ${item.idPemilik ?: "-"}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Tekan untuk lihat butiran",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF2563EB)
            )
        }
    }
}

@Composable
private fun InfoMiniBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF9FAFB),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6B7280)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF111827),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}