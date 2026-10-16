package com.phicdy.mycuration.articlelist

import android.os.Parcelable
import com.phicdy.mycuration.entity.FavoritableArticle
import kotlinx.parcelize.Parcelize

/**
 * Article shown in the detail pane of the two-pane article list. Parcelable so that the
 * selection survives configuration changes.
 */
@Parcelize
data class SelectedArticle(
    val id: Int,
    val title: String,
    val url: String,
) : Parcelable

fun FavoritableArticle.toSelectedArticle(): SelectedArticle = SelectedArticle(id = id, title = title, url = url)
