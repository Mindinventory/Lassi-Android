package com.lassi.presentation.mediadirectory


import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import com.lassi.data.common.Response
import com.lassi.data.mediadirectory.Folder
import com.lassi.domain.common.SafeObserver
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.common.LassiBaseViewModelFragment
import com.lassi.presentation.common.decoration.GridSpacingItemDecoration
import com.lassi.presentation.media.MediaFragment
import com.lassi.presentation.mediadirectory.adapter.FolderAdapter
import kotlinx.android.synthetic.main.fragment_media_picker.*

class FolderFragment : LassiBaseViewModelFragment<FolderViewModel>() {
    companion object {
        fun newInstance(): FolderFragment {
            return FolderFragment()
        }
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
                fetchFolders()
            } else {
                showPermissionDisableAlert()
            }
        }

    private val folderAdapter by lazy { FolderAdapter(this::onItemClick) }

    override fun getContentResource() = R.layout.fragment_media_picker

    override fun buildViewModel(): FolderViewModel {
        return ViewModelProvider(
            requireActivity(), FolderViewModelFactory(requireActivity())
        )[FolderViewModel::class.java]
    }

    override fun initViews() {
        super.initViews()
        rvMedia.layoutManager = GridLayoutManager(context, LassiConfig.getConfig().gridSize)
        rvMedia.adapter = folderAdapter
        rvMedia.addItemDecoration(GridSpacingItemDecoration(LassiConfig.getConfig().gridSize, 10))
        progressBar.indeterminateDrawable.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                LassiConfig.getConfig().progressBarColor,
                BlendModeCompat.SRC_ATOP
            )
        requestPermission()
    }

    override fun initLiveDataObservers() {
        super.initLiveDataObservers()
        viewModel.fetchMediaFolderLiveData.observe(
            viewLifecycleOwner,
            SafeObserver(this::handleFetchedFolders)
        )
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        } else {
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun fetchFolders() {
        viewModel.fetchFolders()
    }

    private fun handleFetchedFolders(response: Response<ArrayList<Folder>>) {
        when (response) {
            is Response.Success -> {
                progressBar.hide()
                folderAdapter.setList(response.item)
            }
            is Response.Loading -> progressBar.show()
            is Response.Error -> progressBar.hide()
        }
    }

    private fun onItemClick(folder: Folder) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.setCustomAnimations(
                R.anim.right_in,
                R.anim.right_out,
                R.anim.right_in,
                R.anim.right_out
            )
            ?.add(R.id.ftContainer, MediaFragment.getInstance(folder))
            ?.addToBackStack(MediaFragment::class.java.simpleName)
            ?.commitAllowingStateLoss()
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
            permissionSettingResult.launch(intent)
        }
        alertDialog.setNegativeButton(R.string.cancel) { _, _ ->
            activity?.onBackPressed()
        }
        val permissionDialog = alertDialog.create()
        permissionDialog.setCancelable(false)
        permissionDialog.show()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.menuCamera)?.isVisible =
            if (LassiConfig.getConfig().mediaType == MediaType.IMAGE
                || LassiConfig.getConfig().mediaType == MediaType.VIDEO
            ) {
                (LassiConfig.getConfig().lassiOption == LassiOption.CAMERA_AND_GALLERY)
            } else {
                false
            }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
}
