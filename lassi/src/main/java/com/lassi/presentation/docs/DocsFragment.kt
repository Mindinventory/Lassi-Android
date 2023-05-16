package com.lassi.presentation.docs

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.lassi.R
import com.lassi.common.extenstions.hide
import com.lassi.common.extenstions.show
import com.lassi.common.utils.KeyUtils
import com.lassi.data.common.Response
import com.lassi.data.media.MiMedia
import com.lassi.databinding.FragmentMediaPickerBinding
import com.lassi.domain.common.SafeObserver
import com.lassi.domain.media.LassiConfig
import com.lassi.presentation.common.LassiBaseViewModelFragment
import com.lassi.presentation.common.decoration.GridSpacingItemDecoration
import com.lassi.presentation.media.SelectedMediaViewModel
import com.lassi.presentation.media.adapter.MediaAdapter

class DocsFragment : LassiBaseViewModelFragment<DocsViewModel, FragmentMediaPickerBinding>() {
    private val mediaAdapter by lazy { MediaAdapter(this::onItemClick) }
    private var mediaPickerConfig = LassiConfig.getConfig()
    private val selectedMediaViewModel by lazy {
        ViewModelProvider(requireActivity())[SelectedMediaViewModel::class.java]
    }

    private val mDocumentChooser =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                handleFileData(activityResult.data)
            } else {
                activity?.onBackPressed()
            }
        }

    private val mPermissionSettingResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            requestPermission()
        }

    private val mRequestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (map.entries.all {
                    it.value == true
                }) {
                fetchDocs()
            } else {
                showPermissionDisableAlert()
            }
        }

    override fun buildViewModel(): DocsViewModel {
        return ViewModelProvider(
            requireActivity(),
            DocsViewModelFactory(requireContext())
        )[DocsViewModel::class.java]
    }

    override fun inflateLayout(layoutInflater: LayoutInflater): FragmentMediaPickerBinding {
        return FragmentMediaPickerBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()
        setImageAdapter()
        binding.progressBar.indeterminateDrawable.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                mediaPickerConfig.progressBarColor,
                BlendModeCompat.SRC_ATOP
            )
        requestPermission()
    }

    private fun requestPermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (Environment.isExternalStorageManager()) {
                    fetchDocs()
                }
            }

            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                openSystemFileExplorer()
            }

            else -> {
                mRequestPermission.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            }
        }
    }

    /**
     * This code only for the Android 10 OS and only for choose the non media files.
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun openSystemFileExplorer() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        val mimeTypesList = ArrayList<String>()
        LassiConfig.getConfig().supportedFileType.forEach { mimeType ->
            MimeTypeMap
                .getSingleton()
                .getMimeTypeFromExtension(mimeType)?.let {
                    mimeTypesList.add(it)
                }
        }
        var mMimeTypeArray = arrayOfNulls<String>(mimeTypesList.size)
        mMimeTypeArray = mimeTypesList.toArray(mMimeTypeArray)
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mMimeTypeArray)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        mDocumentChooser.launch(intent)
    }

    private fun handleFileData(intent: Intent?) {
        val uris: MutableList<String?> = ArrayList()
        if (intent != null) {
            if (intent.dataString != null) {
                val uri = intent.dataString
                uris.add(uri)
            } else if (intent.clipData != null) {
                val clipData = intent.clipData
                clipData?.let {
                    for (i in 0 until clipData.itemCount) {
                        val item = clipData.getItemAt(i)
                        uris.add(item.uri.toString())
                    }
                }
            }

            if (intent.hasExtra("uris")) {
                val paths = intent.getParcelableArrayListExtra<Uri>("uris")
                paths?.let {
                    for (i in it.indices) {
                        uris.add(paths[i].toString())
                    }
                }
            }

            val files = ArrayList<MiMedia>()
            if (LassiConfig.getConfig().maxCount >= uris.size) {
                for (uri in uris) {
                    files.add(MiMedia(path = uri, doesUri = true))
                }
            } else {
                for (index in 0 until LassiConfig.getConfig().maxCount) {
                    files.add(MiMedia(path = uris[index], doesUri = true))
                }
            }
            val returnIntent = Intent().apply {
                putExtra(KeyUtils.SELECTED_MEDIA, files)
            }
            activity?.setResult(Activity.RESULT_OK, returnIntent)
            activity?.finish()
        }
    }

    private fun fetchDocs() {
        viewModel.fetchDocs()
    }

    override fun initLiveDataObservers() {
        super.initLiveDataObservers()
        viewModel.fetchDocsLiveData.observe(viewLifecycleOwner, SafeObserver(this::handleDocs))
    }

    private fun handleDocs(response: Response<ArrayList<MiMedia>>) {
        when (response) {
            is Response.Success -> {
                binding.progressBar.hide()
                mediaAdapter.setList(response.item)
            }

            is Response.Loading -> binding.progressBar.show()
            is Response.Error -> binding.progressBar.hide()
        }
    }

    private fun setImageAdapter() {
        binding.rvMedia.apply {
            layoutManager = GridLayoutManager(context, mediaPickerConfig.gridSize)
            adapter = mediaAdapter
            addItemDecoration(GridSpacingItemDecoration(mediaPickerConfig.gridSize, 10))
        }
    }

    private fun onItemClick(selectedMedias: ArrayList<MiMedia>) {
        if (LassiConfig.getConfig().maxCount > 1) {
            LassiConfig.getConfig().selectedMedias = selectedMedias
            selectedMediaViewModel.addAllSelectedMedia(selectedMedias)
        } else {
            setResultOk(selectedMedias)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.menuCamera)
        menu.findItem(R.id.menuSort)?.isVisible = false
        if (item != null)
            item.isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    override fun hasOptionMenu(): Boolean = true

    private fun setResultOk(selectedMedia: ArrayList<MiMedia>?) {
        val intent = Intent().apply {
            putExtra(KeyUtils.SELECTED_MEDIA, selectedMedia)
        }
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
    }

    private fun showPermissionDisableAlert() {
        val alertDialog = AlertDialog.Builder(requireContext(), R.style.dialogTheme)
        alertDialog.setMessage(R.string.storage_permission_rational)
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton(R.string.ok) { _, _ ->
            val intent = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", activity?.packageName, null)
            }
            mPermissionSettingResult.launch(intent)
        }
        alertDialog.setNegativeButton(R.string.cancel) { _, _ ->
            activity?.onBackPressed()
        }
        val permissionDialog = alertDialog.create()
        permissionDialog.setCancelable(false)
        permissionDialog.show()
    }
}
