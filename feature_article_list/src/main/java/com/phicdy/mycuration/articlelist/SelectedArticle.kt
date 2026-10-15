package com.phicdy.mycuration.articlelist

import android.os.Parcel
import android.os.Parcelable
import com.phicdy.mycuration.entity.FavoritableArticle

/**
 * Article shown in the detail pane of the two-pane article list. Parcelable so that the
 * selection survives configuration changes.
 */
data class SelectedArticle(
    val id: Int,
    val title: String,
    val url: String,
) : Parcelable {

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(title)
        dest.writeString(url)
    }

    companion object CREATOR : Parcelable.Creator<SelectedArticle> {
        override fun createFromParcel(source: Parcel): SelectedArticle = SelectedArticle(
            id = source.readInt(),
            title = source.readString().orEmpty(),
            url = source.readString().orEmpty(),
        )

        override fun newArray(size: Int): Array<SelectedArticle?> = arrayOfNulls(size)
    }
}

fun FavoritableArticle.toSelectedArticle(): SelectedArticle = SelectedArticle(id = id, title = title, url = url)
