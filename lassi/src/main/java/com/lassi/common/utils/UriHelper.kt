package com.lassi.common.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object UriHelper {
    private fun getMediaType(uri: Uri, contentResolver: ContentResolver): String? {
        // Columns to retrieve from the media store
        val projection = arrayOf(MediaStore.MediaColumns.MIME_TYPE)

        // Query the content resolver for the media information
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val mimeTypeColumnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                if (mimeTypeColumnIndex >= 0) {
                    return cursor.getString(mimeTypeColumnIndex)
                }
            }
        }
        return null
    }

    fun isVideo(uri: Uri, contentResolver: ContentResolver): Boolean {
        val mimeType = getMediaType(uri, contentResolver)
        return mimeType?.startsWith("video/") == true
    }

    fun isPhoto(uri: Uri, contentResolver: ContentResolver): Boolean {
        val mimeType: String? = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            contentResolver.getType(uri)
        } else {
            // For file:// URIs, manually get the MIME type based on the file extension
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        val t = mimeType?.startsWith("image/") == true
        return t
    }

    fun getCompressFormatForUri(uri: Uri, context: Context): CompressFormat {
        val mimeType = context.contentResolver?.getType(uri)
        return when (mimeType) {
            "image/png" -> CompressFormat.PNG
            "image/jpeg" -> CompressFormat.JPEG
            "image/webp" -> if (Build.VERSION.SDK_INT >= 30) CompressFormat.WEBP_LOSSLESS else CompressFormat.WEBP
            else -> CompressFormat.JPEG
        }
    }
}