package com.phicdy.mycuration.articlelist

import com.phicdy.mycuration.entity.Article
import java.util.ArrayList

class ArticleListPresenter {

    private lateinit var view: ArticleListView

    private var allArticles: ArrayList<Article> = arrayListOf()
    private var loadedPosition = -1


    fun setView(view: ArticleListView) {
        this.view = view
    }

    fun articleSize(): Int {
        return if (loadedPosition == allArticles.size - 1) allArticles.size else loadedPosition + 2
        // Index starts with 0 and add +1 for footer, so add 2
    }

}
