package com.example.promisclamping.presentation.kompaun

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import androidx.compose.ui.platform.LocalContext
import com.example.promisclamping.models.KompaunItem
import com.example.promisclamping.ui.theme.SecondaryBlue
import com.example.promisclamping.Config.DEV_MAC_ADD
import com.example.promisclamping.print.printBphNotisCajWithSdkV2
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.promisclamping.Config
import coil.imageLoader
import android.bluetooth.BluetoothManager
import android.widget.Toast

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

            Spacer(Modifier.height(16.dp))
            // Paparkan Gambar Clamp 1
            if (!kompaun.dirClamp1.isNullOrBlank()) {
                val imageUrl = "${Config.UPLOAD_BASE_URL}${kompaun.dirClamp1}"
                Text(
                    text = "Gambar Clamping: ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Gambar Clamp 1",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.height(16.dp))
            }

            // Paparkan Gambar Clamp 2
            if (!kompaun.dirClamp2.isNullOrBlank()) {
                val imageUrl = "${Config.UPLOAD_BASE_URL}${kompaun.dirClamp2}"
                Text(
                    text = "Gambar Buka Clamping: ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Gambar Clamp 2",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.height(16.dp))
            }
            Spacer(Modifier.height(30.dp))

            if (!kompaun.catatanBatal.isNullOrBlank()) {
                ReadOnlyField("Catatan Batal", kompaun.catatanBatal ?: "")
            }

            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            var isPrinting by remember { mutableStateOf(false) }

            // --- Butang Cetak Semula ---
            Button(
                onClick = {
                    scope.launch {
                        isPrinting = true
                        try {
                            var fetchedBitmap: Bitmap? = null

                            // Cek kalau ada URL gambar, kita fetch jadi Bitmap dulu
                            if (!kompaun.dirClamp1.isNullOrBlank()) {
                                val imageUrl = "${Config.UPLOAD_BASE_URL}${kompaun.dirClamp1}"

                                val request = ImageRequest.Builder(context)
                                    .data(imageUrl)
                                    // allowHardware(false) ni SANGAT PENTING untuk printer!
                                    // Printer perlukan perisian pixel (software bitmap), bukan hardware bitmap.
                                    .allowHardware(false)
                                    .build()

                                val result = context.imageLoader.execute(request)
                                if (result is SuccessResult) {
                                    fetchedBitmap = (result.drawable as? BitmapDrawable)?.bitmap
                                }
                            }

                            // Hantar ke fungsi print
                            printBphNotisCajWithSdkV2(
                                mac = DEV_MAC_ADD,
                                context = context,
                                gambarBitmap = fetchedBitmap, // <-- Pass bitmap yang dah di-fetch tadi (atau null jika gagal/tiada)
                                noSiri = kompaun.noKompaun ?: "-",
                                tarikh = kompaun.tarikhKompaunStr ?: "-",
                                masa = kompaun.masaKompaunStr ?: "-",
                                noKenderaan = kompaun.noKenderaan ?: "-",
                                kadarCaj = kompaun.kadarKompaun?.toString() ?: "50",
                                jenisKenderaan = kompaun.jenisKenderaan ?: "-",
                                lokasi = kompaun.lokasi ?: "KOMPLEKS F",
                                pegawai = kompaun.namaPegawai ?: "-",
                                savedId = kompaun.id ?: "-"
                            )
                        } catch (e: Exception) {
                            // Boleh panggil snackbar jika ralat
                            // snackbarHostState.showSnackbar("Gagal mencetak: ${e.message}")
                        } finally {
                            isPrinting = false
                        }
                    }
                },
                enabled = !isPrinting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(if (isPrinting) "Mencetak..." else "Cetak Semula Kompaun")
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
