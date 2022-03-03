package com.lassi.common.extenstions

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

fun Context.getFileName(uri: Uri): String? {
    val returnCursor: Cursor? = this.contentResolver.query(uri, null, null, null, null)
    val nameIndex: Int? = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    returnCursor?.moveToFirst()
    val name: String? = nameIndex?.let { returnCursor.getString(it) }
    returnCursor?.close()
    return name
}

fun Context.getFileSize(uri: Uri): Long {
    val returnCursor: Cursor? = this.contentResolver.query(uri, null, null, null, null)
    val sizeIndex: Int? = returnCursor?.getColumnIndex(OpenableColumns.SIZE)
    returnCursor?.moveToFirst()
    val size: Long? = sizeIndex?.let { returnCursor.getLong(it) }
    returnCursor?.close()
    return size ?: 0L
}