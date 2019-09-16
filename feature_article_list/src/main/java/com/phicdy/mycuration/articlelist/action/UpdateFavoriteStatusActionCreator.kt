package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.ActionCreator2
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.FavoriteRepository

class UpdateFavoriteStatusActionCreator(
        private val dispatcher: Dispatcher,
        private val favoriteRepository: FavoriteRepository
) : ActionCreator2<Int, List<ArticleItem>> {

    override suspend fun run(position: Int, currentList: List<ArticleItem>) {
        val item = currentList[position]
        if (item is ArticleItem.Content) {
            val l = currentList.toMutableList()
            val a = item.value.copy(isFavorite = !item.value.isFavorite)
            if (a.isFavorite) {
                favoriteRepository.store(a.id)
            } else {
                favoriteRepository.delete(a.id)
            }
            l.removeAt(position)
            l.add(position, ArticleItem.Content(a))
            dispatcher.dispatch(UpdateFavoriteAction(l))
        }
    }
}