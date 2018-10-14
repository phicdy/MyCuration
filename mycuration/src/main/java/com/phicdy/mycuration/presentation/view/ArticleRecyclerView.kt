package com.phicdy.mycuration.presentation.view

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet

class ArticleRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}