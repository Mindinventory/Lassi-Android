package com.lassi.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.lassi.app.adapter.SelectedMediaAdapter
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.builder.Lassi
import com.lassi.presentation.common.decoration.GridSpacingItemDecoration
import com.lassi.presentation.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener, Lassi.SelectedMediaCallback {

    private val selectedMediaAdapter by lazy { SelectedMediaAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnImagePicker.setOnClickListener(this)
        btnVideoPicker.setOnClickListener(this)
        rvSelectedMedia.adapter = selectedMediaAdapter
        rvSelectedMedia.addItemDecoration(GridSpacingItemDecoration(2, 10))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnImagePicker -> {
                val intent = Lassi(this)
                    .with(LassiOption.CAMERA_AND_GALLERY)
                    .setMaxCount(5)
                    .setGridSize(2)
                    .setPlaceHolder(R.drawable.ic_image_placeholder)
                    .setErrorDrawable(R.drawable.ic_image_placeholder)
                    .setStatusBarColor(R.color.colorPrimaryDark)
                    .setToolbarColor(R.color.colorPrimary)
                    .setToolbarResourceColor(android.R.color.white)
                    .setProgressBarColor(R.color.colorAccent)
                    .setCropType(CropImageView.CropShape.RECTANGLE)
                    .setCropAspectRatio(1, 1)
                    .setSupportedFileTypes("jpg", "jpeg", "png", "webp", "gif")
                    .build()
                startActivity(intent)
            }
            R.id.btnVideoPicker -> {
                val intent = Lassi(this)
                    .with(LassiOption.CAMERA_AND_GALLERY)
                    .setMaxCount(5)
                    .setGridSize(3)
                    .setMinTime(15)
                    .setMaxTime(30)
                    .setMediaType(MediaType.VIDEO)
                    .setErrorDrawable(R.drawable.ic_image_placeholder)
                    .setStatusBarColor(R.color.colorPrimaryDark)
                    .setToolbarColor(R.color.colorPrimary)
                    .setToolbarResourceColor(android.R.color.white)
                    .setProgressBarColor(R.color.colorAccent)
                    .setPlaceHolder(R.drawable.ic_image_placeholder)
                    .setErrorDrawable(R.drawable.ic_image_placeholder)
                    .setSupportedFileTypes("mp4", "mkv", "webm", "avi", "flv", "3gp")
                    .build()
                startActivity(intent)
            }
        }
    }

    override fun onMediaSelected(selectedMedia: ArrayList<MiMedia>?) {
        selectedMediaAdapter.setList(selectedMedia)
    }
}
