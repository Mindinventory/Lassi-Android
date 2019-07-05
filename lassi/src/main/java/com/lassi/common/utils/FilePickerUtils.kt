package com.lassi.common.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log

object FilePickerUtils {

    fun contains(types: Array<String>, path: String): Boolean {
        for (string in types) {
            if (path.toLowerCase().endsWith(string)) return true
        }
        return false
    }

    fun notifyGalleryUpdateNewFile(
        context: Context,
        filePath: String,
        mimeType: String = "image/jpeg",
        onFileScanComplete: (uri: Uri) -> Unit
    ) {
        context.let {
            MediaScannerConnection.scanFile(
                it,
                arrayOf(filePath),
                arrayOf(mimeType)
            ) { path, uri ->
                Log.d("ExternalStorage", "Scanned $path")
                onFileScanComplete(uri)
            }
        }
    }
}
