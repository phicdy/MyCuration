package com.phicdy.mycuration.presentation.view

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet

class ArticleRecyclerView(context: Context, attrs: AttributeSet?) : androidx.recyclerview.widget.RecyclerView(context, attrs) {

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}