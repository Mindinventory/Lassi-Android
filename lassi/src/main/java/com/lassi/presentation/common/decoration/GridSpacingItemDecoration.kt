package com.lassi.presentation.common.decoration

import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(
    private val columnCount: Int,
    @Px preferredSpace: Int,
    private val includeEdge: Boolean = true
) : RecyclerView.ItemDecoration() {

    /**
     * In this algorithm space should divide by 3 without remnant or width of
     * items can have a difference and we want them to be exactly the same
     */
    private val space =
        if (preferredSpace % 3 == 0) preferredSpace else (preferredSpace + (3 - preferredSpace % 3))

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (includeEdge) {
            when {
                position % columnCount == 0 -> {
                    outRect.left = space
                    outRect.right = space / 3
                }
                position % columnCount == columnCount - 1 -> {
                    outRect.right = space
                    outRect.left = space / 3
                }
                else -> {
                    outRect.left = space * 2 / 3
                    outRect.right = space * 2 / 3
                }
            }
            if (position < columnCount) {
                outRect.top = space
            }
            outRect.bottom = space
        } else {
            when {
                position % columnCount == 0 -> outRect.right = space * 2 / 3
                position % columnCount == columnCount - 1 -> outRect.left = space * 2 / 3
                else -> {
                    outRect.left = space / 3
                    outRect.right = space / 3
                }
            }
            if (position >= columnCount) {
                outRect.top = space
            }
        }
    }

}