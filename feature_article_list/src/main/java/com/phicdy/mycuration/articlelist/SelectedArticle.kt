package com.phicdy.mycuration.articlelist

import android.os.Bundle

/**
 * Article shown in the detail pane of the two-pane article list.
 */
data class SelectedArticle(
    val id: Int,
    val title: String,
    val url: String
) {

    fun toBundle(): Bundle = Bundle().apply {
        putInt(KEY_ID, id)
        putString(KEY_TITLE, title)
        putString(KEY_URL, url)
    }

    companion object {
        private const val KEY_ID = "KEY_SELECTED_ARTICLE_ID"
        private const val KEY_TITLE = "KEY_SELECTED_ARTICLE_TITLE"
        private const val KEY_URL = "KEY_SELECTED_ARTICLE_URL"

        fun fromBundle(bundle: Bundle?): SelectedArticle? {
            if (bundle == null || !bundle.containsKey(KEY_URL)) return null
            val url = bundle.getString(KEY_URL) ?: return null
            return SelectedArticle(
                id = bundle.getInt(KEY_ID),
                title = bundle.getString(KEY_TITLE).orEmpty(),
                url = url
            )
        }
    }
}
