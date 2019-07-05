package com.lassi.presentation.mediadirectory


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.lassi.R
import com.lassi.common.extenstions.hide
import com.lassi.common.extenstions.show
import com.lassi.common.utils.ColorUtils
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.Logger
import com.lassi.data.common.Response
import com.lassi.data.mediadirectory.Folder
import com.lassi.domain.common.SafeObserver
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.LassiOption
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

    private val folderAdapter by lazy { FolderAdapter(this::onItemClick) }

    override fun getContentResource() = R.layout.fragment_media_picker

    override fun buildViewModel(): FolderViewModel {
        return ViewModelProviders.of(
            requireActivity(), FolderViewModelFactory(requireActivity())
        )[FolderViewModel::class.java]
    }

    override fun initViews() {
        super.initViews()
        rvMedia.layoutManager = GridLayoutManager(context, LassiConfig.getConfig().gridSize)
        rvMedia.adapter = folderAdapter
        rvMedia.addItemDecoration(GridSpacingItemDecoration(LassiConfig.getConfig().gridSize, 10))
        progressBar.indeterminateDrawable.setColorFilter(
            ColorUtils.getColor(
                requireContext(),
                LassiConfig.getConfig().progressBarColor
            ),
            PorterDuff.Mode.MULTIPLY
        )
        checkPermission()
    }

    override fun initLiveDataObservers() {
        super.initLiveDataObservers()
        viewModel.fetchMediaFolderLiveData.observe(
            viewLifecycleOwner,
            SafeObserver(this::handleFetchedFolders)
        )
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext()
                    , Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    requireContext()
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                this.requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    , KeyUtils.REQUEST_PERMISSIONS_REQUEST_CODE
                )
                return
            }
        }
        fetchFolders()
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == KeyUtils.REQUEST_PERMISSIONS_REQUEST_CODE
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            fetchFolders()
        } else {
            showPermissionDisableAlert()
        }
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
            startActivityForResult(intent, KeyUtils.SETTINGS_REQUEST_CODE)
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
            (LassiConfig.getConfig().lassiOption == LassiOption.CAMERA_AND_GALLERY)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Logger.d("FolderFragment", "onResume")
        if (requestCode == KeyUtils.SETTINGS_REQUEST_CODE) {
            checkPermission()
        } else {
            activity?.supportFragmentManager?.popBackStack()
        }
    }

}
