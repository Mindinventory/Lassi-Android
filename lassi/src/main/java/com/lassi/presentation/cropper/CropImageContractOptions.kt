package com.lassi.presentation.cropper

import android.net.Uri

data class CropImageContractOptions(
  val uri: Uri?,
  val cropImageOptions: CropImageOptions,
)
