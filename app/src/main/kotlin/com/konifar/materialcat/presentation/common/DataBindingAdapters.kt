package com.konifar.materialcat.presentation

import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.text.TextUtils
import android.widget.ImageView
import com.konifar.materialcat.R
import com.konifar.materialcat.presentation.common.customview.ColorPalleteView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

@BindingAdapter("photoImageUrl")
fun setPhotoImageUrl(imageView: ImageView, imageUrl: String?) {
    setImageUrl(imageView, imageUrl, R.color.grey200)
}

private fun setImageUrl(imageView: ImageView, imageUrl: String?, @DrawableRes placeholderResId: Int) {
    if (TextUtils.isEmpty(imageUrl)) {
        imageView.setImageDrawable(ContextCompat.getDrawable(imageView.context, placeholderResId))
    } else {
        Picasso.with(imageView.context)
                .load(imageUrl)
                .placeholder(placeholderResId)
                .error(placeholderResId)
                .into(imageView)
    }
}

@BindingAdapter("swipeRefreshing")
fun setSwipeRefeshing(swipeRefreshLayout: SwipeRefreshLayout, isRefreshing: Boolean) {
    swipeRefreshLayout.isRefreshing = isRefreshing
}

@BindingAdapter("colorPaletteImageUrl")
fun setColorPaletteImageUrl(colorPalleteView: ColorPalleteView, imageUrl: String?) {
    if (TextUtils.isEmpty(imageUrl)) {
        return
    }

    Picasso.with(colorPalleteView.context)
            .load(imageUrl)
            .placeholder(R.color.theme50)
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                    colorPalleteView.initColors(bitmap)
                }

                override fun onBitmapFailed(errorDrawable: Drawable) {}
                override fun onPrepareLoad(placeHolderDrawable: Drawable) {}
            })
}