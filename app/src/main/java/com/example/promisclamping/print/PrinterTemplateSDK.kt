package com.example.promisclamping.print

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import com.example.tscdll.TSCActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

@SuppressLint("MissingPermission")
suspend fun printBphNotisCajWithSdkV2(
    mac: String, context: Context, gambarBitmap: Bitmap? = null,
    noSiri: String,
    tarikh: String,
    masa: String,
    noKenderaan: String,
    kadarCaj: String,
    jenisKenderaan: String,
    lokasi: String,
    pegawai: String,
    savedId: String
) = withContext(Dispatchers.Main) {
    // Ensure we actually have an Activity if the SDK needs it
    val activity = context as? Activity
        ?: throw IllegalArgumentException("Context must be an Activity for TSCActivity")

    val tsc = TSCActivity()

    try {
        // 1️⃣ Open Bluetooth connection
        tsc.openport(mac)

        // 2️⃣ Clear printer’s image/format buffer
        tsc.clearbuffer()               // SDK helper
        tsc.sendcommand("CLS\r\n")      // extra safety – TSPL command

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

        row2b(
            "Isnin hingga Khamis\n9.00 pagi hingga 4.00 petang\n\n" +
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
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"    30 September 2022;\"\n")
        y += 20
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"2. Kadar Caj RM50.00/ RM20.00:\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"    Kelulusan Kementerian Kewangan\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"    Malaysia. Rujukan: MOF.PAM.600-29/44/1\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"    Jld.5 (5) bertarikh 17 November 2022; \"\n")
        y += 20
        tsc.sendcommand("TEXT 50,$y,\"1\",0,1,1,\"3. Penafian: BPH dan Kerajaan Malaysia tidak\"\n")
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
//                val monoBitmap = convertToMonoBmp(bmp)
                val monoBitmap = convertToMonoBmpResized(bmp)
                val jpegBytes = bitmapToJpegUnder1Mb(monoBitmap)

                // Save converted bitmap to temp file
//                val tempFile = File(context.cacheDir, "${savedId}_temp_mono.bmp")
                val tempFile = File(context.cacheDir, "${savedId}_temp_mono.bmp")
                FileOutputStream(tempFile).use { out ->
                    monoBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                val photoY = y + 50 // offset below table
                tsc.sendpicture(100, photoY, tempFile.absolutePath)
            } catch (e: Exception) {
                Log.e("PRINT_IMG", "Failed to print uploaded image", e)
            }
        }

        // 8️⃣ Print & close
        tsc.printlabel(1, 1)
//        tsc.closeport(2000)

        // 5️⃣ (Optional) Clear again after print
        tsc.sendcommand("CLS\r\n")
    } catch (e: Exception) {
        Log.e("TSC_SDK", "Print failed: ${e.message}", e)
    } finally {
        try {
            tsc.closeport(2000)
        } catch (e: Exception) {
            Log.e("TSC_SDK", "Closeport failed", e)
        }
    }
}

@SuppressLint("MissingPermission")
suspend fun printBphNotisCajWithSdkV2(
    mac: String, context: Context,
    noSiri: String,
    tarikh: String,
    masa: String,
    noKenderaan: String,
    kadarCaj: String,
    jenisKenderaan: String,
    lokasi: String,
    pegawai: String
) = withContext(Dispatchers.Main) {
    // Ensure we actually have an Activity if the SDK needs it
    val activity = context as? Activity
        ?: throw IllegalArgumentException("Context must be an Activity for TSCActivity")

    val tsc = TSCActivity()

    try {
        // 1️⃣ Open Bluetooth connection
        tsc.openport(mac)

        // 2️⃣ Clear printer’s image/format buffer
        tsc.clearbuffer()               // SDK helper
        tsc.sendcommand("CLS\r\n")      // extra safety – TSPL command

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

        row2b(
            "Isnin hingga Khamis\n9.00 pagi hingga 4.00 petang\n\n" +
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
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"    Malaysia. Rujukan: MOF.PAM.600-29/44/1\"\n")
        y += 20
        tsc.sendcommand("TEXT 60,$y,\"1\",0,1,1,\"    Jld.5 (5) bertarikh 17 November 2022; \"\n")
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
//        tsc.closeport(2000)

        // 5️⃣ (Optional) Clear again after print
        tsc.sendcommand("CLS\r\n")
    } catch (e: Exception) {
        Log.e("TSC_SDK", "Print failed: ${e.message}", e)
    } finally {
        try {
            tsc.closeport(2000)
        } catch (e: Exception) {
            Log.e("TSC_SDK", "Closeport failed", e)
        }
    }
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

fun convertToMonoBmpResized(
    original: Bitmap,
    maxWidth: Int = 384,          // adjust to your printer head width if needed
    maxHeight: Int = 384,         // or larger if you want
    maxBytes: Int = 1_000_000     // ~1 MB in memory
): Bitmap {
    val origWidth = original.width
    val origHeight = original.height

    if (origWidth <= 0 || origHeight <= 0) {
        throw IllegalArgumentException("Invalid bitmap size: $origWidth x $origHeight")
    }

    // 1️⃣ Base scale from width/height constraints
    var scale = 1f
    val scaleW = maxWidth.toFloat() / origWidth.toFloat()
    val scaleH = maxHeight.toFloat() / origHeight.toFloat()
    scale = minOf(1f, scaleW, scaleH)   // don't upscale; only shrink

    // 2️⃣ Extra scale from maxBytes (in-memory bitmap size)
    val maxPixels = maxBytes / 4       // ARGB_8888 = 4 bytes per pixel
    val origPixels = origWidth.toLong() * origHeight.toLong()
    if (origPixels > maxPixels) {
        val scaleByBytes = kotlin.math.sqrt(maxPixels.toDouble() / origPixels.toDouble()).toFloat()
        scale = minOf(scale, scaleByBytes)
    }

    // 3️⃣ Actually scale the bitmap if needed
    val scaledBitmap = if (scale < 1f) {
        val newWidth = (origWidth * scale).toInt().coerceAtLeast(1)
        val newHeight = (origHeight * scale).toInt().coerceAtLeast(1)
        Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
    } else {
        original
    }

    val width = scaledBitmap.width
    val height = scaledBitmap.height

    // 4️⃣ Create target bitmap and draw grayscale
    val monoBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(monoBitmap)
    val paint = Paint()
    val colorMatrix = ColorMatrix().apply {
        setSaturation(0f) // grayscale
    }
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)

    // 5️⃣ Threshold to pure black & white
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = monoBitmap.getPixel(x, y)
            val gray = Color.red(pixel) // after grayscale, R=G=B
            monoBitmap.setPixel(x, y, if (gray < 160) Color.BLACK else Color.WHITE)
        }
    }

    return monoBitmap
}

fun bitmapToJpegUnder1Mb(
    bitmap: Bitmap,
    maxBytes: Int = 1_000_000,
    minQuality: Int = 40
): ByteArray {
    var quality = 100
    val stream = java.io.ByteArrayOutputStream()

    do {
        stream.reset()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        quality -= 5
    } while (stream.size() > maxBytes && quality >= minQuality)

    return stream.toByteArray()
}
