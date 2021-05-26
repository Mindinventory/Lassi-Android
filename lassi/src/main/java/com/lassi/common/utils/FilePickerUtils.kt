package com.lassi.common.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import java.util.*

object FilePickerUtils {

    fun contains(types: Array<String>, path: String): Boolean {
        for (string in types) {
            if (path.lowercase(Locale.getDefault()).endsWith(string)) return true
        }
        return false
    }

    fun notifyGalleryUpdateNewFile(
        context: Context,
        filePath: String,
        mimeType: String = "image/*",
        onFileScanComplete: (uri: Uri?, path:String?) -> Unit
    ) {
        context.let {
            MediaScannerConnection.scanFile(
                it,
                arrayOf(filePath),
                arrayOf(mimeType)
            ) { path, uri ->
                Log.d("ExternalStorage", "Scanned $path")
                onFileScanComplete(uri, path)
            }
        }
    }
}
