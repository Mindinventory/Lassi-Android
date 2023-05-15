package com.lassi.presentation.media.adapter

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.lassi.common.extenstions.loadImage
import com.lassi.common.extenstions.toBinding
import com.lassi.common.utils.DurationUtils.getDuration
import com.lassi.common.utils.ImageUtils
import com.lassi.common.utils.Logger
import com.lassi.data.media.MiMedia
import com.lassi.databinding.ItemMediaBinding
import com.lassi.domain.media.LassiConfig

class MediaAdapter(
    private val onItemClick: (selectedMedias: ArrayList<MiMedia>) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MyViewHolder>() {
    private val logTag = MediaAdapter::class.java.simpleName
    private val images = ArrayList<MiMedia>()

    fun setList(images: ArrayList<MiMedia>?) {
        if (images != null) {
            this.images.clear()
            this.images.addAll(images)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(parent.toBinding())
    }

    override fun getItemCount() = images.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(images[position])
    }

    private fun addSelected(image: MiMedia, position: Int) {
        with(LassiConfig.getConfig()) {
            if (selectedMedias.size != maxCount) {
                selectedMedias.add(image)
                notifyItemChanged(position)
            }
        }
    }

    fun removeSelected(image: MiMedia, position: Int) {
        if (LassiConfig.getConfig().selectedMedias.remove(image)) {
            Logger.d(logTag, "removeSelected ${image.path}")
            notifyItemChanged(position)
        } else {
            Logger.d(logTag, "not removeSelected ${image.path}")
        }
    }

    inner class MyViewHolder(private val binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(miMedia: MiMedia) {
            binding.apply {
                with(miMedia) {
                    var isSelect = isSelected(this)
                    tvFolderName.text = miMedia.name
                    viewAlpha.alpha = if (isSelect) 0.5f else 0.0f
                    ivSelect.setImageResource(LassiConfig.getConfig().selectionDrawable)
                    ivSelect.isVisible = isSelect
                    ivFolderThumbnail.loadImage(ImageUtils.getThumb(this))
                    if (duration != 0L) {
                        tvDuration.visibility = View.VISIBLE
                        tvDuration.text = getDuration(duration)
                    }

                    root.setOnClickListener {
                        if (LassiConfig.getConfig().maxCount > 1) {
                            isSelect = !isSelect
                            if (!isSelect) {
                                removeSelected(miMedia, absoluteAdapterPosition)
                            } else {
                                addSelected(miMedia, absoluteAdapterPosition)
                            }
                        } else {
                            with(LassiConfig.getConfig()) {
                                if (selectedMedias.size != maxCount) {
                                    selectedMedias.add(0, miMedia)
                                } else {
                                    selectedMedias[0] = miMedia
                                }
                            }
                        }
                        onItemClick(LassiConfig.getConfig().selectedMedias)
                    }
                }
            }
        }

        private fun isSelected(image: MiMedia): Boolean {
            for (selectedImage in LassiConfig.getConfig().selectedMedias) {
                if (selectedImage.path.equals(image.path)) {
                    return true
                }
            }
            return false
        }
    }
}