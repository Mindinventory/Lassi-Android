package com.lassi.presentation.mediadirectory.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lassi.R
import com.lassi.common.extenstions.hide
import com.lassi.common.extenstions.inflate
import com.lassi.common.extenstions.loadImage
import com.lassi.common.extenstions.show
import com.lassi.common.utils.ImageUtils
import com.lassi.data.mediadirectory.Folder
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_media.*

class FolderAdapter(
    private val onItemClick: (folder: Folder) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    private var folders = ArrayList<Folder>()

    fun setList(folders: ArrayList<Folder>?) {
        folders?.let {
            this.folders.clear()
            this.folders.addAll(it)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        return FolderViewHolder(parent.inflate(R.layout.item_media))
    }

    override fun getItemCount() = folders.size

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(folders[position])
    }

    inner class FolderViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bind(folder: Folder) {
            with(folder) {
                tvFolderName.show()
                tvDuration.hide()
                tvFileSize.hide()
                ivFolderThumbnail.loadImage(ImageUtils.getThumb(medias[0]))
                tvFolderName.text = String.format(
                    tvFolderName.context.getString(R.string.directory_with_item_count),
                    folderName,
                    medias.size.toString()
                )
                itemView.setOnClickListener {
                    onItemClick(folder)
                }
            }
        }
    }
}