package com.phicdy.mycuration.curatedarticlelist

import android.content.Context
import android.util.AttributeSet

class CuratedArticleRecyclerView(context: Context, attrs: AttributeSet?) : androidx.recyclerview.widget.RecyclerView(context, attrs) {

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}