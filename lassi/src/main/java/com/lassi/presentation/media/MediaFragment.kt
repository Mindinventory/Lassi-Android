package com.lassi.presentation.media

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.lassi.R
import com.lassi.common.utils.CropUtils
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
import com.lassi.presentation.media.adapter.MediaAdapter
import com.lassi.presentation.mediadirectory.SelectedMediaViewModelFactory
import com.lassi.presentation.videopreview.VideoPreviewActivity
import kotlinx.android.synthetic.main.fragment_media_picker.*
import java.io.File

class MediaFragment : LassiBaseViewModelFragment<SelectedMediaViewModel>() {
    private val mediaAdapter by lazy { MediaAdapter(this::onItemClick) }
    private var bucket: MiItemMedia? = null
    private var mediaPickerConfig = LassiConfig.getConfig()
    private var menu: Menu? = null


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

    override fun initViews() {
        super.initViews()
        rvMedia.setBackgroundColor(LassiConfig.getConfig().galleryBackgroundColor)
        rvMedia.addItemDecoration(GridSpacingItemDecoration(mediaPickerConfig.gridSize, 10))
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
                    val uri = Uri.fromFile(selectedMedias[0].path?.let { File(it) })
                    CropUtils.beginCrop(requireActivity(), uri)
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
        this.menu = menu
        menu.findItem(R.id.menuSort)?.isVisible = true
        val item = menu.findItem(R.id.menuCamera)
        if (item != null)
            item.isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuSort -> handleSorting()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleSorting() {
        // setup the alert builder
        AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.sort_by_date)
            setItems(R.array.sorting_options) { _, isAsc ->
                when (isAsc) {
                    0 -> { /* Ascending */
                        bucket?.let {
                            it.bucketName?.let { bucketName ->
                                viewModel.getSortedDataFromDb(
                                    bucket = bucketName,
                                    isAsc = 0,
                                    mediaType = LassiConfig.getConfig().mediaType
                                )
                            }
                        }
                    }
                    1 -> { /* Descending */
                        bucket?.let {
                            it.bucketName?.let { bucketName ->
                                viewModel.getSortedDataFromDb(
                                    bucket = bucketName,
                                    isAsc = 1,
                                    mediaType = LassiConfig.getConfig().mediaType
                                )
                            }
                        }
                    }
                }
            }
            create().show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
}
