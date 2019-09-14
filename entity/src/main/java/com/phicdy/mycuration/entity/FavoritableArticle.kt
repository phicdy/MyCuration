package com.phicdy.mycuration.entity

data class FavoritableArticle(
        val id: Int,
        var title: String,
        var url: String,
        var status: String,
        val point: String,
        var postedDate: Long,
        val feedId: Int,
        val feedTitle: String,
        val feedIconPath: String,
        val isFavorite: Boolean
)
