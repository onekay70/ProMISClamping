package com.example.promisclamping.presentation.kompaun

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.promisclamping.Config
import com.example.promisclamping.data.local.TokenStore
import com.example.promisclamping.models.ClampingRequestForm
import com.example.promisclamping.models.KompaunItem
import com.example.promisclamping.network.ApiClient
import com.example.promisclamping.ui.theme.PrimaryGreen
import com.example.promisclamping.ui.theme.SecondaryBlue
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

    var selectedStatus by remember { mutableStateOf(kompaun.status ?: "SELESAI") }
    var catatan by remember { mutableStateOf(kompaun.catatanBatal ?: "") }
    var isSaving by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    var extraImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        extraImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(135.dp))

        // --- Read-only form-style fields ---
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
        ReadOnlyField(
            "Kadar Kompaun",
            kompaun.kadarKompaun?.toString() ?: "-"
        )

        Divider()

        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedStatus,
                onValueChange = {},
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
                listOf("SELESAI", "BATAL").forEach { option ->
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
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryBlue,
                    contentColor = Color.White
                ),
                onClick = { imagePickerLauncher.launch("image/*") }

            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Pilih Gambar")
            }

            extraImageUri?.let { uri ->
                Text(
                    uri.lastPathSegment ?: "Gambar Dipilih",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // --- Simpan Button ---
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
                        // 1. Upload extra image if present
                        var dirExtra: String? = null
                        if (extraImageUri != null) {
                            val fp = extraImageUri!!.toFilePart(ctx.contentResolver)
                            if (fp == null) {
                                snackbarHostState.showSnackbar("Tidak dapat baca fail gambar.")
                                isSaving = false
                                return@launch
                            }

                            val uploadResp = ApiClient.upload(ctx).uploadImage(
                                file = fp.part,
                                bucketName = Config.BUCKET_NAME.asTextPart()
                            )

                            if (!uploadResp.isSuccessful || uploadResp.body() == null) {
                                snackbarHostState.showSnackbar("Gagal muat naik (${uploadResp.code()})")
                                isSaving = false
                                return@launch
                            }

                            val body = uploadResp.body()!!
                            dirExtra = "${body.bucketname}/${body.pathId}/${fp.fileName}"
                        }

                        // 2. Build request
                        val requestForm = ClampingRequestForm(
                            id = kompaun.id,
                            noKenderaan = kompaun.noKenderaan,
                            jenisKenderaan = null,
                            blok = null,
                            tempat = null,
                            lokasi = null,
                            status = selectedStatus,
                            catatanBatal = if (selectedStatus == "BATAL") catatan else null,
                            dirClamp1 = null,
                            dirClamp2 = dirExtra
                        )

                        val api = ApiClient.kompaun(ctx)
                        val resp = when (selectedStatus) {
                            "BATAL" -> api.batalKompaun(kompaun.id ?: "", requestForm, authId)
                            "SELESAI" -> api.selesaiKompaun(kompaun.id ?: "", requestForm, authId)
                            else -> api.selesaiKompaun(kompaun.id ?: "", requestForm, authId) // or some default
                        }

                        if (resp.isSuccessful) {
                            // ✅ turn off loading *before* snackbar so UI unlocks immediately
                            isSaving = false
                            snackbarHostState.showSnackbar("Berjaya dikemaskini ($selectedStatus).")
                            onBack()
                        } else {
                            isSaving = false
                            val errText = resp.errorBody()?.string().orEmpty()
                            snackbarHostState.showSnackbar("Gagal (${resp.code()}): ${errText.ifBlank { "Ralat pelayan" }}")
                        }

                    } catch (t: Throwable) {
                        isSaving = false
                        snackbarHostState.showSnackbar("Ralat: ${t.message ?: t.javaClass.simpleName}")
                    }
                }
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSaving) "Menyimpan..." else "Simpan")
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
    }
}

