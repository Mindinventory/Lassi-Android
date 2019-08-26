package com.lassi.common.utils

internal object FileSizeFormatter {
    val BYTES = "Bytes"
    val MEGABYTES = "MB"
    val KILOBYTES = "kB"
    val GIGABYTES = "GB"
    val KILO: Long = 1024
    val MEGA = KILO * 1024
    val GIGA = MEGA * 1024

    fun formatFileSize(pBytes: Long): String {
        return if (pBytes < KILO) {
            pBytes.toString() + BYTES
        } else if (pBytes < MEGA) {
            (0.5 + pBytes / KILO.toDouble()).toInt().toString() + KILOBYTES
        } else if (pBytes < GIGA) {
            (0.5 + pBytes / MEGA.toDouble()).toInt().toString() + MEGABYTES
        } else {
            (0.5 + pBytes / GIGA.toDouble()).toInt().toString() + GIGABYTES
        }
    }
}

   
    