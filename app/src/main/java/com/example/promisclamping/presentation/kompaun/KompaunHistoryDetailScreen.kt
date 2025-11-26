package com.example.promisclamping.presentation.kompaun

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.promisclamping.models.KompaunItem

@Composable
fun KompaunHistoryDetailScreen(
    kompaun: KompaunItem,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Kembali"
                )
            }
            Text(
                text = kompaun.noKompaun ?: "Sejarah Kompaun",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Divider()

        ReadOnlyField("No Kompaun", kompaun.noKompaun ?: "-")
        ReadOnlyField("No Kenderaan", kompaun.noKenderaan ?: "-")
        ReadOnlyField("Pemilik", kompaun.namaPemilik ?: "-")
        ReadOnlyField("Jenis Kenderaan", kompaun.jenisKenderaan ?: "-")
        ReadOnlyField("Tempat", kompaun.tempat ?: "-")
        ReadOnlyField("Lokasi", kompaun.lokasi ?: "-")
        ReadOnlyField(
            "Tarikh & Masa",
            "${kompaun.tarikhKompaunStr ?: "-"} ${kompaun.masaKompaunStr ?: ""}".trim()
        )
        ReadOnlyField("Status", kompaun.status ?: "-")
        ReadOnlyField(
            "Kadar Kompaun",
            kompaun.kadarKompaun?.toString() ?: "-"
        )

        if (!kompaun.catatanBatal.isNullOrBlank()) {
            ReadOnlyField("Catatan Batal", kompaun.catatanBatal ?: "")
        }

        // --- Back Button at the very bottom ---
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text("Kembali")
        }
    }
}
