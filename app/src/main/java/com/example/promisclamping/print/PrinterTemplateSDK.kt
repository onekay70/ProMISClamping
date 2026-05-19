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
import kotlin.math.ceil

@SuppressLint("MissingPermission")
suspend fun printBphNotisCajWithSdkV2(
    mac: String,
    context: Context,
    gambarBitmap: Bitmap? = null,
    noSiri: String,
    tarikh: String,
    masa: String,
    noKenderaan: String,
    kadarCaj: String,
    jenisKenderaan: String,
    lokasi: String,
    pegawai: String,
    savedId: String
) {
    val receiptWidthMm = 100
    val defaultHeightMmWithoutPhoto = 190

    val printableWidthDots = 576
    val photoTargetWidthDots = 520

    /**
     * Process gambar dekat background thread.
     * Jangan buat pixel loop dekat Main thread.
     */
    val uploadedPhotoBitmap: Bitmap? = withContext(Dispatchers.Default) {
        gambarBitmap?.let { bmp ->
            convertToDitheredMonoBitmapFitReceiptWidth(
                original = bmp,
                targetWidth = 500,
                maxAbsoluteHeight = 900,
                cropToLandscape = true,
                brightness = 45,
                contrast = 1.25f,
                threshold = 160f
            )
        }
    }

    val noteEndY = calculateBphTemplateNoteEndY()
    val photoY = noteEndY + 50

    val totalHeightMm = if (uploadedPhotoBitmap != null) {
        val totalHeightDots = photoY + uploadedPhotoBitmap.height + 100
        dotsToMm(totalHeightDots).coerceAtLeast(220)
    } else {
        defaultHeightMmWithoutPhoto
    }

    /**
     * Semua TSC SDK call mesti dekat Main thread.
     * Sebab SDK tu internally guna Handler.
     */
    withContext(Dispatchers.Main) {
        val tsc = TSCActivity()

        try {
            Log.d(
                "PRINT_SETUP",
                "receiptHeightMm=$totalHeightMm, photo=${uploadedPhotoBitmap?.width}x${uploadedPhotoBitmap?.height}, photoY=$photoY"
            )

            openTscPortWithRetry(tsc, mac)

            safeTscCommand(tsc, "CLS\r\n")
            Thread.sleep(200)

            tsc.setup(receiptWidthMm, totalHeightMm, 4, 8, 0, 0, 0)

//            tsc.clearbuffer()
            safeTscCommand(tsc, "CLS\r\n")

            val bmpStream = context.assets.open("jata_malaysia_384px_bw.bmp")
            val logoBitmap = BitmapFactory.decodeStream(bmpStream)
            bmpStream.close()

            tsc.sendbitmap(178, 15, logoBitmap, 200)

            var y = 200

            // Header
            tsc.sendcommand("TEXT 110,${y + 20},\"3\",0,1,1,\"JABATAN PERDANA MENTERI\"\n")
            tsc.sendcommand("TEXT 80,${y + 50},\"3\",0,1,1,\"BAHAGIAN PENGURUSAN HARTANAH\"\n")
            tsc.sendcommand("TEXT 200,${y + 110},\"4\",0,1,1,\"NOTIS CAJ\"\n")
            tsc.sendcommand("TEXT 125,${y + 160},\"2\",0,1,1,\"NO SIRI : $noSiri\"\n")

            tsc.sendcommand(
                "BLOCK 10,${y + 200},560,70,\"1\",0,1,1,0,2,\"" +
                        "Tuan/Puan telah meletak kenderaan di tempat yang tidak dibenarkan di Kompleks F. " +
                        "Oleh itu, tayar kenderaan tuan/puan telah diapit dan caj akan dikenakan.\"\n"
            )

            y += 250
            val boxBottom = y + 180
            tsc.sendcommand("BOX 20,$y,560,$boxBottom,2\n")

            y += 15

            fun row(label: String, value: String) {
                tsc.sendcommand("TEXT 40,$y,\"1\",0,1,1,\"$label\"\n")
                tsc.sendcommand("TEXT 280,$y,\"1\",0,1,1,\"$value\"\n")
                y += 28
            }

            row("TARIKH", tarikh)
            row("MASA", masa)
            row("NOMBOR KENDERAAN", noKenderaan)
            row("KADAR CAJ", "$kadarCaj $jenisKenderaan")
            row("LOKASI", lokasi)
            row("DIKELUARKAN OLEH", pegawai)

            y += 25

            tsc.sendcommand(
                "BLOCK 10,$y,560,70,\"1\",0,1,1,0,2,\"" +
                        "Sila jelaskan kadar caj yang dikenakan untuk membuka apitan dan kunci tayar " +
                        "kenderaan tuan/puan di alamat dan waktu yang tertera di bawah:\"\n"
            )

            y += 50
            val boxBottom2 = y + 50
            tsc.sendcommand("BOX 20,$y,560,$boxBottom2,2\n")

            y += 15
            tsc.sendcommand("BLOCK 35,$y,520,300,\"2\",0,1,1,0,2,\"TUNAI / DALAM TALIAN\"\n")

            y += 35
            val boxBottom2a = y + 105
            tsc.sendcommand("BOX 20,$y,560,$boxBottom2a,2\n")

            y += 10
            tsc.sendcommand(
                "BLOCK 40,$y,520,300,\"1\",0,1,1,0,2,\"" +
                        "Kaunter Hasil (Blok F6)\n" +
                        "Bahagian Pengurusan Hartanah,\n" +
                        "Jabatan Perdana Menteri Aras 2,\n" +
                        "Blok F6, Kompleks F\n" +
                        "Pusat Pentadbiran Kerajaan Persekutuan\n" +
                        "Lebuh Perdana Timur,\n" +
                        "Presint 1 62000 Putrajaya\"\n"
            )

            y += 95
            val boxBottom2b = y + 90
            tsc.sendcommand("BOX 20,$y,560,$boxBottom2b,2\n")

            y += 10
            tsc.sendcommand(
                "BLOCK 40,$y,520,300,\"1\",0,1,1,0,2,\"" +
                        "Isnin hingga Khamis\n" +
                        "9.00 pagi hingga 4.00 petang\n\n" +
                        "Jumaat\n" +
                        "9.00 pagi hingga 12.00 tengah hari\n" +
                        "3.00 petang hingga 4.00 petang\"\n"
            )

            y += 80
            val boxBottom3 = y + 30
            tsc.sendcommand("BOX 20,$y,560,$boxBottom3,2\n")

            y += 10
            tsc.sendcommand("BLOCK 35,$y,520,300,\"1\",0,1,1,0,2,\"https://promis.bph.gov.my (24 jam)\"\n")

            y += 45
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

            uploadedPhotoBitmap?.let { photo ->
                val actualPhotoY = y + 50

                val contentLeftX = 20
                val contentRightX = 560
                val contentWidth = contentRightX - contentLeftX

                val actualPhotoX =
                    contentLeftX + ((contentWidth - photo.width) / 2).coerceAtLeast(0)

                Log.d(
                    "PRINT_IMG",
                    "sendbitmap x=$actualPhotoX, y=$actualPhotoY, size=${photo.width}x${photo.height}"
                )

                tsc.sendbitmap(actualPhotoX, actualPhotoY, photo, 128)

                Thread.sleep(1200)
            }

            Thread.sleep(500)

            tsc.printlabel(1, 1)

            Thread.sleep(2500)

//            tsc.clearbuffer()
//            safeTscCommand(tsc, "CLS\r\n")

        } catch (e: Exception) {
            Log.e("TSC_SDK", "Print failed: ${e.message}", e)
            throw e
        } finally {
            try {
                Thread.sleep(500)
                tsc.closeport(2000)
            } catch (e: Exception) {
                Log.e("TSC_SDK", "Closeport failed", e)
            }
        }
    }
}

@SuppressLint("MissingPermission")
suspend fun printBphNotisCajWithSdkV2(
    mac: String,
    context: Context,
    noSiri: String,
    tarikh: String,
    masa: String,
    noKenderaan: String,
    kadarCaj: String,
    jenisKenderaan: String,
    lokasi: String,
    pegawai: String
) {
    printBphNotisCajWithSdkV2(
        mac = mac,
        context = context,
        gambarBitmap = null,
        noSiri = noSiri,
        tarikh = tarikh,
        masa = masa,
        noKenderaan = noKenderaan,
        kadarCaj = kadarCaj,
        jenisKenderaan = jenisKenderaan,
        lokasi = lokasi,
        pegawai = pegawai,
        savedId = "-"
    )
}

private fun dotsToMm(dots: Int): Int {
    return ceil(dots / 8.0).toInt()
}

private fun calculateBphTemplateNoteEndY(): Int {
    var y = 200

    // Header tidak ubah y secara direct

    // Detail table
    y += 250
    y += 15

    // 6 rows
    repeat(6) {
        y += 28
    }

    y += 25

    // Payment title box
    y += 50
    y += 15

    // TUNAI / DALAM TALIAN
    y += 35

    // Address box
    y += 10
    y += 95

    // Time box
    y += 10
    y += 80

    // URL box
    y += 10

    // Notes section
    y += 45
    y += 30

    // Notes lines.
    // First note line printed, then 16 increments of 20 based on current template.
    repeat(16) {
        y += 20
    }

    return y
}

fun convertToDitheredMonoBitmapFitReceiptWidth(
    original: Bitmap,
    targetWidth: Int = 500,
    maxAbsoluteHeight: Int = 1100,
    cropToLandscape: Boolean = true,
    brightness: Int = 45,
    contrast: Float = 1.25f,
    threshold: Float = 160f
): Bitmap {
    val source = if (cropToLandscape) {
        centerCropBitmap(original, targetAspectRatio = 4f / 3f)
    } else {
        original
    }

    val origWidth = source.width
    val origHeight = source.height

    require(origWidth > 0 && origHeight > 0) {
        "Invalid bitmap size: $origWidth x $origHeight"
    }

    var scale = targetWidth.toFloat() / origWidth.toFloat()
    var newWidth = targetWidth
    var newHeight = (origHeight * scale).toInt().coerceAtLeast(1)

    if (newHeight > maxAbsoluteHeight) {
        scale = maxAbsoluteHeight.toFloat() / origHeight.toFloat()
        newHeight = maxAbsoluteHeight
        newWidth = (origWidth * scale).toInt().coerceAtLeast(1)
    }

    val resized = Bitmap.createScaledBitmap(
        source,
        newWidth,
        newHeight,
        true
    )

    val width = resized.width
    val height = resized.height

    Log.d(
        "PRINT_IMG",
        "source=${original.width}x${original.height}, cropped=${source.width}x${source.height}, resized=${width}x${height}"
    )

    val gray = Array(height) { FloatArray(width) }

    for (y in 0 until height) {
        for (x in 0 until width) {
            val p = resized.getPixel(x, y)

            val r = Color.red(p)
            val g = Color.green(p)
            val b = Color.blue(p)

            var luminance = (0.299f * r + 0.587f * g + 0.114f * b)
            luminance = ((luminance - 128f) * contrast) + 128f + brightness

            gray[y][x] = luminance.coerceIn(0f, 255f)
        }
    }

    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val oldPixel = gray[y][x]
            val newPixel = if (oldPixel < threshold) 0f else 255f
            val error = oldPixel - newPixel

            out.setPixel(
                x,
                y,
                if (newPixel == 0f) Color.BLACK else Color.WHITE
            )

            if (x + 1 < width) {
                gray[y][x + 1] =
                    (gray[y][x + 1] + error * 7f / 16f).coerceIn(0f, 255f)
            }

            if (y + 1 < height) {
                if (x > 0) {
                    gray[y + 1][x - 1] =
                        (gray[y + 1][x - 1] + error * 3f / 16f).coerceIn(0f, 255f)
                }

                gray[y + 1][x] =
                    (gray[y + 1][x] + error * 5f / 16f).coerceIn(0f, 255f)

                if (x + 1 < width) {
                    gray[y + 1][x + 1] =
                        (gray[y + 1][x + 1] + error * 1f / 16f).coerceIn(0f, 255f)
                }
            }
        }
    }

    if (resized != source && !resized.isRecycled) {
        resized.recycle()
    }

    if (source != original && !source.isRecycled) {
        source.recycle()
    }

    return out
}

fun centerCropBitmap(
    original: Bitmap,
    targetAspectRatio: Float = 4f / 3f
): Bitmap {
    val width = original.width
    val height = original.height

    if (width <= 0 || height <= 0) {
        throw IllegalArgumentException("Invalid bitmap size: $width x $height")
    }

    val currentAspectRatio = width.toFloat() / height.toFloat()

    return if (currentAspectRatio > targetAspectRatio) {
        // terlalu wide, crop kiri kanan
        val newWidth = (height * targetAspectRatio).toInt().coerceAtMost(width)
        val xOffset = ((width - newWidth) / 2).coerceAtLeast(0)

        Bitmap.createBitmap(
            original,
            xOffset,
            0,
            newWidth,
            height
        )
    } else {
        // terlalu tinggi / portrait, crop atas bawah
        val newHeight = (width / targetAspectRatio).toInt().coerceAtMost(height)
        val yOffset = ((height - newHeight) / 2).coerceAtLeast(0)

        Bitmap.createBitmap(
            original,
            0,
            yOffset,
            width,
            newHeight
        )
    }
}

private fun openTscPortWithRetry(
    tsc: TSCActivity,
    mac: String,
    retryCount: Int = 1
) {
    var lastError: Throwable? = null

    repeat(retryCount + 1) { attempt ->
        try {
            Log.d("TSC_SDK", "Opening port attempt ${attempt + 1}")

            tsc.openport(mac)
            Thread.sleep(700)

            // Test command. Kalau OutputStream null, dia akan crash di sini.
            tsc.sendcommand("\r\n")

            Log.d("TSC_SDK", "Printer port ready")
            return

        } catch (e: Throwable) {
            lastError = e
            Log.e("TSC_SDK", "Open port attempt ${attempt + 1} failed", e)

            try {
                tsc.closeport(1000)
            } catch (_: Throwable) {
            }

            Thread.sleep(1000)
        }
    }

    throw IllegalStateException(
        "Printer tidak bersedia / Bluetooth belum connect betul",
        lastError
    )
}

private fun safeTscCommand(
    tsc: TSCActivity,
    command: String
) {
    try {
        tsc.sendcommand(command)
    } catch (e: NullPointerException) {
        throw IllegalStateException(
            "Sambungan printer terputus. Sila hidupkan printer / reconnect Bluetooth dan cuba semula.",
            e
        )
    }
}