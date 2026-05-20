package com.example.promisclamping.presentation.kompaun

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalPrintshop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.promisclamping.Config
import com.example.promisclamping.Config.DEV_MAC_ADD
import com.example.promisclamping.models.KompaunItem
import com.example.promisclamping.print.printBphNotisCajWithSdkV2
import com.example.promisclamping.ui.theme.ActionBackGrey
import com.example.promisclamping.ui.theme.ActionPrintPurple
import com.example.promisclamping.ui.theme.CardBackground
import com.example.promisclamping.ui.theme.NavyHeader
import com.example.promisclamping.ui.theme.NavyHeaderDark
import com.example.promisclamping.ui.theme.PageBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KompaunHistoryDetailScreen(
    kompaun: KompaunItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isPrinting by remember { mutableStateOf(false) }

    val statusText = kompaun.status ?: "-"
    val statusColors = remember(statusText) { historyStatusColors(statusText) }

    Scaffold(
        containerColor = PageBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Maklumat Kompaun",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = kompaun.noKompaun ?: "-",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFDDE7FF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = NavyHeader,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HistoryHeroCard(
                noKenderaan = kompaun.noKenderaan ?: "-",
                noKompaun = kompaun.noKompaun ?: "-",
                status = statusText,
                statusBackground = statusColors.background,
                statusTextColor = statusColors.text
            )

            HistorySectionCard(
                title = "Ringkasan Kompaun",
                subtitle = "Maklumat utama rekod kompaun.",
                icon = Icons.Default.ReceiptLong
            ) {
                HistoryInfoRow(
                    icon = Icons.Default.ReceiptLong,
                    label = "No. Kompaun",
                    value = kompaun.noKompaun ?: "-"
                )
                HistoryInfoRow(
                    icon = Icons.Default.CalendarMonth,
                    label = "Tarikh & Masa",
                    value = "${kompaun.tarikhKompaunStr ?: "-"} ${kompaun.masaKompaunStr ?: ""}".trim()
                )
                HistoryInfoRow(
                    icon = Icons.Default.Verified,
                    label = "Status",
                    value = statusText,
                    valueColor = statusColors.text
                )
                HistoryInfoRow(
                    icon = Icons.Default.Payment,
                    label = "Kadar Kompaun",
                    value = "RM ${kompaun.kadarKompaun?.toString() ?: "-"}"
                )
            }

            HistorySectionCard(
                title = "Maklumat Kenderaan & Pemilik",
                subtitle = "Butiran kenderaan yang terlibat.",
                icon = Icons.Default.DirectionsCar
            ) {
                HistoryInfoRow(
                    icon = Icons.Default.DirectionsCar,
                    label = "No. Kenderaan",
                    value = kompaun.noKenderaan ?: "-"
                )
                HistoryInfoRow(
                    icon = Icons.Default.Badge,
                    label = "Jenis Kenderaan",
                    value = kompaun.jenisKenderaan ?: "-"
                )
                HistoryInfoRow(
                    icon = Icons.Default.Person,
                    label = "Pemilik",
                    value = kompaun.namaPemilik ?: "-"
                )
            }

            HistorySectionCard(
                title = "Lokasi",
                subtitle = "Maklumat lokasi kompaun dikeluarkan.",
                icon = Icons.Default.LocationOn
            ) {
                HistoryInfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Tempat",
                    value = kompaun.tempat ?: "-"
                )
                HistoryInfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Lokasi",
                    value = kompaun.lokasi ?: "-"
                )
            }

            if (!kompaun.catatanBatal.isNullOrBlank()) {
                HistorySectionCard(
                    title = "Catatan Batal",
                    subtitle = "Sebab atau catatan pembatalan.",
                    icon = Icons.Default.StickyNote2
                ) {
                    Text(
                        text = kompaun.catatanBatal ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF374151),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFFFF7ED),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                    )
                }
            }

            if (!kompaun.dirClamp1.isNullOrBlank() || !kompaun.dirClamp2.isNullOrBlank()) {
                HistorySectionCard(
                    title = "Gambar Rekod",
                    subtitle = "Gambar bukti apitan dan pembukaan apitan.",
                    icon = Icons.Default.Image
                ) {
                    if (!kompaun.dirClamp1.isNullOrBlank()) {
                        HistoryImageBlock(
                            title = "Gambar Apitan",
                            imageUrl = "${Config.UPLOAD_BASE_URL}${kompaun.dirClamp1}"
                        )
                    }

                    if (!kompaun.dirClamp2.isNullOrBlank()) {
                        HistoryImageBlock(
                            title = "Gambar Buka Apitan",
                            imageUrl = "${Config.UPLOAD_BASE_URL}${kompaun.dirClamp2}"
                        )
                    }
                }
            }

            HistorySectionCard(
                title = "Tindakan",
                subtitle = "Cetak semula notis atau kembali ke senarai sejarah.",
                icon = Icons.Default.LocalPrintshop
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isPrinting = true
                            try {
                                var fetchedBitmap: Bitmap? = null

                                if (!kompaun.dirClamp1.isNullOrBlank()) {
                                    val imageUrl = "${Config.UPLOAD_BASE_URL}${kompaun.dirClamp1}"

                                    val request = ImageRequest.Builder(context)
                                        .data(imageUrl)
                                        .allowHardware(false)
                                        .build()

                                    val result = context.imageLoader.execute(request)
                                    if (result is SuccessResult) {
                                        fetchedBitmap = (result.drawable as? BitmapDrawable)?.bitmap
                                    }
                                }

                                printBphNotisCajWithSdkV2(
                                    mac = DEV_MAC_ADD,
                                    context = context,
                                    gambarBitmap = fetchedBitmap,
                                    noSiri = kompaun.noKompaun ?: "-",
                                    tarikh = kompaun.tarikhKompaunStr ?: "-",
                                    masa = kompaun.masaKompaunStr ?: "-",
                                    noKenderaan = kompaun.noKenderaan ?: "-",
                                    kadarCaj = "RM ${kompaun.kadarKompaun?.toString() ?: "50"}",
                                    jenisKenderaan = kompaun.jenisKenderaan ?: "-",
                                    lokasi = kompaun.lokasi ?: "KOMPLEKS F",
                                    pegawai = kompaun.namaPegawai ?: "-",
                                    savedId = kompaun.id ?: "-"
                                )

                                snackbarHostState.showSnackbar("Cetak semula dihantar.")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    "Gagal mencetak: ${e.message ?: e.javaClass.simpleName}"
                                )
                            } finally {
                                isPrinting = false
                            }
                        }
                    },
                    enabled = !isPrinting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActionPrintPurple,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    if (isPrinting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Mencetak...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.LocalPrintshop,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Cetak Semula Kompaun")
                    }
                }

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActionBackGrey,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Kembali")
                }
            }

            Spacer(Modifier.height(110.dp))
            Spacer(
                modifier = Modifier
                    .height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            )
        }
    }
}

@Composable
private fun HistoryHeroCard(
    noKenderaan: String,
    noKompaun: String,
    status: String,
    statusBackground: Color,
    statusTextColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = NavyHeader),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(NavyHeader, NavyHeaderDark)
                    )
                )
                .padding(18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "REKOD SEJARAH",
                        color = Color(0xFFDDE7FF),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = noKenderaan,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = noKompaun,
                    color = Color(0xFFDDE7FF),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    color = statusBackground,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = status,
                        color = statusTextColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistorySectionCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFEAF2FF),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF1565C0),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Bold
                    )

                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE5E7EB))

            content()
        }
    }
}

@Composable
private fun HistoryInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color(0xFF111827)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF9FAFB),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF1565C0),
            modifier = Modifier
                .padding(top = 2.dp)
                .size(19.dp)
        )

        Spacer(Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF6B7280)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value.ifBlank { "-" },
                style = MaterialTheme.typography.bodyLarge,
                color = valueColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HistoryImageBlock(
    title: String,
    imageUrl: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF374151),
            fontWeight = FontWeight.Bold
        )

        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFE5E7EB))
        )
    }
}

private data class HistoryStatusColors(
    val background: Color,
    val text: Color
)

private fun historyStatusColors(status: String): HistoryStatusColors {
    return when (status.uppercase()) {
        "BARU" -> HistoryStatusColors(
            background = Color(0xFFFFF3E0),
            text = Color(0xFFEF6C00)
        )
        "BAYAR" -> HistoryStatusColors(
            background = Color(0xFFE8F5E9),
            text = Color(0xFF2E7D32)
        )
        "SELESAI" -> HistoryStatusColors(
            background = Color(0xFFE3F2FD),
            text = Color(0xFF1565C0)
        )
        "BATAL" -> HistoryStatusColors(
            background = Color(0xFFFFEBEE),
            text = Color(0xFFC62828)
        )
        else -> HistoryStatusColors(
            background = Color(0xFFECEFF1),
            text = Color(0xFF455A64)
        )
    }
}
