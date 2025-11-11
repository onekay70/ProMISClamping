package com.example.promisclamping.print

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import com.example.tscdll.TSCActivity
import kotlin.String
import android.graphics.Bitmap
import android.graphics.Color
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

@SuppressLint("MissingPermission")
fun printWithSdk(macAddress: String) {
    val tsc = TSCActivity()

    try {
        // 1️⃣ Open Bluetooth Port
        tsc.openport(macAddress)

        tsc.downloadbmp("jata_malaysia_384px.bmp")

        tsc.sendcommand("PUTBMP 100,20,\"jata_malaysia_384px.bmp\"\n")

        // 2️⃣ Configure printer
        tsc.setup(72, 120, 4, 8, 0, 0, 0) // width mm, height mm, speed, density, gap, etc
        tsc.clearbuffer()

        // 3️⃣ Example: Text printing
        tsc.sendcommand("TEXT 120,50,\"3\",0,1,1,\"JABATAN PERDANA MENTERI\"\n")
        tsc.sendcommand("TEXT 100,100,\"3\",0,1,1,\"NOTIS CAJ\"\n")
        tsc.sendcommand("TEXT 100,150,\"1\",0,1,1,\"NO SIRI: BPH/2025/11/0057\"\n")

        // 4️⃣ Example: Box drawing
        tsc.sendcommand("BOX 40,200,560,450,2\n")

        // 5️⃣ Example: Row text
        tsc.sendcommand("TEXT 60,220,\"1\",0,1,1,\"TARIKH: 11/11/2025\"\n")
        tsc.sendcommand("TEXT 60,250,\"1\",0,1,1,\"MASA: 02:10 PM\"\n")

        // 6️⃣ Example: QR Code
        tsc.sendcommand("QRCODE 180,480,L,6,A,0,\"BPH1234\"\n")

        // 7️⃣ Print
        tsc.printlabel(1, 1)

        // 8️⃣ Close port
        tsc.closeport(2000)

    } catch (e: Exception) {
        Log.e("TSC_SDK", "Print failed: ${e.message}", e)
    }
}

@SuppressLint("MissingPermission")
fun printBphNotisCajWithSdk(
    mac: String, context: Context,
    noSiri: String,
    tarikh: String,
    masa: String,
    noKenderaan: String,
    kadarCaj: String,
    jenisKenderaan: String,
    lokasi: String,
    pegawai: String,
    savedId: String,
    officerId: String = ""
) {
    val tsc = TSCActivity()

    try {
        // 1️⃣ Open Bluetooth connection
        tsc.openport(mac)
        tsc.setup(72, 150, 4, 8, 0, 0, 0)
        tsc.clearbuffer()

        // Load logo
        val bmpStream = context.assets.open("jata_malaysia_384px_bw.bmp")
        val originalBitmap = BitmapFactory.decodeStream(bmpStream)
        bmpStream.close()

        // Convert to pure mono
        val monoBitmap = convertToMonoBmp(originalBitmap)

        // Save converted bitmap to temp file
        val tempFile = File(context.cacheDir, "jata_temp_mono.bmp")
        FileOutputStream(tempFile).use { out ->
            monoBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        // Print image from path
        tsc.sendpicture(150, 10, tempFile.absolutePath)

        var y = 250

        // 2️⃣ Header
        tsc.sendcommand("TEXT 110,${y + 20},\"3\",0,1,1,\"JABATAN PERDANA MENTERI\"\n")
        tsc.sendcommand("TEXT 80,${y + 60},\"3\",0,1,1,\"BAHAGIAN PENGURUSAN HARTANAH\"\n")
        tsc.sendcommand("TEXT 200,${y + 110},\"4\",0,1,1,\"NOTIS CAJ\"\n")
        tsc.sendcommand("TEXT 125,${y + 160},\"2\",0,1,1,\"NO SIRI : $noSiri\"\n")

        // 3️⃣ Info text
        tsc.sendcommand("BLOCK 40,${y + 200},520,60,\"1\",0,1,1,0,2,\"Tuan/Puan telah meletak kenderaan di tempat yang tidak dibenarkan di Kompleks F. Oleh itu, tayar kenderaan tuan/puan telah diapit dan caj akan dikenakan.\"\n")

        // 4️⃣ Boxed detail table
        y += 270
        val boxBottom = y + 180
        tsc.sendcommand("BOX 40,$y,560,$boxBottom,2\n")

        y += 15
        fun row(label: String, value: String): Int {
            tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"$label\"\n")
            tsc.sendcommand("TEXT 280,$y,\"1\",0,1,1,\"$value\"\n")
            y += 28
            return y
        }

        row("TARIKH", "$tarikh")
        row("MASA", "$masa")
        row("NOMBOR KENDERAAN", "$noKenderaan")
        row("KADAR CAJ", "$kadarCaj $jenisKenderaan")
        row("LOKASI", "$lokasi")
        row("DIKELUARKAN OLEH", "$pegawai")

        // -----------------------------------------------@----------------------------------------------------
        y += 25
        // 3️⃣ Info text
        tsc.sendcommand("BLOCK 40,$y,520,60,\"1\",0,1,1,0,2,\"Sila jelaskan kadar caj yang dikenakan untuk membuka apitan dan kunci tayar kenderaan tuan/puan di alamat dan waktu yang tertera di bawah:\"\n")

        // 4️⃣ Boxed detail table
        y += 50
        val boxBottom2 = y + 230
        tsc.sendcommand("BOX 40,$y,560,$boxBottom2,2\n")

        y += 15
        fun row2(label: String, value: String): Int {
            tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"$label\"\n")
            tsc.sendcommand("BLOCK 200,$y,350,250,\"1\",0,1,1,0,2,\"$value\"\n")
            return y
        }

        row2(
            "TUNAI",
            "Kaunter Hasil (Blok F6)\nBahagian Pengurusan\nHartanah,\nJabatan Perdana Menteri\n Aras 2, " +
                    "Blok F6, Kompleks F\nPusat Pentadbiran Kerajaan\nPersekutuan\nLebuh Perdana Timur,\n Presint 1\n 62000 Putrajaya\n\n" +
                    "Isnin hingga Khamis\n" +
                    "9.00 pagi – 4.00 petang\n\n" +
                    "Jumaat\n" +
                    "9.00 pagi – 12.00 tengah hari\n" +
                    "3.00 petang – 4.00 petang"
        )

        // 4️⃣ Boxed detail table
        y += 215
        val boxBottom3 = y + 50
        tsc.sendcommand("BOX 40,$y,560,$boxBottom3,2\n")

        y += 15
        fun row3(label: String, value: String): Int {
            tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"$label\"\n")
            tsc.sendcommand("BLOCK 200,$y,350,60,\"1\",0,1,1,0,2,\"$value\"\n")
            return y
        }
        row3("DALAM TALIAN", "https://promis.bph.gov.my\n(24 jam)")

        // 8️⃣ Print & close
        tsc.printlabel(1, 1)
        tsc.closeport(2000)

    } catch (e: Exception) {
        Log.e("TSC_SDK", "Print failed: ${e.message}", e)
    }
}

@SuppressLint("MissingPermission")
fun printBphNotisCajWithSdkV2(
    mac: String, context: Context, gambarBitmap: Bitmap? = null,
    noSiri: String,
    tarikh: String,
    masa: String,
    noKenderaan: String,
    kadarCaj: String,
    jenisKenderaan: String,
    lokasi: String,
    pegawai: String,
    savedId: String,
    officerId: String = ""
) {
    val tsc = TSCActivity()

    try {
        // 1️⃣ Open Bluetooth connection
        tsc.openport(mac)
        tsc.setup(100, 220, 4, 8, 0, 0, 0)
        tsc.clearbuffer()

        // Load logo
        val bmpStream = context.assets.open("jata_malaysia_384px_bw.bmp")
        val originalBitmap = BitmapFactory.decodeStream(bmpStream)
        bmpStream.close()

        // Convert to pure mono
        val monoBitmap = convertToMonoBmp(originalBitmap)

        // Save converted bitmap to temp file
        val tempFile = File(context.cacheDir, "jata_temp_mono.bmp")
        FileOutputStream(tempFile).use { out ->
            monoBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        // Print image from path
        tsc.sendpicture(170, 10, tempFile.absolutePath)

        var y = 200

        // 2️⃣ Header
        tsc.sendcommand("TEXT 110,${y + 20},\"3\",0,1,1,\"JABATAN PERDANA MENTERI\"\n")
        tsc.sendcommand("TEXT 80,${y + 50},\"3\",0,1,1,\"BAHAGIAN PENGURUSAN HARTANAH\"\n")
        tsc.sendcommand("TEXT 200,${y + 110},\"4\",0,1,1,\"NOTIS CAJ\"\n")
        tsc.sendcommand("TEXT 125,${y + 160},\"2\",0,1,1,\"NO SIRI : $noSiri\"\n")

        // 3️⃣ Info text
        tsc.sendcommand("BLOCK 10,${y + 200},560,70,\"1\",0,1,1,0,2,\"Tuan/Puan telah meletak kenderaan di tempat yang tidak dibenarkan di Kompleks F. Oleh itu, tayar kenderaan tuan/puan telah diapit dan caj akan dikenakan.\"\n")

        // 4️⃣ Boxed detail table
        y += 250
        val boxBottom = y + 180
        tsc.sendcommand("BOX 20,$y,560,$boxBottom,2\n")

        y += 15
        fun row(label: String, value: String): Int {
            tsc.sendcommand("TEXT 40,$y,\"1\",0,1,1,\"$label\"\n")
            tsc.sendcommand("TEXT 280,$y,\"1\",0,1,1,\"$value\"\n")
            y += 28
            return y
        }

        row("TARIKH", "$tarikh")
        row("MASA", "$masa")
        row("NOMBOR KENDERAAN", "$noKenderaan")
        row("KADAR CAJ", "$kadarCaj $jenisKenderaan")
        row("LOKASI", "$lokasi")
        row("DIKELUARKAN OLEH", "$pegawai")

        // -----------------------------------------------@----------------------------------------------------
        y += 25
        // 3️⃣ Info text
        tsc.sendcommand("BLOCK 10,$y,560,70,\"1\",0,1,1,0,2,\"Sila jelaskan kadar caj yang dikenakan untuk membuka apitan dan kunci tayar kenderaan tuan/puan di alamat dan waktu yang tertera di bawah:\"\n")

        // 4️⃣ Boxed detail table
        y += 50
        val boxBottom2 = y + 50
        tsc.sendcommand("BOX 20,$y,560,$boxBottom2,2\n")

        y += 15
        fun row2(label: String): Int {
            tsc.sendcommand("BLOCK 35,$y,520,300,\"2\",0,1,1,0,2,\"$label\"\n")
            return y
        }

        row2("TUNAI / DALAM TALIAN")

        y += 35
        val boxBottom2a = y + 105
        tsc.sendcommand("BOX 20,$y,560,$boxBottom2a,2\n")

        y += 10
        fun row2a(label: String): Int {
            tsc.sendcommand("BLOCK 40,$y,520,300,\"1\",0,1,1,0,2,\"$label\"\n")
            return y
        }

        row2a(
            "Kaunter Hasil (Blok F6)\nBahagian Pengurusan Hartanah,\nJabatan Perdana Menteri Aras 2,\n" +
                    "Blok F6, Kompleks F\nPusat Pentadbiran Kerajaan Persekutuan\nLebuh Perdana Timur,\nPresint 1 62000 Putrajaya"
        )

        y += 95
        val boxBottom2b = y + 90
        tsc.sendcommand("BOX 20,$y,560,$boxBottom2b,2\n")

        y += 10
        fun row2b(label: String): Int {
            tsc.sendcommand("BLOCK 40,$y,520,300,\"1\",0,1,1,0,2,\"$label\"\n")
            return y
        }

        row2b("Isnin hingga Khamis\n9.00 pagi hingga 4.00 petang\n\n" +
                    "Jumaat\n" +
                    "9.00 pagi hingga 12.00 tengah hari\n" +
                    "3.00 petang hingga 4.00 petang"
        )

        // 4️⃣ Boxed detail table
        y += 80
        val boxBottom3 = y + 30
        tsc.sendcommand("BOX 20,$y,560,$boxBottom3,2\n")

        y += 10
        fun row3(label: String): Int {
            tsc.sendcommand("BLOCK 35,$y,520,300,\"1\",0,1,1,0,2,\"$label\"\n")
            return y
        }
        row3("https://promis.bph.gov.my (24 jam)")

        // 🟩 NOTES SECTION
        y += 45 // adjust position below your QR or last image
        tsc.sendcommand("TEXT 30,$y,\"2\",0,1,1,\"Nota: \"\n")

        y += 30
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"1. Punca Kuasa Pengapitan Tayar Kenderaan:\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\" i. Kelulusan dari Timbalan Ketua Setiausaha\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"    Kanan, Jabatan Perdana Menteri\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"ii. Kelulusan dari Pesuruhjaya Tanah Persekutuan\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"    Rujukan: JKPTG/UTP/356/17 JLD 3 bertarikh\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"    30 September 2022\"\n")
        y += 20
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"2. Kadar Caj RM50.00/ RM20.00:\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"    Kelulusan Kementerian Kewangan\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"    Malaysia\"\n")
        y += 20
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"3. Penafian BPH dan Kerajaan Malaysia tidak\"\n")
        y += 20
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"   bertanggungjawab atas sebarang kehilangan\"\n")
        y += 20
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"   atau kerosakan kepada kenderaan yang mungkin\"\n")
        y += 20
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"   berlaku.\"\n")
        y += 20
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"4. Had masa membuka apitan tayar adalah dari\"\n")
        y += 20
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"   9.00 pagi hingga 9.30 malam sahaja.\"\n")

        // ✅ Print uploaded image (if exists)
        gambarBitmap?.let { bmp ->
            try {
                // Convert to pure mono
                val monoBitmap = convertToMonoBmp(bmp)

                // Save converted bitmap to temp file
                val tempFile = File(context.cacheDir, "${savedId}_temp_mono.bmp")
                FileOutputStream(tempFile).use { out ->
                    monoBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                val photoY = y + 50 // offset below table
                tsc.sendpicture(10, photoY, tempFile.absolutePath)
            } catch (e: Exception) {
                Log.e("PRINT_IMG", "Failed to print uploaded image", e)
            }
        }

        // 8️⃣ Print & close
        tsc.printlabel(1, 1)
        tsc.closeport(2000)

    } catch (e: Exception) {
        Log.e("TSC_SDK", "Print failed: ${e.message}", e)
    }
}

@SuppressLint("MissingPermission")
fun printBphNotisCajWithSdkV2(
    mac: String, context: Context,
    noSiri: String,
    tarikh: String,
    masa: String,
    noKenderaan: String,
    kadarCaj: String,
    jenisKenderaan: String,
    lokasi: String,
    pegawai: String,
    savedId: String,
    officerId: String = ""
) {
    val tsc = TSCActivity()

    try {
        // 1️⃣ Open Bluetooth connection
        tsc.openport(mac)
        tsc.setup(100, 170, 4, 8, 0, 0, 0)
        tsc.clearbuffer()

        // Load logo
        val bmpStream = context.assets.open("jata_malaysia_384px_bw.bmp")
        val originalBitmap = BitmapFactory.decodeStream(bmpStream)
        bmpStream.close()

        // Convert to pure mono
        val monoBitmap = convertToMonoBmp(originalBitmap)

        // Save converted bitmap to temp file
        val tempFile = File(context.cacheDir, "jata_temp_mono.bmp")
        FileOutputStream(tempFile).use { out ->
            monoBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        // Print image from path
        tsc.sendpicture(170, 10, tempFile.absolutePath)

        var y = 200

        // 2️⃣ Header
        tsc.sendcommand("TEXT 110,${y + 20},\"3\",0,1,1,\"JABATAN PERDANA MENTERI\"\n")
        tsc.sendcommand("TEXT 80,${y + 50},\"3\",0,1,1,\"BAHAGIAN PENGURUSAN HARTANAH\"\n")
        tsc.sendcommand("TEXT 200,${y + 110},\"4\",0,1,1,\"NOTIS CAJ\"\n")
        tsc.sendcommand("TEXT 125,${y + 160},\"2\",0,1,1,\"NO SIRI : $noSiri\"\n")

        // 3️⃣ Info text
        tsc.sendcommand("BLOCK 10,${y + 200},560,70,\"1\",0,1,1,0,2,\"Tuan/Puan telah meletak kenderaan di tempat yang tidak dibenarkan di Kompleks F. Oleh itu, tayar kenderaan tuan/puan telah diapit dan caj akan dikenakan.\"\n")

        // 4️⃣ Boxed detail table
        y += 250
        val boxBottom = y + 180
        tsc.sendcommand("BOX 20,$y,560,$boxBottom,2\n")

        y += 15
        fun row(label: String, value: String): Int {
            tsc.sendcommand("TEXT 40,$y,\"1\",0,1,1,\"$label\"\n")
            tsc.sendcommand("TEXT 260,$y,\"1\",0,1,1,\"$value\"\n")
            y += 28
            return y
        }

        row("TARIKH", "$tarikh")
        row("MASA", "$masa")
        row("NOMBOR KENDERAAN", "$noKenderaan")
        row("KADAR CAJ", "$kadarCaj $jenisKenderaan")
        row("LOKASI", "$lokasi")
        row("DIKELUARKAN OLEH", "$pegawai")

        // -----------------------------------------------@----------------------------------------------------
        y += 25
        // 3️⃣ Info text
        tsc.sendcommand("BLOCK 10,$y,560,70,\"1\",0,1,1,0,2,\"Sila jelaskan kadar caj yang dikenakan untuk membuka apitan dan kunci tayar kenderaan tuan/puan di alamat dan waktu yang tertera di bawah:\"\n")

        // 4️⃣ Boxed detail table
        y += 50
        val boxBottom2 = y + 50
        tsc.sendcommand("BOX 20,$y,560,$boxBottom2,2\n")

        y += 15
        fun row2(label: String): Int {
            tsc.sendcommand("BLOCK 35,$y,520,300,\"2\",0,1,1,0,2,\"$label\"\n")
            return y
        }

        row2("TUNAI / DALAM TALIAN")

        y += 35
        val boxBottom2a = y + 105
        tsc.sendcommand("BOX 20,$y,560,$boxBottom2a,2\n")

        y += 10
        fun row2a(label: String): Int {
            tsc.sendcommand("BLOCK 40,$y,520,300,\"1\",0,1,1,0,2,\"$label\"\n")
            return y
        }

        row2a(
            "Kaunter Hasil (Blok F6)\nBahagian Pengurusan Hartanah,\nJabatan Perdana Menteri Aras 2,\n" +
                    "Blok F6, Kompleks F\nPusat Pentadbiran Kerajaan Persekutuan\nLebuh Perdana Timur,\nPresint 1 62000 Putrajaya"
        )

        y += 95
        val boxBottom2b = y + 90
        tsc.sendcommand("BOX 20,$y,560,$boxBottom2b,2\n")

        y += 10
        fun row2b(label: String): Int {
            tsc.sendcommand("BLOCK 40,$y,520,300,\"1\",0,1,1,0,2,\"$label\"\n")
            return y
        }

        row2b("Isnin hingga Khamis\n9.00 pagi hingga 4.00 petang\n\n" +
                "Jumaat\n" +
                "9.00 pagi hingga 12.00 tengah hari\n" +
                "3.00 petang hingga 4.00 petang"
        )

        // 4️⃣ Boxed detail table
        y += 80
        val boxBottom3 = y + 30
        tsc.sendcommand("BOX 20,$y,560,$boxBottom3,2\n")

        y += 10
        fun row3(label: String): Int {
            tsc.sendcommand("BLOCK 35,$y,520,300,\"1\",0,1,1,0,2,\"$label\"\n")
            return y
        }
        row3("https://promis.bph.gov.my (24 jam)")

        // 🟩 NOTES SECTION
        y += 45 // adjust position below your QR or last image
        tsc.sendcommand("TEXT 30,$y,\"2\",0,1,1,\"Nota: \"\n")

        y += 30
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"1. Punca Kuasa Pengapitan Tayar Kenderaan:\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\" i. Kelulusan dari Timbalan Ketua Setiausaha\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"    Kanan, Jabatan Perdana Menteri\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"ii. Kelulusan dari Pesuruhjaya Tanah Persekutuan\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"    Rujukan: JKPTG/UTP/356/17 JLD 3 bertarikh\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"    30 September 2022\"\n")
        y += 20
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"2. Kadar Caj RM50.00/ RM20.00:\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"    Kelulusan Kementerian Kewangan\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"    Malaysia\"\n")
        y += 20
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"3. Penafian BPH dan Kerajaan Malaysia tidak\"\n")
        y += 20
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"   bertanggungjawab atas sebarang kehilangan\"\n")
        y += 20
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"   atau kerosakan kepada kenderaan yang mungkin\"\n")
        y += 20
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"   berlaku.\"\n")
        y += 20
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"4. Had masa membuka apitan tayar adalah dari\"\n")
        y += 20
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"   9.00 pagi hingga 9.30 malam sahaja.\"\n")

//        y += 35
//        tsc.sendcommand("BLOCK 60,$y,500,400,\"1\",0,1,1,0,2,\"\n" +
//                "1. Punca Kuasa Pengapitan Tayar Kenderaan:\n" +
//                "   • Kelulusan dari Timbalan Ketua Setiausaha Kanan, Jabatan Perdana Menteri\n" +
//                "   • Kelulusan dari Pesuruhjaya Tanah Persekutuan\n" +
//                "     Rujukan: JKPTG/UTP/356/17 JLD 3 bertarikh 30 September 2022\n\n" +
//                "2. Kadar Caj RM50.00 / RM20.00:\n" +
//                "   Kelulusan Kementerian Kewangan Malaysia\n" +
//                "   Rujukan: MOF.PAM.600-29/44/1 JLD.5 (5) bertarikh 17 November 2022\n\n" +
//                "3. Penafian BPH dan Kerajaan Malaysia tidak bertanggungjawab atas sebarang kehilangan atau kerosakan kepada kenderaan yang mungkin berlaku.\n\n" +
//                "4. Had masa membuka apitan tayar adalah dari 9.00 pagi hingga 9.30 malam sahaja.\"\n")

        // 8️⃣ Print & close
        tsc.printlabel(1, 1)
        tsc.closeport(2000)

    } catch (e: Exception) {
        Log.e("TSC_SDK", "Print failed: ${e.message}", e)
    }
}

@SuppressLint("MissingPermission")
fun printBphNotisCajWithSdk_Debug(mac: String, context: Context, gambarBitmap: Bitmap? = null) {
    val tsc = TSCActivity()

    try {
        Log.d("TSC_DEBUG", "Opening port...")
        tsc.openport(mac)
        tsc.setup(72, 300, 4, 8, 0, 0, 0)
        Log.d("TSC_DEBUG", "Port opened & setup OK")
        tsc.clearbuffer()

        // --- Send a simple test first ---
        Log.d("TSC_DEBUG", "Sending TEXT test...")
        tsc.sendcommand("TEXT 100,50,\"3\",0,1,1,\"DEBUG TEST PRINT\"")
        tsc.printlabel(1, 1)
        Thread.sleep(2000)
        Log.d("TSC_DEBUG", "✅ TEST PRINT done")

        // Now continue your normal content (step by step)
        tsc.clearbuffer()

        // Try first part: header
        Log.d("TSC_DEBUG", "Printing header...")
        tsc.sendcommand("TEXT 110,50,\"3\",0,1,1,\"HEADER TEST\"")
        tsc.printlabel(1, 1)
        Thread.sleep(1500)

        // Try block text
        tsc.clearbuffer()
        Log.d("TSC_DEBUG", "Printing block text test...")
        tsc.sendcommand("BLOCK 40,100,520,60,\"1\",0,1,1,0,2,\"This is block test.\"")
        tsc.printlabel(1, 1)
        Thread.sleep(1500)

        var y = 30
        gambarBitmap?.let { bmp ->
            try {
                Log.d("PRINT_IMG", "Converting uploaded image...")

                // 🔹 Step 1: Resize safely (width multiple of 8)
                val targetWidth = 384  // max width for Alpha-30L (72mm * 8 dots/mm)
                val maxHeight = 1000   // keep within label memory buffer

                val aspect = bmp.height.toFloat() / bmp.width.toFloat()
                val newHeight = (targetWidth * aspect).toInt().coerceAtMost(maxHeight)

                val resized = Bitmap.createScaledBitmap(bmp, targetWidth, newHeight, true)

                // 🔹 Step 2: Convert to monochrome manually
                val bw = Bitmap.createBitmap(targetWidth, newHeight, Bitmap.Config.ARGB_8888)
                val c = Canvas(bw)
                val p = Paint().apply {
                    colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
                }
                c.drawBitmap(resized, 0f, 0f, p)

                for (yPos in 0 until newHeight) {
                    for (x in 0 until targetWidth) {
                        val pixel = bw.getPixel(x, yPos)
                        val gray = Color.red(pixel)
                        bw.setPixel(x, yPos, if (gray < 180) Color.BLACK else Color.WHITE)
                    }
                }

                // 🔹 Step 3: Save as BMP (never PNG)
                val tempFile = File(context.cacheDir, "${System.currentTimeMillis()}_photo.bmp")
                saveAsBmp(bw, tempFile)
                Log.d("PRINT_IMG", "Saved BMP to ${tempFile.absolutePath}")

                // 🔹 Step 4: Send to printer
                val photoY = y + 100
                val centerX = (576 - targetWidth) / 2

                Log.d("TSC_DEBUG", "BMP 001 : ${tempFile.absolutePath}")
                Log.d("TSC_DEBUG", "BMP 002 : ${tempFile.name}")

                val bmpFromFile = BitmapFactory.decodeFile(tempFile.absolutePath)
                // Print label text + image
                tsc.sendcommand("TEXT ${centerX + 80},${photoY - 25},\"1\",0,1,1,\"GAMBAR APITAN\"")
//                tsc.sendpicture(centerX, photoY, tempFile.absolutePath)
                tsc.sendbitmap(centerX, photoY, bmpFromFile)
//                tsc.sendcommand("PUTBMP $centerX,$photoY,\"${tempFile.name}\"\n");
//                tsc.sendcommand("TEXT ${centerX + 80},${photoY - 25},\"1\",0,1,1,\"GAMBAR APITAN\"")
//                tsc.sendpicture(centerX, photoY, tempFile.absolutePath)

                Log.d("PRINT_IMG", "✅ Sent resized ${bw.width}x${bw.height} image to printer")

            } catch (e: Exception) {
                Log.e("PRINT_IMG", "❌ Failed to print uploaded image", e)
            }
        }

        // End
        tsc.closeport(2000)
        Log.d("TSC_DEBUG", "✅ Port closed successfully")

    } catch (e: Exception) {
        Log.e("TSC_DEBUG", "Print failed: ${e.message}", e)
    }
}

@SuppressLint("MissingPermission")
fun testPrintImageOnly(mac: String, context: Context) {
    val tsc = TSCActivity()
    tsc.openport(mac)
    tsc.setup(72, 100, 4, 8, 0, 0, 0)
    tsc.clearbuffer()

    val bmpStream = context.assets.open("jata_malaysia_384px_bw.bmp")
    val bmp = BitmapFactory.decodeStream(bmpStream)
    bmpStream.close()

    val mono = convertToMonoBmp(bmp)
    val file = File(context.cacheDir, "logo_test.bmp")
    FileOutputStream(file).use { out -> mono.compress(Bitmap.CompressFormat.PNG, 100, out) }

    tsc.sendpicture(100, 50, file.absolutePath)
    tsc.printlabel(1, 1)
    tsc.closeport(2000)
}

fun convertToMonoBmp(original: Bitmap): Bitmap {
    val width = original.width
    val height = original.height
    val monoBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(monoBitmap)
    val paint = Paint()
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(0f) // grayscale
    val filter = ColorMatrixColorFilter(colorMatrix)
    paint.colorFilter = filter
    canvas.drawBitmap(original, 0f, 0f, paint)

    // Dithering: convert to pure black/white
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = monoBitmap.getPixel(x, y)
            val gray = Color.red(pixel)
            monoBitmap.setPixel(x, y, if (gray < 160) Color.BLACK else Color.WHITE)
        }
    }
    return monoBitmap
}

fun convertToPrinterMono(context: Context, bitmap: Bitmap, targetWidth: Int = 384): File {
    val ratio = targetWidth / bitmap.width.toFloat()
    val height = (bitmap.height * ratio).toInt()
    val resized = Bitmap.createScaledBitmap(bitmap, targetWidth, height, true)

    // Convert to grayscale → black & white
    val gray = Bitmap.createBitmap(targetWidth, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(gray)
    val paint = Paint().apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
    }
    canvas.drawBitmap(resized, 0f, 0f, paint)

    for (y in 0 until height) {
        for (x in 0 until targetWidth) {
            val pixel = gray.getPixel(x, y)
            val brightness = Color.red(pixel)
            gray.setPixel(x, y, if (brightness < 160) Color.BLACK else Color.WHITE)
        }
    }

    // Save as temp BMP for printer
    val tempFile = File(context.cacheDir, "uploaded_photo.bmp")
    FileOutputStream(tempFile).use { out ->
        gray.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return tempFile
}

fun saveAsBmp(bitmap: Bitmap, file: File) {
    val width = bitmap.width
    val height = bitmap.height

    // Each row must be padded to a multiple of 4 bytes
    val bytesPerRow = ((width + 31) / 32) * 4
    val pixelArraySize = bytesPerRow * height
    val fileHeaderSize = 14
    val dibHeaderSize = 40
    val colorTableSize = 8  // black + white
    val offsetToPixels = fileHeaderSize + dibHeaderSize + colorTableSize
    val fileSize = offsetToPixels + pixelArraySize

    val buffer = ByteBuffer.allocate(fileSize)
    buffer.order(ByteOrder.LITTLE_ENDIAN)

    // === BMP FILE HEADER ===
    buffer.put('B'.code.toByte())
    buffer.put('M'.code.toByte())
    buffer.putInt(fileSize)
    buffer.putShort(0)
    buffer.putShort(0)
    buffer.putInt(offsetToPixels)

    // === DIB HEADER ===
    buffer.putInt(dibHeaderSize)
    buffer.putInt(width)
    buffer.putInt(height)
    buffer.putShort(1) // planes
    buffer.putShort(1) // bits per pixel
    buffer.putInt(0)   // no compression
    buffer.putInt(pixelArraySize)
    buffer.putInt(0) // x pixels per meter
    buffer.putInt(0) // y pixels per meter
    buffer.putInt(2) // colors used
    buffer.putInt(0) // important colors

    // === COLOR TABLE ===
    buffer.put(0x00) // black
    buffer.put(0x00)
    buffer.put(0x00)
    buffer.put(0x00)
    buffer.put(0xFF.toByte()) // white
    buffer.put(0xFF.toByte())
    buffer.put(0xFF.toByte())
    buffer.put(0x00)

    // === PIXEL DATA ===
    val row = ByteArray(bytesPerRow)
    for (y in height - 1 downTo 0) {
        row.fill(0)
        var bitIndex = 0
        var byteIndex = 0
        for (x in 0 until width) {
            val color = bitmap.getPixel(x, y)
            val bit = if (Color.red(color) < 128) 1 else 0
            row[byteIndex] = (row[byteIndex].toInt() or (bit shl (7 - bitIndex))).toByte()
            bitIndex++
            if (bitIndex == 8) {
                bitIndex = 0
                byteIndex++
            }
        }
        buffer.put(row)
    }

    FileOutputStream(file).use { it.write(buffer.array()) }
}