package com.example.promisclamping.print

import android.bluetooth.BluetoothAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

private val SPP_UUID: UUID =
    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

object LogoManager {

    private var logoUploaded = false

    suspend fun uploadLogoIfNeeded(mac: String, bmpData: ByteArray) =
        withContext(Dispatchers.IO) {

            if (logoUploaded) return@withContext

            val adapter = BluetoothAdapter.getDefaultAdapter()
            val device = adapter.getRemoteDevice(mac)
            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)

            socket.use { s ->
                s.connect()
                val out = s.outputStream

                // Start FLASH download
                out.write("DOWNLOAD F,JATA.BMP,${bmpData.size}\r\n".toByteArray())
                out.flush()
                Thread.sleep(200)

                // Send raw BMP binary
                out.write(bmpData)
                out.flush()
                Thread.sleep(200)

                // End download properly for Alpha-series
                out.write("\r\n".toByteArray())
                out.flush()
                Thread.sleep(200)

                // Force printer to commit
                out.write("FILES\r\n".toByteArray())
                out.flush()
            }

            logoUploaded = true
        }

    suspend fun uploadLogoAlpha(mac: String, bmpData: ByteArray) =
        withContext(Dispatchers.IO) {

            val adapter = BluetoothAdapter.getDefaultAdapter()
            val device = adapter.getRemoteDevice(mac)
            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)

            socket.use { s ->
                s.connect()
                val out = s.outputStream

                // Start file download to FLASH
                out.write("DOWNLOAD F,JATA.BMP,${bmpData.size}\r\n".toByteArray())
                out.flush()
                Thread.sleep(150)

                // Send binary in chunks (Alpha printers NEED chunk streaming)
                var offset = 0
                val chunkSize = 1024
                while (offset < bmpData.size) {
                    val end = minOf(offset + chunkSize, bmpData.size)
                    out.write(bmpData.copyOfRange(offset, end))
                    out.flush()
                    offset = end
                    Thread.sleep(20) // give printer buffer time
                }

                // End download
                out.write("\r\n".toByteArray())
                out.flush()
                Thread.sleep(200)
            }
        }

}
