package com.example.promisclamping.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

fun String.asTextPart(): RequestBody =
    this.toRequestBody("text/plain".toMediaTypeOrNull())

data class FilePart(val part: MultipartBody.Part, val fileName: String)

fun Uri.toFilePart(
    resolver: ContentResolver,
    partName: String = "file"
): FilePart? =
    runCatching {
        val name = resolver.query(this, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
        } ?: "upload"

        val mime = resolver.getType(this)
            ?: MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(name.substringAfterLast('.', ""))
            ?: "application/octet-stream"

        resolver.openInputStream(this)?.use { input ->
            val tmp = File.createTempFile("upload_", "_$name")
            FileOutputStream(tmp).use { out -> input.copyTo(out) }
            val body = tmp.asRequestBody(mime.toMediaTypeOrNull())
            FilePart(
                part = MultipartBody.Part.createFormData(partName, name, body),
                fileName = name
            )
        }
    }.getOrNull()
