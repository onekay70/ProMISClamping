package com.example.promisclamping.presentation.kompaun

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            items.isEmpty() -> {
                Text("Tiada rekod.")
            }

            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = item.noKompaun ?: "-",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "No Kenderaan: ${item.noKenderaan ?: "-"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "No KP: ${item.idPemilik ?: "-"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Nama: ${item.namaPemilik ?: "-"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Tarikh: ${item.tarikhKompaunStr ?: "-"} ${item.masaKompaunStr ?: ""}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Status: ${item.status ?: "-"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
//                                item.tarikhMasa?.let {
//                                    Text(
//                                        text = "Tarikh: $it",
//                                        style = MaterialTheme.typography.bodySmall
//                                    )
//                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
