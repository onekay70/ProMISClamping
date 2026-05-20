package com.example.promisclamping.presentation.kompaun

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.promisclamping.models.KompaunItem
import com.example.promisclamping.ui.theme.NavyHeader
import com.example.promisclamping.ui.theme.SecondaryBlue
import com.example.promisclamping.ui.theme.TextMuted

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
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = NavyHeader
        )

        Spacer(Modifier.height(10.dp))

        if (showSearch) {
            SearchKompaunCard(
                searchPlate = searchPlate,
                onSearchPlateChange = onSearchPlateChange,
                searchNoKompaun = searchNoKompaun,
                onSearchNoKompaunChange = onSearchNoKompaunChange,
                onSearchClick = onSearchClick,
                onClearSearch = onClearSearch,
                isLoading = isLoading
            )

            Spacer(Modifier.height(14.dp))
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
                EmptyInfoCard(
                    title = "Tiada rekod dipaparkan",
                    message = error
                )
            }

            items.isEmpty() -> {
                EmptyInfoCard(
                    title = "Tiada rekod",
                    message = if (showSearch) {
                        "Cuba cari menggunakan No. Kenderaan atau No. Kompaun."
                    } else {
                        "Tiada rekod kompaun dijumpai."
                    }
                )
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 120.dp),
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
                            Button(
                                onClick = onLoadMore,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SecondaryBlue,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(if (isLoading) "Memuat..." else "Muat Lagi")
                            }
                        }
                    }

                    if (error != null) {
                        item {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchKompaunCard(
    searchPlate: String,
    onSearchPlateChange: (String) -> Unit,
    searchNoKompaun: String,
    onSearchNoKompaunChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onClearSearch: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Carian Kompaun",
                style = MaterialTheme.typography.titleMedium,
                color = NavyHeader
            )

            Text(
                text = "Cari berdasarkan No. Kenderaan atau No. Kompaun.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )

            OutlinedTextField(
                value = searchPlate,
                onValueChange = { value -> onSearchPlateChange(value.uppercase()) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("No. Kenderaan") },
                placeholder = { Text("Contoh: VAB1234") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchPlate.isNotBlank()) {
                        IconButton(onClick = { onSearchPlateChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Kosongkan No. Kenderaan")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchClick() })
            )

            OutlinedTextField(
                value = searchNoKompaun,
                onValueChange = { value -> onSearchNoKompaunChange(value.uppercase()) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("No. Kompaun") },
                placeholder = { Text("Contoh: KMP202600001") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchNoKompaun.isNotBlank()) {
                        IconButton(onClick = { onSearchNoKompaunChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Kosongkan No. Kompaun")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchClick() })
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onClearSearch,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isLoading
                ) {
                    Text("Reset")
                }

                Button(
                    onClick = onSearchClick,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(if (isLoading) "Mencari..." else "Cari")
                }
            }
        }
    }
}

@Composable
private fun EmptyInfoCard(
    title: String,
    message: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = NavyHeader
            )
            Text(
                text = message ?: "-",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
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
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                Column(modifier = Modifier.weight(1f)) {
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
                        color = TextMuted,
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
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Tekan untuk lihat butiran",
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryBlue
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
                color = TextMuted
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
