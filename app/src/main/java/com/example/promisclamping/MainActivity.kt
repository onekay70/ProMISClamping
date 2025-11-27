package com.example.promisclamping

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.promisclamping.Config.DEV_MAC_ADD
import com.example.promisclamping.domain.auth.AuthState
import com.example.promisclamping.models.ClampingRequestForm
import com.example.promisclamping.models.ClampingResponseForm
import com.example.promisclamping.models.VehicleType
import com.example.promisclamping.network.ApiClient
import com.example.promisclamping.presentation.auth.AuthViewModel
import com.example.promisclamping.presentation.auth.LoginScreen
import com.example.promisclamping.presentation.main.MainTabs
import com.example.promisclamping.print.printBphNotisCajWithSdkV2
import kotlinx.coroutines.launch
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import com.example.promisclamping.data.local.TokenStore
import com.example.promisclamping.presentation.auth.AuthViewModelFactory
import com.example.promisclamping.ui.theme.PrimaryGreen
import com.example.promisclamping.ui.theme.ProMISClampingTheme
import com.example.promisclamping.ui.theme.SecondaryBlue
import com.example.promisclamping.util.asTextPart
import com.example.promisclamping.util.toFilePart
import com.example.promisclamping.util.FilePart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProMISClampingTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authState by authViewModel.authState.collectAsState()

                    when (authState) {
                        AuthState.Unknown -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        AuthState.Unauthenticated -> {
                            LoginScreen(
                                onLogin = { username, password ->
                                    authViewModel.login(username, password)
                                },
                                isLoading = authViewModel.isLoading,
                                errorMessage = authViewModel.loginError
                            )
                        }


                        is AuthState.Authenticated -> {
                            MainTabs(
                                onLogout = { authViewModel.logout() }
                            )
                        }
                    }
                }
            }
        }
    }
}

private val btPermissions = arrayOf(
    Manifest.permission.BLUETOOTH_CONNECT,
    Manifest.permission.BLUETOOTH_SCAN
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarKompaunScreen() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) {
            Toast.makeText(context, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Bluetooth permissions denied", Toast.LENGTH_SHORT).show()
        }
    }


    var lastSaved by remember { mutableStateOf<ClampingResponseForm?>(null) }

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val needsBtConnectPerm = android.os.Build.VERSION.SDK_INT >= 31
    val btPerms = if (needsBtConnectPerm)
        arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
    else emptyArray()

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* optional: inspect results */ }

    fun hasBtConnectPermission(): Boolean =
        !needsBtConnectPerm || ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED

    // --- Form state ---
    var noKenderaan by remember { mutableStateOf("") }
    var blok by remember { mutableStateOf("BLOK F6") }
    var tempatKompaun by remember { mutableStateOf("PARKING") }

    // Control flags
    var isSaving by remember { mutableStateOf(false) }
    var savedId by remember { mutableStateOf<String?>(null) }

    val blokList = listOf(
        "BLOK F1",
        "BLOK F2",
        "BLOK F3",
        "BLOK F4",
        "BLOK F5",
        "BLOK F6",
        "BLOK F7",
        "BLOK F8",
        "BLOK F9",
        "BLOK F10"
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("DAFTAR KOMPAUN") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(40.dp))

            Text(
                text = "Maklumat Kompaun",
                style = MaterialTheme.typography.titleMedium
            )

            // --- No Kenderaan ---
            OutlinedTextField(
                value = noKenderaan,
                onValueChange = { noKenderaan = it.uppercase() },
                label = { Text("No Kenderaan *") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Characters
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            )

            // --- Jenis Kenderaan ---
            var selectedJenis by remember { mutableStateOf(VehicleType("03", "KERETA")) }

            JenisKenderaanDropdown(
                selected = selectedJenis,
                onSelect = { selectedJenis = it },
                enabled = !isSaving
            )

            // --- Blok ---
            var blokExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = blokExpanded,
                onExpandedChange = { if (!isSaving) blokExpanded = !blokExpanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = blok,
                    onValueChange = {},
                    label = { Text("Blok") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = blokExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    enabled = !isSaving
                )
                ExposedDropdownMenu(
                    expanded = blokExpanded,
                    onDismissRequest = { blokExpanded = false }) {
                    blokList.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = { blok = it; blokExpanded = false })
                    }
                }
            }

            // --- Tempat Kompaun ---
            OutlinedTextField(
                value = tempatKompaun,
                onValueChange = { tempatKompaun = it },
                label = { Text("Tempat Kompaun") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            )

            val context = LocalContext.current
            var gambarBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var gambarUri by remember { mutableStateOf<Uri?>(null) }
            var gambarSizeBytes by remember { mutableStateOf<Long?>(null) }

            val pickImage = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                gambarUri = uri
                gambarSizeBytes = null

                if (uri != null) {
                    // Get size
                    runCatching {
                        val size = context.contentResolver.openFileDescriptor(uri, "r")?.statSize
                        gambarSizeBytes = size
                    }

                    // Decode bitmap for printing
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    gambarBitmap = bitmap
                }
            }

            Divider()

            // --- Gambar Upload ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Gambar Apitan")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { if (!isSaving) pickImage.launch("image/*") },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SecondaryBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Muat Naik")
                    }
                    if (gambarUri != null) {
                        AssistChip(
                            onClick = { if (!isSaving) pickImage.launch("image/*") },
                            label = { Text("Tukar Gambar") },
                            enabled = !isSaving
                        )
                    }
                }

                val sizeOk = gambarSizeBytes?.let { it <= 5L * 1024 * 1024 } ?: true
                if (gambarUri != null) {
                    Text(
                        text = "Dipilih: ${gambarUri?.lastPathSegment ?: ""} " +
                                (gambarSizeBytes?.let { "(${it / 1024} KB)" } ?: ""),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (!sizeOk) {
                    Text("⚠️ Saiz melebihi 5 MB", color = MaterialTheme.colorScheme.error)
                }
            }

            // --- Buttons ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                /* 🟩 SIMPAN */
                Button(
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            // basic validation
                            when {
                                noKenderaan.isBlank() -> {
                                    snackbarHostState.showSnackbar("Isi No Kenderaan.")
                                    return@launch
                                }

                                gambarSizeBytes != null && gambarSizeBytes!! > 5L * 1024 * 1024 -> {
                                    snackbarHostState.showSnackbar("Gambar melebihi 5 MB.")
                                    return@launch
                                }
                            }

                            isSaving = true
                            var gambarPath: String? = null

                            try {
                                if (gambarUri != null) {
                                    val fp = gambarUri!!.toFilePart(ctx.contentResolver) ?: run {
                                        snackbarHostState.showSnackbar("Tidak dapat baca fail gambar.")
                                        isSaving = false
                                        return@launch
                                    }

                                    val bucket = Config.BUCKET_NAME
                                    val resp = ApiClient.upload(ctx).uploadImage(
                                        file = fp.part,
                                        bucketName = bucket.asTextPart()
                                    )

                                    if (!resp.isSuccessful || resp.body() == null) {
                                        snackbarHostState.showSnackbar("Gagal muat naik (${resp.code()})")
                                        isSaving = false
                                        return@launch
                                    }

                                    val body = resp.body()!!
                                    val fileName = fp.fileName
                                    gambarPath = "${body.bucketname}/${body.pathId}/$fileName"
                                }

                                val jenisId = selectedJenis?.id

                                val request = ClampingRequestForm(
                                    id = null,
                                    noKenderaan = noKenderaan,
                                    jenisKenderaan = jenisId.toString(),
                                    blok = blok,
                                    tempat = tempatKompaun,
                                    lokasi = null,
                                    status = null,
                                    catatanBatal = null,
                                    dirClamp1 = gambarPath,
                                    dirClamp2 = null
                                )

                                val authId = TokenStore(context).userId
                                if (authId.isNullOrBlank()) {
                                    snackbarHostState.showSnackbar("Sila log masuk semula (ID pengguna tiada).")
                                    isSaving = false
                                    return@launch
                                }

                                val resp = ApiClient.kompaun(ctx).createClamping(request, authId)

                                if (resp.isSuccessful) {
                                    val body = resp.body()
                                    if (body != null) {
                                        savedId = body.id
                                        lastSaved = body
                                        snackbarHostState.showSnackbar("Disimpan! No Kompaun: ${body.noKompaun ?: "-"}")
                                    } else {
                                        snackbarHostState.showSnackbar("Gagal: respons kosong.")
                                    }
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
                    }
                ) {
                    Text(if (isSaving) "Menyimpan..." else "Simpan")
                }

                /* 🟦 CETAK (only visible after save) */
                if (savedId != null) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val missing = btPermissions.any {
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            it
                                        ) != PackageManager.PERMISSION_GRANTED
                                    }

                                    if (missing) {
                                        launcher.launch(btPermissions)
                                    } else {
                                        if (gambarBitmap != null) {
                                            printBphNotisCajWithSdkV2(
                                                DEV_MAC_ADD, context, gambarBitmap,
                                                noSiri = lastSaved?.noKompaun ?: "-",
                                                tarikh = lastSaved?.tarikhKompaunStr ?: "-",
                                                masa = lastSaved?.masaKompaunStr ?: "-",
                                                noKenderaan = noKenderaan,
                                                kadarCaj = "RM ${lastSaved?.kadarKompaun}",
                                                jenisKenderaan = selectedJenis?.label ?: "",
                                                lokasi = lastSaved?.lokasi ?: "-",
                                                pegawai = lastSaved?.namaPegawai ?: "-",
                                                savedId = savedId!!
                                            )
                                        } else {
                                            printBphNotisCajWithSdkV2(
                                                DEV_MAC_ADD, context,
                                                noSiri = lastSaved?.noKompaun ?: "-",
                                                tarikh = lastSaved?.tarikhKompaunStr ?: "-",
                                                masa = lastSaved?.masaKompaunStr ?: "-",
                                                noKenderaan = noKenderaan,
                                                kadarCaj = "RM ${lastSaved?.kadarKompaun}",
                                                jenisKenderaan = selectedJenis?.label ?: "",
                                                lokasi = lastSaved?.lokasi ?: "-",
                                                pegawai = lastSaved?.namaPegawai ?: "-"
                                            )
                                        }
                                    }

                                    snackbarHostState.showSnackbar("Cetak dihantar ✅")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Gagal cetak: ${e.message}")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cetak Kompaun")
                    }
                }

                /* 🟨 DAFTAR BARU */
                OutlinedButton(
                    onClick = {
                        noKenderaan = ""
                        selectedJenis = VehicleType("03", "KERETA")
                        blok = blokList.first()
                        tempatKompaun = ""
                        gambarUri = null
                        gambarSizeBytes = null
                        savedId = null
                        lastSaved = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Daftar Baru")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JenisKenderaanDropdown(
    selected: VehicleType?,
    onSelect: (VehicleType) -> Unit,
    enabled: Boolean = true
) {
    // Your list of vehicle types with IDs and names
    val jenisList = listOf(
        VehicleType("01", "BAS"),
        VehicleType("02", "PACUAN EMPAT RODA"),
        VehicleType("03", "KERETA"),
        VehicleType("04", "VAN"),
        VehicleType("05", "LORI"),
        VehicleType("06", "MOTOSIKAL"),
        VehicleType("07", "LAIN-LAIN")
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selected?.label ?: "",
            onValueChange = {},
            label = { Text("Jenis Kenderaan *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = enabled
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            jenisList.forEach { jenis ->
                DropdownMenuItem(
                    text = { Text(jenis.label) },
                    onClick = {
                        onSelect(jenis)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ====== constants ======
private val SPP_UUID: UUID =
    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

// ====== Bluetooth socket helper with fallbacks ======
private fun createTscSocket(device: BluetoothDevice): BluetoothSocket {
    // 1) normal SPP
    runCatching { return device.createRfcommSocketToServiceRecord(SPP_UUID) }
    // 2) insecure SPP (some printers prefer this)
    runCatching {
        val m = device.javaClass.getMethod(
            "createInsecureRfcommSocketToServiceRecord", UUID::class.java
        )
        return m.invoke(device, SPP_UUID) as BluetoothSocket
    }
    // 3) legacy channel 1
    val meth = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
    return meth.invoke(device, 1) as BluetoothSocket
}

// ====== Printer call (send raw TSPL) ======
suspend fun printToTscOverBluetooth(
    macAddress: String,
    tspl: String
) = withContext(Dispatchers.IO) {
    val adapter = BluetoothAdapter.getDefaultAdapter()
        ?: throw IllegalStateException("Bluetooth not available")

    val device = adapter.getRemoteDevice(macAddress) // needs BLUETOOTH_CONNECT on API 31+
    val socket = createTscSocket(device)

    socket.use { s ->
        s.connect()
        s.outputStream.use { out ->
            out.write(tspl.toByteArray(Charsets.US_ASCII)) // ASCII for reliability
            out.flush()
            Thread.sleep(120) // small settle time
        }
    }
}

suspend fun uploadJataToPrinter(mac: String, bitmapBytes: ByteArray) {
    val adapter = BluetoothAdapter.getDefaultAdapter()
    val device = adapter.getRemoteDevice(mac)
    val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)

    socket.use { s ->
        s.connect()
        val out = s.outputStream

        // enter file write mode
        out.write("DOWNLOAD F,JATA.BMP,${bitmapBytes.size}\n".toByteArray())
        out.write(bitmapBytes)
        out.flush()
        Thread.sleep(200)

        out.write("PRINT 1,1\n".toByteArray())
        out.flush()
    }
}