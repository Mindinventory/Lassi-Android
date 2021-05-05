package com.lassi.presentation.docs

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
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
import com.lassi.domain.common.SafeObserver
import com.lassi.domain.media.LassiConfig
import com.lassi.presentation.common.LassiBaseViewModelFragment
import com.lassi.presentation.common.decoration.GridSpacingItemDecoration
import com.lassi.presentation.media.SelectedMediaViewModel
import com.lassi.presentation.media.adapter.MediaAdapter
import kotlinx.android.synthetic.main.fragment_media_picker.*

class DocsFragment : LassiBaseViewModelFragment<DocsViewModel>() {
    private val mediaAdapter by lazy { MediaAdapter(this::onItemClick) }
    private var mediaPickerConfig = LassiConfig.getConfig()
    private val selectedMediaViewModel by lazy {
        ViewModelProvider(requireActivity())[SelectedMediaViewModel::class.java]
    }

    private val permissionSettingResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            requestPermission()
        }

    private val requestPermission =
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

    override fun getContentResource() = R.layout.fragment_media_picker

    override fun initViews() {
        super.initViews()
        setImageAdapter()
        progressBar.indeterminateDrawable.colorFilter =
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
                } else {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.addCategory("android.intent.category.DEFAULT")
                        intent.data = Uri.parse(
                            String.format("package:%s", context?.applicationContext?.packageName)
                        )
                        permissionSettingResult.launch(intent)
                    } catch (e: Exception) {
                        val intent = Intent()
                        intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                        permissionSettingResult.launch(intent)
                    }
                }
            }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                requestPermission.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                )
            }
            else -> {
                requestPermission.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            }
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
                progressBar.hide()
                mediaAdapter.setList(response.item)
            }
            is Response.Loading -> progressBar.show()
            is Response.Error -> progressBar.hide()
        }
    }

    private fun setImageAdapter() {
        rvMedia.layoutManager = GridLayoutManager(context, mediaPickerConfig.gridSize)
        rvMedia.adapter = mediaAdapter
        rvMedia.addItemDecoration(GridSpacingItemDecoration(mediaPickerConfig.gridSize, 10))
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
        if (item != null)
            item.isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private fun setResultOk(selectedMedia: ArrayList<MiMedia>?) {
        val intent = Intent().apply {
            putExtra(KeyUtils.SELECTED_MEDIA, selectedMedia)
        }
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
    }

    private fun showPermissionDisableAlert() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setMessage(R.string.storage_permission_rational)
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton(R.string.ok) { _, _ ->
            val intent = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", activity?.packageName, null)
            }
            permissionSettingResult.launch(intent)
        }
        alertDialog.setNegativeButton(R.string.cancel) { _, _ ->
            activity?.onBackPressed()
        }
        val permissionDialog = alertDialog.create()
        permissionDialog.setCancelable(false)
        permissionDialog.show()
    }
}
