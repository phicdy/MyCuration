package com.phicdy.mycuration.articlelist

import android.content.Context
import android.util.AttributeSet

class ArticleRecyclerView(context: Context, attrs: AttributeSet?) : androidx.recyclerview.widget.RecyclerView(context, attrs) {

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}