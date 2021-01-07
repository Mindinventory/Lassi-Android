package com.lassi.presentation.media

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.lassi.R
import com.lassi.common.utils.CropUtils
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.KeyUtils.SELECTED_FOLDER
import com.lassi.data.media.MiMedia
import com.lassi.data.mediadirectory.Folder
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.MediaType
import com.lassi.presentation.common.LassiBaseViewModelFragment
import com.lassi.presentation.common.decoration.GridSpacingItemDecoration
import com.lassi.presentation.media.adapter.MediaAdapter
import com.lassi.presentation.videopreview.VideoPreviewActivity
import kotlinx.android.synthetic.main.fragment_media_picker.*
import java.io.File

class MediaFragment : LassiBaseViewModelFragment<SelectedMediaViewModel>() {
    private val mediaAdapter by lazy { MediaAdapter(this::onItemClick) }
    private var folder: Folder? = null
    private var mediaPickerConfig = LassiConfig.getConfig()

    override fun getContentResource() = R.layout.fragment_media_picker

    companion object {
        fun getInstance(folder: Folder): MediaFragment {
            val miMediaPickerFragment = MediaFragment()
            val args = Bundle().apply {
                putParcelable(SELECTED_FOLDER, folder)
            }
            miMediaPickerFragment.arguments = args
            return miMediaPickerFragment
        }
    }

    override fun initViews() {
        super.initViews()
        setImageAdapter()
        progressBar.indeterminateDrawable.setColorFilter(
            mediaPickerConfig.progressBarColor,
            PorterDuff.Mode.MULTIPLY
        )
    }

    override fun getBundle() {
        super.getBundle()
        arguments?.let {
            folder = it.getParcelable(SELECTED_FOLDER)
        }
    }

    override fun buildViewModel(): SelectedMediaViewModel {
        return ViewModelProviders.of(requireActivity())[SelectedMediaViewModel::class.java]
    }

    private fun setImageAdapter() {
        rvMedia.layoutManager = GridLayoutManager(context, mediaPickerConfig.gridSize)
        rvMedia.adapter = mediaAdapter
        rvMedia.addItemDecoration(GridSpacingItemDecoration(mediaPickerConfig.gridSize, 10))
        mediaAdapter.setList(folder?.medias)
    }

    private fun onItemClick(selectedMedias: ArrayList<MiMedia>) {
        when (LassiConfig.getConfig().mediaType) {
            MediaType.IMAGE -> {
                /*viewModel.addAllSelectedMedia(selectedMedias)
                setResultOk(selectedMedias)*/
                if (LassiConfig.getConfig().maxCount == 1 && LassiConfig.isCrop()) {
                    val uri = Uri.fromFile(File(selectedMedias[0].path))
                    CropUtils.beginCrop(requireActivity(), uri)
                } else if (LassiConfig.getConfig().maxCount > 1) {
                    viewModel.addAllSelectedMedia(selectedMedias)

                } else {
                    viewModel.addAllSelectedMedia(selectedMedias)
                    setResultOk(selectedMedias)
                }

                /* if (LassiConfig.getConfig().maxCount > 1 && LassiConfig.isCrop()) {
                     viewModel.addAllSelectedMedia(selectedMedias)
                     setResultOk(selectedMedias)
                 } else {
                     val uri = Uri.fromFile(File(selectedMedias[0].path))
                     CropUtils.beginCrop(requireActivity(), uri)
                 }*/


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
        }


        /*if (LassiConfig.getConfig().maxCount > 1) {
            viewModel.addAllSelectedMedia(selectedMedias)
        } else {
            if (LassiConfig.getConfig().mediaType == MediaType.IMAGE) {
                val uri = Uri.fromFile(File(selectedMedias[0].path))
                CropUtils.beginCrop(requireActivity(), uri)
            } else {
                VideoPreviewActivity.startVideoPreview(
                    activity,
                    selectedMedias[0].path!!
                )
            }
        }*/
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
}
