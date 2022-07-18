package com.lassi.presentation.mediadirectory.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lassi.R
import com.lassi.common.extenstions.hide
import com.lassi.common.extenstions.inflate
import com.lassi.common.extenstions.loadImage
import com.lassi.common.extenstions.show
import com.lassi.data.media.MiItemMedia
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_media.*

class FolderAdapter(
    private val onItemClick: (bucket: MiItemMedia) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    private var buckets = ArrayList<MiItemMedia>()

    fun setList(buckets: ArrayList<MiItemMedia>?) {
        buckets?.let {
            this.buckets.clear()
            this.buckets.addAll(it)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        return FolderViewHolder(parent.inflate(R.layout.item_media))
    }

    override fun getItemCount() = buckets.size

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(buckets[position])
    }

    fun clear() {
        val size: Int = buckets.size
        buckets.clear()
        notifyItemRangeRemoved(0, size)
    }

    inner class FolderViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bind(bucket: MiItemMedia) {
            with(bucket) {
                tvFolderName.show()
                tvDuration.hide()
                ivFolderThumbnail.loadImage(bucket.latestItemPathForBucket)
                tvFolderName.text = String.format(
                    tvFolderName.context.getString(R.string.directory_with_item_count),
                    bucketName,
                    totalItemSizeForBucket.toString()
                )
                itemView.setOnClickListener {
                    onItemClick(bucket)
                }
            }
        }
    }
}