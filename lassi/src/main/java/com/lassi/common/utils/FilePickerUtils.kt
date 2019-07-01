package com.lassi.common.utils

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import java.io.File

object FilePickerUtils {

    fun getFileExtension(file: File): String {
        val name = file.name
        try {
            return name.substring(name.lastIndexOf(".") + 1)
        } catch (e: Exception) {
            return ""
        }

    }

    fun contains(types: Array<String>, path: String): Boolean {
        for (string in types) {
            if (path.toLowerCase().endsWith(string)) return true
        }
        return false
    }

    fun <T> contains2(array: Array<T>, v: T?): Boolean {
        if (v == null) {
            for (e in array)
                if (e == null)
                    return true
        } else {
            for (e in array)
                if (e == v || v == e)
                    return true
        }

        return false
    }

    fun notifyMediaStore(context: Context, path: String?) {
        if (path != null && !TextUtils.isEmpty(path)) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val f = File(path)
            val contentUri = Uri.fromFile(f)
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        }
    }

    fun createDirIfNotExists(path: String): Boolean {
        val file = File(Environment.getExternalStorageDirectory(), path)
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return false //Problem creating MiMedia folder
            }
        }
        return true
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
