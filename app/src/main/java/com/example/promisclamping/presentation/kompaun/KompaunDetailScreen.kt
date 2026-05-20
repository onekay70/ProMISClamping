package com.example.promisclamping.presentation.kompaun

import android.bluetooth.BluetoothManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.promisclamping.Config
import com.example.promisclamping.Config.DEV_MAC_ADD
import com.example.promisclamping.data.local.TokenStore
import com.example.promisclamping.models.ClampingRequestForm
import com.example.promisclamping.models.KompaunItem
import com.example.promisclamping.network.ApiClient
import com.example.promisclamping.print.printBphNotisCajWithSdkV2
import com.example.promisclamping.ui.theme.ActionBackGrey
import com.example.promisclamping.ui.theme.ActionCancelRed
import com.example.promisclamping.ui.theme.ActionDisabledBg
import com.example.promisclamping.ui.theme.ActionDisabledText
import com.example.promisclamping.ui.theme.ActionDoneBlue
import com.example.promisclamping.ui.theme.ActionPayGreen
import com.example.promisclamping.ui.theme.ActionPrintPurple
import com.example.promisclamping.ui.theme.NavyHeader
import com.example.promisclamping.ui.theme.PageBackground
import com.example.promisclamping.ui.theme.SecondaryBlue
import com.example.promisclamping.ui.theme.TextMuted
import com.example.promisclamping.util.asTextPart
import com.example.promisclamping.util.toFilePart
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KompaunDetailScreen(
    kompaun: KompaunItem,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val tokenStore = remember { TokenStore(ctx) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentStatus by remember { mutableStateOf(kompaun.status ?: "BARU") }
    var currentNoResit by remember { mutableStateOf("") }

    var catatan by remember { mutableStateOf(kompaun.catatanBatal ?: "") }
    var isSaving by remember { mutableStateOf(false) }
    var isBatal by remember { mutableStateOf(false) }
    var isFormLocked by remember { mutableStateOf(false) }

    var showManualPaymentDialog by remember { mutableStateOf(false) }
    var noResitManual by remember { mutableStateOf("") }
    var isSavingPayment by remember { mutableStateOf(false) }
    var paymentError by remember { mutableStateOf<String?>(null) }

    var extraImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        extraImageUri = uri
    }

    val bolehBayarManual = currentStatus.equals("BARU", ignoreCase = true)
    val bolehSelesai = currentStatus.equals("BARU", ignoreCase = true) ||
            currentStatus.equals("BAYAR", ignoreCase = true)

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DetailHeaderCard(
                noKenderaan = kompaun.noKenderaan ?: "-",
                noKompaun = kompaun.noKompaun ?: "-",
                status = currentStatus
            )

            DetailSectionCard(
                title = "Maklumat Kompaun",
                subtitle = "Butiran notis dan lokasi apitan."
            ) {
                DetailInfoRow("No. Kompaun", kompaun.noKompaun ?: "-")
                DetailInfoRow(
                    "Tarikh & Masa",
                    "${kompaun.tarikhKompaunStr ?: "-"} ${kompaun.masaKompaunStr ?: ""}".trim()
                )
                DetailInfoRow("Tempat", kompaun.tempat ?: "-")
                DetailInfoRow("Lokasi", kompaun.lokasi ?: "-")
                DetailInfoRow("Pegawai", kompaun.namaPegawai ?: "-")
            }

            DetailSectionCard(
                title = "Maklumat Kenderaan",
                subtitle = "Maklumat kenderaan dan pemilik."
            ) {
                DetailInfoRow("No. Kenderaan", kompaun.noKenderaan ?: "-")
                DetailInfoRow("Jenis Kenderaan", kompaun.jenisKenderaan ?: "-")
                DetailInfoRow("Nama Pemilik", kompaun.namaPemilik ?: "-")
                DetailInfoRow("No. Pengenalan", kompaun.idPemilik ?: "-")
            }

            DetailSectionCard(
                title = "Bayaran",
                subtitle = "Kemaskini bayaran manual jika pemilik telah membuat bayaran."
            ) {
                DetailInfoRow("Kadar Kompaun", "RM ${kompaun.kadarKompaun?.toString() ?: "-"}")
                DetailInfoRow("Status", currentStatus.ifBlank { "-" })

                if (currentNoResit.isNotBlank()) {
                    DetailInfoRow("No. Resit", currentNoResit)
                }

                Button(
                    onClick = {
                        paymentError = null
                        noResitManual = ""
                        showManualPaymentDialog = true
                    },
                    enabled = bolehBayarManual && !isSavingPayment && !isFormLocked,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActionPayGreen,
                        contentColor = Color.White,
                        disabledContainerColor = ActionDisabledBg,
                        disabledContentColor = ActionDisabledText
                    )
                ) {
                    Text(
                        if (bolehBayarManual) {
                            "Kemaskini Bayaran Manual"
                        } else {
                            "Bayaran Telah Dikemaskini"
                        }
                    )
                }
            }

            DetailSectionCard(
                title = "Gambar Apitan",
                subtitle = "Gambar asal semasa apitan dibuat."
            ) {
                if (!kompaun.dirClamp1.isNullOrBlank()) {
                    val imageUrl = "${Config.UPLOAD_BASE_URL}${kompaun.dirClamp1}"
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Gambar Clamp",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Text(
                        text = "Tiada gambar apitan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }

            DetailSectionCard(
                title = "Tindakan Apitan",
                subtitle = "Isi catatan dan gambar bukti sebelum selesai atau batal."
            ) {
                OutlinedTextField(
                    enabled = !isFormLocked,
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SecondaryBlue,
                            contentColor = Color.White
                        ),
                        onClick = { imagePickerLauncher.launch("image/*") },
                        enabled = !isFormLocked,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Pilih Gambar")
                    }

                    extraImageUri?.let { uri ->
                        Text(
                            uri.lastPathSegment ?: "Gambar Dipilih",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            val authId = tokenStore.userId ?: ""

                            if (authId.isBlank()) {
                                snackbarHostState.showSnackbar("ID pengguna tiada. Sila log masuk semula.")
                                return@launch
                            }

                            if (!bolehSelesai) {
                                snackbarHostState.showSnackbar("Status semasa tidak membenarkan tindakan selesai.")
                                return@launch
                            }

                            isSaving = true
                            isFormLocked = true

                            try {
                                var dirExtra: String? = null
                                if (extraImageUri != null) {
                                    val fp = extraImageUri!!.toFilePart(ctx.contentResolver)
                                    if (fp == null) {
                                        snackbarHostState.showSnackbar("Tidak dapat baca fail gambar.")
                                        isSaving = false
                                        isFormLocked = false
                                        return@launch
                                    }

                                    val uploadResp = ApiClient.upload(ctx).uploadImage(
                                        file = fp.part,
                                        bucketName = Config.BUCKET_NAME.asTextPart()
                                    )

                                    if (!uploadResp.isSuccessful || uploadResp.body() == null) {
                                        snackbarHostState.showSnackbar("Gagal muat naik (${uploadResp.code()})")
                                        isSaving = false
                                        isFormLocked = false
                                        return@launch
                                    }

                                    val body = uploadResp.body()!!
                                    dirExtra = "${body.bucketname}/${body.pathId}/${fp.fileName}"
                                }

                                val requestForm = ClampingRequestForm(
                                    id = kompaun.id,
                                    noKenderaan = kompaun.noKenderaan,
                                    jenisKenderaan = null,
                                    blok = null,
                                    tempat = null,
                                    lokasi = null,
                                    status = "SELESAI",
                                    catatanBatal = catatan,
                                    dirClamp1 = null,
                                    dirClamp2 = dirExtra
                                )

                                val resp = ApiClient.kompaun(ctx)
                                    .selesaiKompaun(kompaun.id ?: "", requestForm, authId)

                                if (resp.isSuccessful) {
                                    currentStatus = "SELESAI"
                                    isSaving = false
                                    isFormLocked = true
                                    snackbarHostState.showSnackbar("Kompaun berjaya diselesaikan.")
                                } else {
                                    isSaving = false
                                    isFormLocked = false
                                    val errText = resp.errorBody()?.string().orEmpty()
                                    snackbarHostState.showSnackbar("Gagal (${resp.code()}): ${errText.ifBlank { "Ralat pelayan" }}")
                                }
                            } catch (t: Throwable) {
                                isSaving = false
                                isFormLocked = false
                                snackbarHostState.showSnackbar("Ralat: ${t.message ?: t.javaClass.simpleName}")
                            }
                        }
                    },
                    enabled = bolehSelesai && !isSaving && !isFormLocked,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActionDoneBlue,
                        contentColor = Color.White,
                        disabledContainerColor = ActionDisabledBg,
                        disabledContentColor = ActionDisabledText
                    )
                ) {
                    Text(if (isSaving) "Menyimpan..." else "Selesai / Buka Apitan")
                }

                Button(
                    onClick = {
                        scope.launch {
                            val authId = tokenStore.userId ?: ""

                            if (authId.isBlank()) {
                                snackbarHostState.showSnackbar("ID pengguna tiada. Sila log masuk semula.")
                                return@launch
                            }

                            if (catatan.isBlank()) {
                                snackbarHostState.showSnackbar("Sila masukkan catatan batal.")
                                return@launch
                            }

                            isBatal = true
                            isFormLocked = true

                            try {
                                var dirExtra: String? = null
                                if (extraImageUri != null) {
                                    val fp = extraImageUri!!.toFilePart(ctx.contentResolver)
                                    if (fp == null) {
                                        snackbarHostState.showSnackbar("Tidak dapat baca fail gambar.")
                                        isBatal = false
                                        isFormLocked = false
                                        return@launch
                                    }

                                    val uploadResp = ApiClient.upload(ctx).uploadImage(
                                        file = fp.part,
                                        bucketName = Config.BUCKET_NAME.asTextPart()
                                    )

                                    if (!uploadResp.isSuccessful || uploadResp.body() == null) {
                                        snackbarHostState.showSnackbar("Gagal muat naik (${uploadResp.code()})")
                                        isBatal = false
                                        isFormLocked = false
                                        return@launch
                                    }

                                    val body = uploadResp.body()!!
                                    dirExtra = "${body.bucketname}/${body.pathId}/${fp.fileName}"
                                }

                                val requestForm = ClampingRequestForm(
                                    id = kompaun.id,
                                    noKenderaan = kompaun.noKenderaan,
                                    jenisKenderaan = null,
                                    blok = null,
                                    tempat = null,
                                    lokasi = null,
                                    status = "BATAL",
                                    catatanBatal = catatan,
                                    dirClamp1 = null,
                                    dirClamp2 = dirExtra
                                )

                                val resp = ApiClient.kompaun(ctx)
                                    .batalKompaun(kompaun.id ?: "", requestForm, authId)

                                if (resp.isSuccessful) {
                                    currentStatus = "BATAL"
                                    isBatal = false
                                    isFormLocked = true
                                    snackbarHostState.showSnackbar("Kompaun berjaya dibatalkan.")
                                } else {
                                    isBatal = false
                                    isFormLocked = false
                                    val errText = resp.errorBody()?.string().orEmpty()
                                    snackbarHostState.showSnackbar("Gagal (${resp.code()}): ${errText.ifBlank { "Ralat pelayan" }}")
                                }
                            } catch (t: Throwable) {
                                isBatal = false
                                isFormLocked = false
                                snackbarHostState.showSnackbar("Ralat: ${t.message ?: t.javaClass.simpleName}")
                            }
                        }
                    },
                    enabled = !isBatal && !isFormLocked,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActionCancelRed,
                        contentColor = Color.White,
                        disabledContainerColor = ActionDisabledBg,
                        disabledContentColor = ActionDisabledText
                    )
                ) {
                    Text(if (isBatal) "Menyimpan..." else "Batal Kompaun")
                }
            }

            val context = LocalContext.current
            var isPrinting by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                    val bluetoothAdapter = bluetoothManager.adapter

                    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                        Toast.makeText(context, "Sila hidupkan Bluetooth terlebih dahulu!", Toast.LENGTH_LONG).show()
                        return@Button
                    }

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
                                kadarCaj = kompaun.kadarKompaun?.toString() ?: "50",
                                jenisKenderaan = kompaun.jenisKenderaan ?: "-",
                                lokasi = kompaun.lokasi ?: "KOMPLEKS F",
                                pegawai = kompaun.namaPegawai ?: "-",
                                savedId = kompaun.id ?: "-"
                            )

                            snackbarHostState.showSnackbar("Cetak dihantar.")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Ralat Cetakan: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isPrinting = false
                        }
                    }
                },
                enabled = !isPrinting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActionPrintPurple,
                    contentColor = Color.White
                )
            ) {
                Text(if (isPrinting) "Mencetak..." else "Cetak Semula Kompaun")
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActionBackGrey,
                    contentColor = Color.White
                )
            ) {
                Text("Kembali")
            }

            Spacer(Modifier.height(120.dp))
        }

        if (showManualPaymentDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!isSavingPayment) showManualPaymentDialog = false
                },
                title = { Text("Bayaran Manual") },
                text = {
                    Column {
                        Text("Masukkan no. resit yang telah dibayar oleh pemilik kenderaan.")

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = noResitManual,
                            onValueChange = {
                                noResitManual = it.uppercase()
                                paymentError = null
                            },
                            singleLine = true,
                            label = { Text("No. Resit") },
                            placeholder = { Text("Contoh: RCP123456") },
                            enabled = !isSavingPayment,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!paymentError.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = paymentError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (isSavingPayment) {
                            Spacer(Modifier.height(12.dp))
                            CircularProgressIndicator()
                        }
                    }
                },
                confirmButton = {
                    Button(
                        enabled = !isSavingPayment && noResitManual.isNotBlank(),
                        onClick = {
                            scope.launch {
                                val authId = tokenStore.userId ?: ""

                                if (authId.isBlank()) {
                                    paymentError = "ID pengguna tiada. Sila log masuk semula."
                                    return@launch
                                }

                                if (kompaun.id.isNullOrBlank()) {
                                    paymentError = "ID kompaun tidak dijumpai."
                                    return@launch
                                }

                                isSavingPayment = true
                                paymentError = null

                                try {
                                    val requestForm = ClampingRequestForm(
                                        id = kompaun.id,
                                        noKenderaan = kompaun.noKenderaan,
                                        jenisKenderaan = null,
                                        blok = null,
                                        tempat = null,
                                        lokasi = null,
                                        status = null,
                                        catatanBatal = null,
                                        dirClamp1 = null,
                                        dirClamp2 = null
                                    )

                                    val resp = ApiClient.kompaun(ctx).updateBayaranKompaunManual(
                                        id = kompaun.id ?: "",
                                        body = requestForm,
                                        noResit = noResitManual.trim(),
                                        authId = authId
                                    )

                                    if (resp.isSuccessful) {
                                        currentStatus = "BAYAR"
                                        currentNoResit = noResitManual.trim()
                                        showManualPaymentDialog = false
                                        noResitManual = ""
                                        snackbarHostState.showSnackbar(
                                            "Bayaran berjaya dikemaskini. Sila tekan Selesai selepas apitan dibuka."
                                        )
                                    } else {
                                        val errText = resp.errorBody()?.string().orEmpty()
                                        paymentError = "Gagal (${resp.code()}): ${errText.ifBlank { "Ralat pelayan" }}"
                                    }
                                } catch (t: Throwable) {
                                    paymentError = t.message ?: t.javaClass.simpleName
                                } finally {
                                    isSavingPayment = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActionPayGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (isSavingPayment) "Menyimpan..." else "Simpan")
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !isSavingPayment,
                        onClick = { showManualPaymentDialog = false }
                    ) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailHeaderCard(
    noKenderaan: String,
    noKompaun: String,
    status: String
) {
    val statusColor = when (status.uppercase()) {
        "BARU" -> Color(0xFFFFA000)
        "BAYAR" -> Color(0xFF2E7D32)
        "SELESAI" -> Color(0xFF1565C0)
        "BATAL" -> Color(0xFFC62828)
        else -> Color(0xFF607D8B)
    }

    val statusBg = when (status.uppercase()) {
        "BARU" -> Color(0xFFFFF3E0)
        "BAYAR" -> Color(0xFFE8F5E9)
        "SELESAI" -> Color(0xFFE3F2FD)
        "BATAL" -> Color(0xFFFFEBEE)
        else -> Color(0xFFECEFF1)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = NavyHeader),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = noKenderaan,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = noKompaun,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFDDE7FF)
            )

            Surface(
                color = statusBg,
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = status,
                    color = statusColor,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = NavyHeader
            )

            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            HorizontalDivider(color = Color(0xFFE5E7EB))

            content()
        }
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted
        )
        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF111827)
        )
    }
}
