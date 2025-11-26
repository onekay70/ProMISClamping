package com.example.promisclamping.presentation.kompaun

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.promisclamping.Config
import com.example.promisclamping.data.local.TokenStore
import com.example.promisclamping.models.ClampingRequestForm
import com.example.promisclamping.models.KompaunItem
import com.example.promisclamping.network.ApiClient
import kotlinx.coroutines.launch
import com.example.promisclamping.util.asTextPart
import com.example.promisclamping.util.toFilePart
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KompaunDetailScreen2(
    kompaun: KompaunItem,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val tokenStore = remember { TokenStore(ctx) }
    val scope = rememberCoroutineScope()

    var selectedStatus by remember { mutableStateOf(kompaun.status ?: "BATAL") }
    var catatan by remember { mutableStateOf(kompaun.catatanBatal ?: "") }
    var isSaving by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    var extraImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        extraImageUri = uri
    }

    // we don’t use an inner Scaffold, just draw everything inside a Column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 🔹 Back row at the top
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
                text = kompaun.noKompaun ?: "Butiran Kompaun",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Divider()

        // Basic info
        Text("No Kompaun: ${kompaun.noKompaun ?: "-"}", style = MaterialTheme.typography.titleMedium)
        Text("No Kenderaan: ${kompaun.noKenderaan ?: "-"}")
        Text("Pemilik: ${kompaun.namaPemilik ?: "-"}")
        Text("Tempat: ${kompaun.tempat ?: "-"}")
        Text("Lokasi: ${kompaun.lokasi ?: "-"}")
        Text("Status semasa: ${kompaun.status ?: "-"}")

        Divider()

        Text("Tukar Status", style = MaterialTheme.typography.titleMedium)

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedStatus,
                onValueChange = { },
                readOnly = true,
                label = { Text("Status") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("BATAL", "SELESAI").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedStatus = option
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = catatan,
            onValueChange = { catatan = it },
            label = { Text("Catatan") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Text("Gambar Tambahan (Clamp / Bukti)", style = MaterialTheme.typography.titleMedium)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Pilih Gambar")
            }
            extraImageUri?.let { uri ->
                Text(
                    text = uri.lastPathSegment ?: "Gambar dipilih",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // We keep using SnackbarHostState for messages, but show snackbars via LaunchedEffect
        // when needed (you already do that in other code paths).
        Button(
            onClick = {
                scope.launch {
                    val authId = tokenStore.userId ?: ""

                    if (authId.isBlank()) {
                        snackbarHostState.showSnackbar("ID pengguna tiada. Sila log masuk semula.")
                        return@launch
                    }

                    isSaving = true

                    try {
                        var dirExtra: String? = null
                        if (extraImageUri != null) {
                            val fp = extraImageUri!!.toFilePart(ctx.contentResolver) ?: run {
                                snackbarHostState.showSnackbar("Tidak dapat baca fail gambar.")
                                isSaving = false
                                return@launch
                            }

                            val bucket = Config.BUCKET_NAME
                            val uploadResp = ApiClient.upload(ctx).uploadImage(
                                file = fp.part,
                                bucketName = bucket.asTextPart()
                            )

                            if (!uploadResp.isSuccessful || uploadResp.body() == null) {
                                snackbarHostState.showSnackbar("Gagal muat naik (${uploadResp.code()})")
                                isSaving = false
                                return@launch
                            }

                            val body = uploadResp.body()!!
                            val fileName = fp.fileName
                            dirExtra = "${body.bucketname}/${body.pathId}/$fileName"
                        }

                        val requestForm = ClampingRequestForm(
                            id = kompaun.id,
                            noKenderaan = kompaun.noKenderaan,
                            jenisKenderaan = null,
                            blok = null,
                            tempat = null,
                            lokasi = null,
                            status = null,
                            catatanBatal = if (selectedStatus == "BATAL") catatan else null,
                            dirClamp1 = null,
                            dirClamp2 = dirExtra
                        )

                        val api = ApiClient.kompaun(ctx)
                        val resp = if (selectedStatus == "BATAL") {
                            api.batalKompaun(kompaun.id ?: "", requestForm, authId)
                        } else {
                            api.selesaiKompaun(kompaun.id ?: "", requestForm, authId)
                        }

                        if (resp.isSuccessful) {
                            snackbarHostState.showSnackbar("Berjaya dikemaskini ($selectedStatus).")
                            onBack()
                        } else {
                            val errText = resp.errorBody()?.string().orEmpty()
                            snackbarHostState.showSnackbar("Gagal (${resp.code()}): ${errText.ifBlank { "Ralat pelayan" }}")
                        }

                    } catch (t: Throwable) {
                        snackbarHostState.showSnackbar("Ralat: ${t.message ?: t.javaClass.simpleName}")
                    } finally {
                        isSaving = false
                    }
                }
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSaving) "Menyimpan..." else "Simpan")
        }
    }
}
