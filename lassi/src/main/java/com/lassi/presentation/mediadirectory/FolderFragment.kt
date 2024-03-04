package com.lassi.presentation.mediadirectory


import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.lassi.R
import com.lassi.common.extenstions.hide
import com.lassi.common.extenstions.safeObserve
import com.lassi.common.extenstions.show
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.ToastUtils
import com.lassi.data.common.Response
import com.lassi.data.media.MiItemMedia
import com.lassi.data.media.MiMedia
import com.lassi.databinding.FragmentMediaPickerBinding
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.domain.media.MultiLangConfig
import com.lassi.presentation.common.LassiBaseViewModelFragment
import com.lassi.presentation.common.decoration.GridSpacingItemDecoration
import com.lassi.presentation.media.MediaFragment
import com.lassi.presentation.mediadirectory.adapter.FolderAdapter
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FolderFragment : LassiBaseViewModelFragment<FolderViewModel, FragmentMediaPickerBinding>() {

    companion object {
        fun newInstance(): FolderFragment {
            return FolderFragment()
        }
    }

    var needsStorage = true

    private val photoPermission = mutableListOf(
        Manifest.permission.READ_MEDIA_IMAGES
    )

    private val photoPermissionAnd14 = mutableListOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    )

    private val vidPermission = mutableListOf(
        Manifest.permission.READ_MEDIA_VIDEO
    )

    private val vidPermissionAnd14 = mutableListOf(
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    )

    private val audioPermission = mutableListOf(
        Manifest.permission.READ_MEDIA_AUDIO
    )

    private val permissionSettingResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            requestPermission()
        }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (map.entries.all { it.value }) {
                viewModel.checkInsert()
            } else {
                showPermissionDisableAlert()
            }
        }

    /**
     * This is the approach to store picked media (Video for now) in the DB and rest of the flow
     * would remain folder wise as same as it was before
     */
    private val requestPhotoPickerPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            // Specifically check for android.permission.READ_MEDIA_VISUAL_USER_SELECTED permission for Android 14 case
            if (map[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true) {
                viewModel.addPhotoPickerDataInDatabase()
            } else {
                 showPermissionDisableAlert()
            }
        }

    /**
     * Video picker in case of Android 14 will be handled here
     * Need to modifty for handling multp
     */
    private val mediaPickerLauncher =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            if (uris.isNotEmpty()) {
                val mediaPaths = ArrayList(uris.mapNotNull { uri ->
                    MiMedia(path = context?.let { getMediaPathFromURI(it, uri) })
                })
                Log.d("PhotoPicker", "!@# PHOTO-PICKER:: Media paths: $mediaPaths")
                setResultOk(mediaPaths)
            } else {
                Log.d("PhotoPicker", "!@# PHOTO-PICKER:: No media selected")
            }
        }

    private fun setResultOk(selectedMedia: ArrayList<MiMedia>?) {
        if (selectedMedia?.size!! > LassiConfig.getConfig().maxCount) {
            ToastUtils.showToast(
                requireContext(), LassiConfig.getConfig().customLimitExceedingErrorMessage
            )
            activity?.finish()
        } else {
            val intent = Intent().apply {
                putExtra(KeyUtils.SELECTED_MEDIA, selectedMedia)
            }
            activity?.setResult(Activity.RESULT_OK, intent)
            activity?.finish()
        }
    }

    private val folderAdapter by lazy { FolderAdapter(this::onItemClick) }

    override fun buildViewModel(): FolderViewModel {
        return ViewModelProvider(
            requireActivity(), FolderViewModelFactory(requireActivity())
        )[FolderViewModel::class.java]
    }

    override fun inflateLayout(layoutInflater: LayoutInflater): FragmentMediaPickerBinding {
        return FragmentMediaPickerBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()
        binding.rvMedia.apply {
            setBackgroundColor(LassiConfig.getConfig().galleryBackgroundColor)
            layoutManager = GridLayoutManager(context, LassiConfig.getConfig().gridSize)
            adapter = folderAdapter
            addItemDecoration(GridSpacingItemDecoration(LassiConfig.getConfig().gridSize, 10))
        }
        binding.progressBar.indeterminateDrawable.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                LassiConfig.getConfig().progressBarColor, BlendModeCompat.SRC_ATOP
            )
        requestPermission()
    }

    override fun initLiveDataObservers() {
        super.initLiveDataObservers()
        viewModel.fetchMediaFolderLiveData.safeObserve(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {
                    binding.tvNoDataFound.visibility = View.GONE
                    binding.progressBar.show()
                }

                is Response.Success -> {
                    Log.d("FolderFragment", "!@# initLiveDataObservers: ${response.item.size}")
                }

                is Response.Error -> {
                    binding.progressBar.hide()
                    response.throwable.printStackTrace()
                }
            }
        }

        viewModel.getMediaItemList().observe(viewLifecycleOwner) {
            binding.progressBar.hide()
            if (!it.isNullOrEmpty()) {
                folderAdapter.setList(it)
            }
        }

        viewModel.emptyList.observe(viewLifecycleOwner) {
            binding.tvNoDataFound.visibility = if (it) View.VISIBLE else View.GONE
            binding.tvNoDataFound.text = MultiLangConfig.getConfig().noDataFound
        }

        viewModel.fileRemovalCheck.observe(viewLifecycleOwner) { isTrue ->
            if (isTrue) {
                folderAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (LassiConfig.getConfig().mediaType == MediaType.IMAGE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    ) != PackageManager.PERMISSION_GRANTED
                    requestPhotoPickerPermission.launch(photoPermissionAnd14.toTypedArray())
                } else {
                    needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                    requestPermission.launch(photoPermission.toTypedArray())
                }
            } else if (LassiConfig.getConfig().mediaType == MediaType.VIDEO) {
                Log.d("TAG", "!@# PHOTO-PICKER:: mediaType == MediaType.VIDEO")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    ) != PackageManager.PERMISSION_GRANTED
                    requestPhotoPickerPermission.launch(vidPermissionAnd14.toTypedArray())
                } else {
                    needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.READ_MEDIA_VIDEO
                    ) != PackageManager.PERMISSION_GRANTED
                    requestPermission.launch(vidPermission.toTypedArray())
                }
            }  else if (LassiConfig.getConfig().mediaType == MediaType.PHOTO_PICKER) {
                Log.d("TAG", "!@# PHOTO-PICKER:: mediaType == MediaType.PHOTOPICKER")
                needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) != PackageManager.PERMISSION_GRANTED
                Log.d("TAG", "!@# PHOTO-PICKER:: PickVisualMedia.VideoOnly")
                mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
            } else {
                if (LassiConfig.getConfig().mediaType == MediaType.AUDIO) {
                    needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.READ_MEDIA_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                    requestPermission.launch(audioPermission.toTypedArray())
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (LassiConfig.getConfig().mediaType == MediaType.PHOTO_PICKER) {
                needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) != PackageManager.PERMISSION_GRANTED
                Log.d("TAG", "!@# PHOTO-PICKER:: PickVisualMedia.VideoOnly")
                mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
            } else {
                requestPermission.launch(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                )
            }
        } else {
            if (LassiConfig.getConfig().mediaType == MediaType.PHOTO_PICKER) {
                needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) != PackageManager.PERMISSION_GRANTED
                Log.d("TAG", "!@# PHOTO-PICKER:: PickVisualMedia.VideoOnly")
                mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
            } else {
                requestPermission.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            }
        }
    }

    private fun getMediaPathFromURI(context: Context, uri: Uri): String? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val fileName = getFileName(context.contentResolver, uri)
        inputStream?.use { input ->
            val outputFile =
                File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), fileName)
            FileOutputStream(outputFile).use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
                return outputFile.absolutePath
            }
        }
        return null
    }

    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String {
        var fileName = "temp_media"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                fileName = cursor.getString(displayNameIndex)
            }
        }
        return fileName
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkRemoval()
    }

    private fun onItemClick(bucket: MiItemMedia) {
        activity?.supportFragmentManager?.beginTransaction()?.setCustomAnimations(
            R.anim.right_in, R.anim.right_out, R.anim.right_in, R.anim.right_out
        )?.add(R.id.ftContainer, MediaFragment.getInstance(bucket))
            ?.addToBackStack(MediaFragment::class.java.simpleName)?.commitAllowingStateLoss()
    }

    private fun showPermissionDisableAlert() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            showPermissionAlert(msg = MultiLangConfig.getConfig().storagePermissionRational)
        } else {
            if (LassiConfig.getConfig().mediaType == MediaType.IMAGE) {
                showPermissionAlert(msg = MultiLangConfig.getConfig().readMediaImagesVideoPermissionRational)
            } else if (LassiConfig.getConfig().mediaType == MediaType.VIDEO) {
                showPermissionAlert(msg = MultiLangConfig.getConfig().readMediaImagesVideoPermissionRational)
            } else {
                if (LassiConfig.getConfig().mediaType == MediaType.AUDIO) {
                    showPermissionAlert(msg = MultiLangConfig.getConfig().readMediaAudioPermissionRational)
                }
            }
        }
    }

    private fun showPermissionAlert(msg: String) {
        val alertDialog = AlertDialog.Builder(requireContext(), R.style.dialogTheme)
        alertDialog.setMessage(msg)
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton(MultiLangConfig.getConfig().ok) { _, _ ->
            val intent = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", activity?.packageName, null)
            }
            permissionSettingResult.launch(intent)
        }

        alertDialog.setNegativeButton(MultiLangConfig.getConfig().cancel) { _, _ ->
            activity?.onBackPressed()
        }
        val permissionDialog = alertDialog.create()
        permissionDialog.setCancelable(false)
        permissionDialog.show()
        with(LassiConfig.getConfig()) {
            permissionDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(alertDialogNegativeButtonColor)
            permissionDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(alertDialogPositiveButtonColor)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.menuCamera)?.isVisible =
            if (LassiConfig.getConfig().mediaType == MediaType.IMAGE
                || LassiConfig.getConfig().mediaType == MediaType.VIDEO
            ) {
                (LassiConfig.getConfig().lassiOption == LassiOption.CAMERA_AND_GALLERY
                        || LassiConfig.getConfig().lassiOption == LassiOption.CAMERA)
            } else {
                false
            }
        menu.findItem(R.id.menuSort)?.isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    override fun hasOptionMenu(): Boolean = true
}

