package com.lassi.presentation.docs

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import androidx.lifecycle.ViewModelProviders
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
        ViewModelProviders.of(requireActivity())[SelectedMediaViewModel::class.java]
    }

    override fun buildViewModel(): DocsViewModel {
        return ViewModelProviders.of(
            requireActivity(),
            DocsViewModelFactory(requireContext())
        )[DocsViewModel::class.java]
    }

    override fun getContentResource() = R.layout.fragment_media_picker

    override fun initViews() {
        super.initViews()
        setImageAdapter()
        progressBar.indeterminateDrawable.setColorFilter(
            mediaPickerConfig.progressBarColor,
            PorterDuff.Mode.MULTIPLY
        )
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
}
