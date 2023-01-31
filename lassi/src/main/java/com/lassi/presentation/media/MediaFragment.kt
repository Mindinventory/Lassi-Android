package com.lassi.presentation.media

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.lassi.R
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.KeyUtils.SELECTED_FOLDER
import com.lassi.common.utils.Logger
import com.lassi.data.common.Response
import com.lassi.data.media.MiItemMedia
import com.lassi.data.media.MiMedia
import com.lassi.domain.common.SafeObserver
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.MediaType
import com.lassi.presentation.common.LassiBaseViewModelFragment
import com.lassi.presentation.common.decoration.GridSpacingItemDecoration
import com.lassi.presentation.cropper.CropImageContract
import com.lassi.presentation.cropper.CropImageContractOptions
import com.lassi.presentation.cropper.CropImageOptions
import com.lassi.presentation.cropper.CropImageView
import com.lassi.presentation.media.adapter.MediaAdapter
import com.lassi.presentation.mediadirectory.SelectedMediaViewModelFactory
import com.lassi.presentation.videopreview.VideoPreviewActivity
import kotlinx.android.synthetic.main.fragment_media_picker.*
import java.io.File

class MediaFragment : LassiBaseViewModelFragment<SelectedMediaViewModel>(),
    CropImageView.OnSetImageUriCompleteListener, CropImageView.OnCropImageCompleteListener {
    private val mediaAdapter by lazy { MediaAdapter(this::onItemClick) }
    private var bucket: MiItemMedia? = null
    private var mediaPickerConfig = LassiConfig.getConfig()
    private var uri: Uri? = null

    override fun getContentResource() = R.layout.fragment_media_picker

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

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Use the returned uri.
            val uriContent = result.uriContent
            val uriFilePath = result.getUriFilePath(requireContext()) // optional usage
        } else {
            // An error occurred.
            val exception = result.error
        }
    }

    override fun initViews() {
        super.initViews()
        rvMedia.setBackgroundColor(LassiConfig.getConfig().galleryBackgroundColor)
        bucket?.let {
            it.bucketName?.let { bucketName ->
                viewModel.getSelectedMediaData(bucket = bucketName)
            }
        }
        progressBar.indeterminateDrawable.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                mediaPickerConfig.progressBarColor,
                BlendModeCompat.SRC_ATOP
            )
    }

    override fun getBundle() {
        super.getBundle()
        arguments?.let {
            bucket = it.getParcelable(SELECTED_FOLDER)
        }
    }

    private fun croppingOptions(
        uri: Uri? = null,
        includeCamera: Boolean? = false,
        includeGallery: Boolean? = false
    ) {
        // Start picker to get image for cropping and then use the image in cropping activity.
        cropImage.launch(
            includeCamera?.let { includeCamera ->
                includeGallery?.let { includeGallery ->
                    CropImageOptions(
                        imageSourceIncludeCamera = includeCamera,
                        imageSourceIncludeGallery = includeGallery,
                        cropShape = CropImageView.CropShape.RECTANGLE,
                        showCropOverlay = true,
                        guidelines = CropImageView.Guidelines.ON,
                        multiTouchEnabled = false,

                        )
                }
            }?.let {
                CropImageContractOptions(
                    uri = uri,
                    cropImageOptions = it,
                )
            }
        )
    }

    override fun buildViewModel(): SelectedMediaViewModel {
        return ViewModelProvider(
            requireActivity(),
            SelectedMediaViewModelFactory(requireActivity())
        )[SelectedMediaViewModel::class.java]
    }

    override fun initLiveDataObservers() {
        super.initLiveDataObservers()

        viewModel.fetchedMediaLiveData.observe(
            viewLifecycleOwner,
            SafeObserver(::handleFetchedData)
        )
    }

    private fun handleFetchedData(response: Response<java.util.ArrayList<MiMedia>>?) {
        rvMedia.layoutManager = GridLayoutManager(context, mediaPickerConfig.gridSize)
        rvMedia.adapter = mediaAdapter
        rvMedia.addItemDecoration(GridSpacingItemDecoration(mediaPickerConfig.gridSize, 10))

        when (response) {
            is Response.Success -> {
                Logger.d("mediaFragment", "handleFetchedData SUCCESS size -> ${response.item.size}")
                mediaAdapter.setList(response.item)
            }
            is Response.Error -> {
                Logger.d("mediaFragment", "handleFetchedData ERROR")
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
                    VideoPreviewActivity.startVideoPreview(
                        activity,
                        selectedMedias[0].path!!
                    )
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
        val item = menu.findItem(R.id.menuCamera)
        if (item != null)
            item.isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
        if (error != null) {
            Toast.makeText(activity, "Image load failed: " + error.message, Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        if (result.error == null) {
        } else {
            Toast
                .makeText(activity, "Crop failed: ${result.error?.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
