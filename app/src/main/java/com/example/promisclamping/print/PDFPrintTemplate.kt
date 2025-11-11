package com.example.promisclamping.print

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.math.roundToInt

// Copy an asset PDF to cache and return its Uri
suspend fun copyAssetToCacheUri(context: Context, assetName: String): Uri =
    withContext(Dispatchers.IO) {
        val outFile = File(context.cacheDir, assetName)
        context.assets.open(assetName).use { inp ->
            FileOutputStream(outFile).use { out -> inp.copyTo(out) }
        }
        Uri.fromFile(outFile)
    }

// If the Uri is content://, copy to a temp file for PdfRenderer
suspend fun uriToLocalFile(context: Context, uri: Uri): File = withContext(Dispatchers.IO) {
    if (uri.scheme == "file") return@withContext File(uri.path!!)
    val out = File.createTempFile("pdf_", ".pdf", context.cacheDir)
    context.contentResolver.openInputStream(uri)!!.use { inp ->
        FileOutputStream(out).use { outp -> inp.copyTo(outp) }
    }
    out
}

// Main entry: render each PDF page, send as TSPL BITMAP to printer over BT
suspend fun printPdfUriOverBluetooth(context: Context, mac: String, pdfUri: Uri) =
    withContext(Dispatchers.IO) {
        val file = uriToLocalFile(context, pdfUri)
        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        PdfRenderer(pfd).use { renderer ->
            // Open BT socket (classic SPP)
            val socket = openBtSocket(mac)
            socket.use { s ->
                val out = s.outputStream
                for (i in 0 until renderer.pageCount) {
                    renderer.openPage(i).use { page ->
                        // Render page to bitmap at a reasonable scale
                        val bmp = renderPageToWidthDots(page, 576)
                        sendBitmapAsTspl(out, bmp)

                    }
                }
                out.flush()
            }
        }
    }

// Open SPP socket
private fun openBtSocket(mac: String): BluetoothSocket {
    val adapter = BluetoothAdapter.getDefaultAdapter()
    val device: BluetoothDevice = adapter.getRemoteDevice(mac)
    // Standard SerialPortService ID
    val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    val socket = device.createRfcommSocketToServiceRecord(uuid)
//    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
    socket.connect()
    return socket
}

// Render a PDF page to a bitmap whose width == targetDotsW (<=576)
private fun renderPageToWidthDots(page: PdfRenderer.Page, targetDotsW: Int): Bitmap {
    // scale maintaining aspect
    val scale = targetDotsW.toFloat() / page.width.toFloat()
    val w = targetDotsW
    val h = (page.height * scale).roundToInt()
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    bmp.eraseColor(Color.WHITE)
    val canvas = Canvas(bmp)
    val m = Matrix().apply { setScale(scale, scale) }
    canvas.drawColor(Color.WHITE)
    page.render(bmp, null, m, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
    return bmp
}

// Floyd–Steinberg dither to 1-bit and send TSPL BITMAP
private fun sendBitmapAsTspl(out: OutputStream, src: Bitmap) {
    // Ensure width multiple of 8 (BITMAP expects bytes per row)
    val widthDots = if (src.width % 8 == 0) src.width else ((src.width / 8 + 1) * 8)
    val heightDots = src.height

    val padded = if (widthDots != src.width) {
        Bitmap.createBitmap(widthDots, heightDots, Bitmap.Config.ARGB_8888).also { pad ->
            val c = Canvas(pad)
            c.drawColor(Color.WHITE)
            c.drawBitmap(src, 0f, 0f, null)
        }
    } else src

    // 1-bit dither
    val mono = ditherToMono(padded)

    // Header (mm from dots @203dpi)
    val heightMm = heightDots * 25.4 / 203.0
    val header = buildString {
        appendLine("SIZE 72 mm,${"%.2f".format(heightMm)} mm")
        appendLine("GAP 2 mm,0")
        appendLine("DIRECTION 0")
        appendLine("SPEED 4")
        appendLine("DENSITY 11")
        appendLine("CLS")
    }.crlf()
    out.write(header)

    // Write in bands to avoid buffer overflow
    val bytesPerRow = widthDots / 8
    val maxBandRows = 200  // safe band height; tune 100–400
    var y0 = 0
    while (y0 < heightDots) {
        val bandRows = minOf(maxBandRows, heightDots - y0)

        // Pack just this band
        val bandPacked = packMonoBandToBytes(mono, widthDots, heightDots, y0, bandRows)

        // TSPL: BITMAP x,y,bytes_per_row,rows,mode,<data>
        val cmd = "BITMAP 0,$y0,$bytesPerRow,$bandRows,1,".crlf()
        out.write(cmd)
        out.write(bandPacked)
        out.write("\r\n".toByteArray(Charsets.US_ASCII))

        y0 += bandRows
    }

    out.write("PRINT 1,1\r\n".toByteArray(Charsets.US_ASCII))
    out.flush()
}


// Convert to monochrome with simple FS dithering
// 1-bit Floyd–Steinberg dither: returns BooleanArray (true=black)
private fun ditherToMono(src: Bitmap): BooleanArray {
    val w = src.width
    val h = src.height
    val out = BooleanArray(w * h)
    val err = DoubleArray((w + 1) * (h + 1)) // simple error buffer

    for (y in 0 until h) {
        for (x in 0 until w) {
            val px = src.getPixel(x, y)
            val r = (px shr 16) and 0xff
            val g = (px shr 8) and 0xff
            val b = px and 0xff
            val gray = 0.299 * r + 0.587 * g + 0.114 * b

            val idxE = y * (w + 1) + x
            val adjusted = gray + err[idxE]
            val black = adjusted < 128.0
            out[y * w + x] = black
            val e = adjusted - if (black) 0.0 else 255.0

            // distribute error (bounds-safe because err has +1 padding each row)
            err[idxE + 1] += e * 7 / 16.0          // right
            err[idxE + (w + 1)] += e * 5 / 16.0    // below
            err[idxE + (w + 1) + 1] += e * 1 / 16.0// below-right
            if (x > 0) err[idxE + (w + 1) - 1] += e * 3 / 16.0 // below-left
        }
    }
    return out
}

// Packs a band (rows y0..y0+rows-1) into bytes (MSB first), 1=black
private fun packMonoBandToBytes(
    mono: BooleanArray,
    width: Int,
    height: Int,
    y0: Int,
    rows: Int
): ByteArray {
    require(y0 >= 0 && rows > 0 && y0 + rows <= height)
    val bytesPerRow = width / 8
    val out = ByteArray(bytesPerRow * rows)
    var oi = 0
    var i = y0 * width
    for (y in 0 until rows) {
        var bit = 7
        var b = 0
        for (x in 0 until width) {
            if (mono[i++]) b = b or (1 shl bit) // black = 1
            bit--
            if (bit < 0) {
                out[oi++] = b.toByte()
                bit = 7; b = 0
            }
        }
    }
    return out
}


// Overloaded pack with known width/height
private fun packMonoToBytes(mono: BooleanArray, width: Int, height: Int): ByteArray {
    val bytesPerRow = width / 8
    val out = ByteArray(bytesPerRow * height)
    var oi = 0
    var i = 0
    for (y in 0 until height) {
        var bit = 7
        var b = 0
        for (x in 0 until width) {
            if (mono[i++]) b = b or (1 shl bit)
            bit--
            if (bit < 0) {
                out[oi++] = b.toByte()
                bit = 7; b = 0
            }
        }
    }
    return out
}

// Small extension helpers
private fun String.crlf(): ByteArray =
    (this.lines().joinToString("\r\n") + "\r\n").toByteArray(Charsets.US_ASCII)

private inline fun throwIf(cond: Boolean, msg: () -> String) {
    if (cond) throw IllegalArgumentException(msg())
}
