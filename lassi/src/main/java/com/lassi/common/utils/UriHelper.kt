package com.lassi.common.utils

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore

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
}