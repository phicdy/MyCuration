package com.phicdy.mycuration.articlelist.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat

fun bitmapFrom(context: Context, vectorDrawableId: Int): Bitmap? {
    val drawable = ContextCompat.getDrawable(context, vectorDrawableId)
    drawable?.let {
        val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }
    return null
}

