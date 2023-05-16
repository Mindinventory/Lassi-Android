package com.lassi.presentation.mediadirectory.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lassi.R
import com.lassi.common.extenstions.hide
import com.lassi.common.extenstions.loadImage
import com.lassi.common.extenstions.show
import com.lassi.common.extenstions.toBinding
import com.lassi.data.media.MiItemMedia
import com.lassi.databinding.ItemMediaBinding

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
        return FolderViewHolder(parent.toBinding())
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

    inner class FolderViewHolder(val binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(bucket: MiItemMedia) {
            with(bucket) {
                binding.apply {
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
}