package com.gohahotel.connect.data.remote

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloudinary image/video upload service.
 *
 * FREE plan: 25 GB storage, 25 GB bandwidth/month — no credit card needed.
 *
 * ─── HOW TO SET UP ────────────────────────────────────────────────────────────
 * 1. Go to https://cloudinary.com/users/register/free
 * 2. Sign up (email or Google) — completely free, no card required
 * 3. After login → Dashboard → copy:
 *      Cloud Name  →  replace CLOUD_NAME below
 *      API Key     →  replace API_KEY below
 *      API Secret  →  replace API_SECRET below
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Singleton
class CloudinaryService @Inject constructor(
    private val context: Context
) {
    // ── ⚠️  REPLACE THESE WITH YOUR CLOUDINARY CREDENTIALS ──────────────────
    private val CLOUD_NAME  = "dmp9qmbp7"
    private val API_KEY     = "493174132136472"
    private val API_SECRET  = "FGHNhxPA4k3bhALgwWGCsoU4-w8"
    // ─────────────────────────────────────────────────────────────────────────

    private val client = OkHttpClient()

    /**
     * Upload an image or video from a content URI.
     * Returns the secure HTTPS URL of the uploaded file.
     */
    suspend fun uploadFile(uri: Uri, folder: String): String = withContext(Dispatchers.IO) {
        val timestamp = (System.currentTimeMillis() / 1000).toString()

        // Read bytes from the content URI
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open file stream")
        val bytes = inputStream.use { it.readBytes() }

        // Determine MIME type
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val isVideo  = mimeType.startsWith("video")

        // Build signature: SHA-1 of "folder=X&timestamp=Y" + API_SECRET
        val signatureString = "folder=$folder&timestamp=$timestamp$API_SECRET"
        val signature = sha1(signatureString)

        // Build multipart request
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "upload_${System.currentTimeMillis()}",
                bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            )
            .addFormDataPart("api_key",   API_KEY)
            .addFormDataPart("timestamp", timestamp)
            .addFormDataPart("folder",    folder)
            .addFormDataPart("signature", signature)
            .build()

        val resourceType = if (isVideo) "video" else "image"
        val uploadUrl = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/$resourceType/upload"

        val request = Request.Builder()
            .url(uploadUrl)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
            ?: throw Exception("Empty response from Cloudinary")

        if (!response.isSuccessful) {
            val error = try {
                JSONObject(responseBody).optString("error", responseBody)
            } catch (_: Exception) { responseBody }
            throw Exception("Cloudinary upload failed: $error")
        }

        val json = JSONObject(responseBody)
        json.getString("secure_url")
    }

    private fun sha1(input: String): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val bytes  = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
