package com.example.promisclamping.presentation.kompaun

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.promisclamping.models.KompaunItem
import com.example.promisclamping.ui.theme.SecondaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KompaunHistoryDetailScreen(
    kompaun: KompaunItem,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("MAKLUMAT KOMPAUN") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(40.dp))

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
                    containerColor = SecondaryBlue,
                    contentColor = Color.White
                ),
            ) {
                Text("Kembali")
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}
