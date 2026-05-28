package com.lassi.presentation.cropper.utils

import android.content.Context
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.core.content.FileProvider
import com.lassi.BuildConfig
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths
import androidx.core.net.toUri

internal fun Context.authority() = "$packageName.cropper.fileprovider"

/**
 * This class exist because of two issues. One is related to the new Scope Storage for OS 10+
 * Where we should not access external storage anymore. Because of this we cannot get an external uri
 *
 * Using FileProvider to retrieve the path can return a value that is not the real one for some devices
 * This happens in specific devices and OSs. Because of this is needed to do a lot of if/else and
 * try/catch to just use the latest cases when needed.
 *
 * This code is not good, but work. I don't suggest anyone to reproduce it.
 *
 * Most of the devices will work fine, but if you worry about memory usage, please remember to clean
 * the cache from time to time,
 */
internal fun getUriForFile(context: Context, file: File): Uri {
  val authority = context.authority()
  try {
    return FileProvider.getUriForFile(context, authority, file)
  } catch (e: Exception) {
    try {
      // Note: Periodically clear this cache
      val cacheFolder = File(context.cacheDir, "CROP_LIB_CACHE")
      val cacheLocation = File(cacheFolder, file.name)
      var input: InputStream? = null
      var output: OutputStream? = null
      try {
        input = FileInputStream(file)
        output = FileOutputStream(cacheLocation) // appending output stream
        input.copyTo(output)
        return FileProvider.getUriForFile(context, authority, cacheLocation)
      } catch (e: Exception) {
        val path = "content://$authority/files/my_images/"

        if (SDK_INT >= 26) {
          Files.createDirectories(Paths.get(path))
        } else {
          val directory = File(path)
          if (!directory.exists()) directory.mkdirs()
        }
        return "$path${file.name}".toUri()
      } finally {
        input?.close()
        output?.close()
      }
    } catch (e: Exception) {

      if (SDK_INT < 29) {
        val cacheDir = context.externalCacheDir
        cacheDir?.let {
          try {
            return Uri.fromFile(File(cacheDir.path, file.absolutePath))
          } catch (e: Exception) {
            Log.e("AIC", "second catch block --------------> ${e.message}")
          }
        }
      }
      // If nothing else work we try
      return Uri.fromFile(file)
    }
  }
}
