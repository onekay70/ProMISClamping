package com.example.promisclamping

import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response

data class UploadResult(
    val ok: Boolean,
    val imageId: String? = null,
    val message: String? = null
)

/** Try to extract {success, imageId, message} from either JSON or plain text. */
fun parseUploadResponse(resp: Response<ResponseBody>): UploadResult {
    if (!resp.isSuccessful) {
        // Try JSON error first
        val err = resp.errorBody()?.string().orEmpty()
        // Common Spring error: {"timestamp":..., "status": 500, "error":"...", "message":"Upload Failed!", "path": "..."}
        val msg = extractMessageFromTextOrJson(err).ifBlank { "Upload failed (${resp.code()})" }
        return UploadResult(ok = false, message = msg)
    }

    val bodyText = resp.body()?.string().orEmpty()
    if (bodyText.isBlank()) {
        return UploadResult(ok = false, message = "Empty response from server.")
    }

    // Try JSON success shapes first
    extractFromKnownJson(bodyText)?.let { return it }

    // Fallback: plain text such as "Upload Successful: IMG123" or "Upload Failed!"
    if (bodyText.contains("Failed", ignoreCase = true)) {
        return UploadResult(ok = false, message = bodyText)
    }
    // Try to pull an ID-ish token from text
    val id = Regex("""([A-Za-z]{2,}\d{2,}|[0-9a-fA-F-]{8,})""").find(bodyText)?.value
    return UploadResult(ok = id != null, imageId = id, message = if (id == null) bodyText else null)
}

private fun extractMessageFromTextOrJson(text: String): String {
    // JSON?
    runCatching {
        val o = JSONObject(text)
        // common keys: message, error, detail
        val keys = listOf("message", "error", "detail")
        for (k in keys) if (o.has(k)) return o.optString(k)
    }
    // Not JSON: return as-is (plain text)
    return text
}

private fun extractFromKnownJson(bodyText: String): UploadResult? = runCatching {
    val o = JSONObject(bodyText)

    // Shape A: { success: true, imageId: "IMG123", message?: string }
    if (o.has("success") || o.has("imageId")) {
        val ok = o.optBoolean("success", o.has("imageId"))
        val id = o.optString("imageId", null)
        val msg = o.optString("message", null)
        return@runCatching UploadResult(ok = ok, imageId = id, message = msg)
    }

    // Shape B: { data: { id: "IMG123" }, message?: string }
    if (o.has("data")) {
        val data = o.optJSONObject("data")
        val id = data?.optString("id", null) ?: data?.optString("imageId", null)
        val msg = o.optString("message", null)
        return@runCatching UploadResult(ok = id != null, imageId = id, message = msg)
    }

    null
}.getOrNull()