package com.lassi.presentation.media

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.lassi.R
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.KeyUtils.ASCENDING_ORDER
import com.lassi.common.utils.KeyUtils.DESCENDING_ORDER
import com.lassi.common.utils.KeyUtils.SELECTED_FOLDER
import com.lassi.common.utils.Logger
import com.lassi.data.common.Response
import com.lassi.data.common.StartVideoContract
import com.lassi.data.media.MiItemMedia
import com.lassi.data.media.MiMedia
import com.lassi.databinding.FragmentMediaPickerBinding
import com.lassi.domain.common.SafeObserver
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.common.LassiBaseViewModelFragment
import com.lassi.presentation.common.decoration.GridSpacingItemDecoration
import com.lassi.presentation.cropper.CropImageContract
import com.lassi.presentation.cropper.CropImageContractOptions
import com.lassi.presentation.cropper.CropImageOptions
import com.lassi.presentation.cropper.CropImageView
import com.lassi.presentation.media.adapter.MediaAdapter
import com.lassi.presentation.mediadirectory.FolderViewModel
import com.lassi.presentation.mediadirectory.FolderViewModelFactory
import com.lassi.presentation.mediadirectory.SelectedMediaViewModelFactory
import java.io.File

class MediaFragment :
    LassiBaseViewModelFragment<SelectedMediaViewModel, FragmentMediaPickerBinding>(),
    CropImageView.OnSetImageUriCompleteListener, CropImageView.OnCropImageCompleteListener {
    private val mediaAdapter by lazy { MediaAdapter(this::onItemClick) }
    private var bucket: MiItemMedia? = null
    private var mediaPickerConfig = LassiConfig.getConfig()
    private var uri: Uri? = null
    private var menu: Menu? = null

    companion object {
        fun getInstance(bucket: MiItemMedia): MediaFragment {
            val miMediaPickerFragment = MediaFragment()
            val args = Bundle().apply {
                putParcelable(SELECTED_FOLDER, bucket)
            }
            miMediaPickerFragment.arguments = args
            return miMediaPickerFragment
        }
    }

    private val folderViewModel by lazy {
        ViewModelProvider(
            this, FolderViewModelFactory(requireContext())
        )[FolderViewModel::class.java]
    }
    private val startMediaContract = registerForActivityResult(StartVideoContract()) { miMedia ->
        if (LassiConfig.isSingleMediaSelection()) {
            miMedia?.let { setResultOk(arrayListOf(it)) }
        } else {
            LassiConfig.getConfig().selectedMedias.add(miMedia!!)
            viewModel.addSelectedMedia(miMedia)
            folderViewModel.checkInsert()
            if (LassiConfig.getConfig().lassiOption == LassiOption.CAMERA_AND_GALLERY || LassiConfig.getConfig().lassiOption == LassiOption.GALLERY) {
                setResultOk(arrayListOf(miMedia))
                parentFragmentManager.popBackStack()
            }
        }
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { miMedia ->
        if (LassiConfig.isSingleMediaSelection()) {
            miMedia?.let { setResultOk(arrayListOf(it)) }
        } else {
            LassiConfig.getConfig().selectedMedias.add(miMedia!!)
            viewModel.addSelectedMedia(miMedia)
            folderViewModel.checkInsert()
            if (LassiConfig.getConfig().lassiOption == LassiOption.CAMERA_AND_GALLERY || LassiConfig.getConfig().lassiOption == LassiOption.GALLERY) {
                setResultOk(arrayListOf(miMedia))
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun inflateLayout(layoutInflater: LayoutInflater): FragmentMediaPickerBinding {
        return FragmentMediaPickerBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()
        bucket?.let {
            it.bucketName?.let { bucketName ->
                when (viewModel.currentSortingOption.value) {
                    ASCENDING_ORDER -> {
                        viewModel.getSortedDataFromDb(
                            bucket = bucketName,
                            isAsc = ASCENDING_ORDER,
                            mediaType = LassiConfig.getConfig().mediaType
                        )
                    }

                    DESCENDING_ORDER -> {
                        viewModel.getSortedDataFromDb(
                            bucket = bucketName,
                            isAsc = DESCENDING_ORDER,
                            mediaType = LassiConfig.getConfig().mediaType
                        )
                    }

                    else -> {  /* Default Ascending */
                        viewModel.getSelectedMediaData(bucket = bucketName)
                    }
                }
            }
        }
        binding.progressBar.indeterminateDrawable.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                mediaPickerConfig.progressBarColor, BlendModeCompat.SRC_ATOP
            )
        binding.rvMedia.apply {
            setBackgroundColor(LassiConfig.getConfig().galleryBackgroundColor)
            addItemDecoration(GridSpacingItemDecoration(mediaPickerConfig.gridSize, 10))
            layoutManager = GridLayoutManager(context, mediaPickerConfig.gridSize)
            adapter = mediaAdapter
            addItemDecoration(GridSpacingItemDecoration(mediaPickerConfig.gridSize, 10))
        }
    }

    override fun onStop() {
        super.onStop()
        if (LassiConfig.getConfig().maxCount == 1 && LassiConfig.getConfig().selectedMedias.isNotEmpty()) {
            LassiConfig.getConfig().selectedMedias.clear()
        }
    }

    override fun getBundle() {
        super.getBundle()
        arguments?.let {
            bucket = it.getParcelable(SELECTED_FOLDER)
        }
    }

    private fun croppingOptions(
        uri: Uri? = null, includeCamera: Boolean? = false, includeGallery: Boolean? = false
    ) {
        // Start picker to get image for cropping and then use the image in cropping activity.
        cropImage.launch(includeCamera?.let { includeCamera ->
            includeGallery?.let { includeGallery ->
                LassiConfig.getConfig().cropAspectRatio?.x?.let { x ->
                    LassiConfig.getConfig().cropAspectRatio?.y?.let { y ->
                        CropImageOptions(
                            imageSourceIncludeCamera = includeCamera,
                            imageSourceIncludeGallery = includeGallery,
                            cropShape = LassiConfig.getConfig().cropType,
                            showCropOverlay = true,
                            guidelines = CropImageView.Guidelines.ON,
                            multiTouchEnabled = false,
                            aspectRatioX = x,
                            aspectRatioY = y,
                            fixAspectRatio = LassiConfig.getConfig().enableActualCircleCrop
                        )
                    }
                }
            }
        }?.let {
            CropImageContractOptions(
                uri = uri,
                cropImageOptions = it,
            )
        })
    }

    override fun buildViewModel(): SelectedMediaViewModel {
        return ViewModelProvider(
            requireActivity(), SelectedMediaViewModelFactory(requireActivity())
        )[SelectedMediaViewModel::class.java]
    }

    override fun initLiveDataObservers() {
        super.initLiveDataObservers()

        viewModel.fetchedMediaLiveData.observe(
            viewLifecycleOwner, SafeObserver(::handleFetchedData)
        )
    }

    private fun handleFetchedData(response: Response<java.util.ArrayList<MiMedia>>?) {
        when (response) {
            is Response.Success -> {
                Logger.d("mediaFragment", "handleFetchedData SUCCESS size -> ${response.item.size}")
                mediaAdapter.setList(response.item)
            }

            is Response.Error -> {
                Logger.d("mediaFragment", "!@# handleFetchedData ERROR")
            }

            else -> {}
        }
    }

    private fun onItemClick(selectedMedias: ArrayList<MiMedia>) {
        when (LassiConfig.getConfig().mediaType) {
            MediaType.IMAGE -> {
                if (LassiConfig.getConfig().maxCount == 1 && LassiConfig.getConfig().isCrop) {
                    uri = Uri.fromFile(selectedMedias[0].path?.let { File(it) })
                    uri?.let {
                        croppingOptions(uri = uri)
                    }
                } else if (LassiConfig.getConfig().maxCount > 1) {
                    viewModel.addAllSelectedMedia(selectedMedias)
                } else {
                    viewModel.addAllSelectedMedia(selectedMedias)
                    setResultOk(selectedMedias)
                }
            }

            MediaType.VIDEO, MediaType.AUDIO, MediaType.DOC -> {
                if (LassiConfig.getConfig().maxCount > 1) {
                    viewModel.addAllSelectedMedia(selectedMedias)
                } else {
                    startMediaContract.launch(selectedMedias[0].path!!)
                }
            }

            else -> {}
        }
    }

    private fun setResultOk(selectedMedia: ArrayList<MiMedia>?) {
        val intent = Intent().apply {
            putExtra(KeyUtils.SELECTED_MEDIA, selectedMedia)
        }
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        this.menu = menu
        menu.findItem(R.id.menuSort)?.isVisible = true
        val item = menu.findItem(R.id.menuCamera)
        if (item != null) item.isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuSort -> handleSorting()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleSorting() {
        val customDialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.sorting_option, null)
        val sortingRadioGroup = customDialogView.findViewById<RadioGroup>(R.id.sortingRadioGroup)

        //To set the previously selected option as checked.
        sortingRadioGroup.check(
            when (viewModel.currentSortingOption.value) {
                ASCENDING_ORDER -> R.id.radioAscending
                DESCENDING_ORDER -> R.id.radioDescending
                else -> R.id.radioAscending
            }
        )

        //Set up the alert builder with the custom layout..
        AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.sort_by_date)
            setView(customDialogView)
            setPositiveButton(android.R.string.ok) { _, _ ->
                val checkedRadioButtonId = sortingRadioGroup.checkedRadioButtonId
                val selectedOption =
                    if (checkedRadioButtonId == R.id.radioAscending) ASCENDING_ORDER else DESCENDING_ORDER

                viewModel.currentSortingOptionUpdater(selectedOption)

                when (selectedOption) {
                    ASCENDING_ORDER -> {
                        // Handle ascending sorting
                        bucket?.let {
                            it.bucketName?.let { bucketName ->
                                viewModel.getSortedDataFromDb(
                                    bucket = bucketName,
                                    isAsc = ASCENDING_ORDER,
                                    mediaType = LassiConfig.getConfig().mediaType
                                )
                            }
                        }
                    }

                    DESCENDING_ORDER -> {
                        // Handle descending sorting
                        bucket?.let {
                            it.bucketName?.let { bucketName ->
                                viewModel.getSortedDataFromDb(
                                    bucket = bucketName,
                                    isAsc = DESCENDING_ORDER,
                                    mediaType = LassiConfig.getConfig().mediaType
                                )
                            }
                        }
                    }
                }
            }
            setNegativeButton(android.R.string.cancel, null)
            create().show()
        }
    }

    override fun hasOptionMenu(): Boolean = true

    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
        if (error != null) {
            Toast.makeText(activity, "Image load failed: " + error.message, Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        if (result.error != null) {
            Toast.makeText(activity, "Crop failed: ${result.error.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
