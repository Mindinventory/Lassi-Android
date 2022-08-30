package com.lassi.common.extenstions

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.lassi.domain.media.LassiConfig


fun ImageView.loadImage(source: String?) {
    val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
    Glide.with(context)
        .load(source ?: "")
        .error(LassiConfig.getConfig().errorDrawable)
        .placeholder(LassiConfig.getConfig().placeHolder)
        .transition(DrawableTransitionOptions.withCrossFade(factory))
        .into(this)
}