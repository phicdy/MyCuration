package com.phicdy.mycuration.articlelist

import com.phicdy.mycuration.entity.Article
import java.util.ArrayList

class ArticleListPresenter {

    companion object {
        const val DEFAULT_CURATION_ID = -1

        const val VIEW_TYPE_ARTICLE = 0
        const val VIEW_TYPE_FOOTER = 1
    }

    private lateinit var view: ArticleListView

    private var allArticles: ArrayList<Article> = arrayListOf()
    private var loadedPosition = -1


    fun setView(view: ArticleListView) {
        this.view = view
    }

    fun onListItemLongClicked(position: Int) {
        if (position < 0) return
        val article = allArticles[position]
        view.showShareUi(article.url)
    }

    fun articleSize(): Int {
        return if (loadedPosition == allArticles.size - 1) allArticles.size else loadedPosition + 2
        // Index starts with 0 and add +1 for footer, so add 2
    }

}
