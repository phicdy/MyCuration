package com.phicdy.mycuration.domain.entity


import android.os.Parcel
import android.os.Parcelable

data class Feed(
        val id: Int = DEFAULT_FEED_ID,
        var title: String = "",
        var url: String = "",
        var iconPath: String = DEDAULT_ICON_PATH,
        val format: String = "",
        var unreadAriticlesCount: Int = 0,
        var siteUrl: String = ""
) : Parcelable {

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(title)
        dest.writeString(url)
        dest.writeString(iconPath)
        dest.writeString(siteUrl)
        dest.writeInt(unreadAriticlesCount)
    }

    companion object {

        const val TABLE_NAME = "feeds"
        const val TITLE = "title"
        const val ID = "_id"
        const val URL = "url"
        const val FORMAT = "format"
        const val SITE_URL = "siteUrl"
        const val ICON_PATH = "iconPath"
        const val UNREAD_ARTICLE = "unreadArticle"

        const val DEDAULT_ICON_PATH = "defaultIconPath"
        const val ALL_FEED_ID = -1
        const val DEFAULT_FEED_ID = -100

        const val RSS_1 = "RSS1.0"
        const val RSS_2 = "RSS2.0"
        const val ATOM = "ATOM"

        const val CREATE_TABLE_SQL = "create table " + TABLE_NAME + "(" +
                ID + " integer primary key autoincrement," +
                TITLE + " text," +
                URL + " text," +
                FORMAT + " text," +
                SITE_URL + " text," +
                ICON_PATH + " text," +
                UNREAD_ARTICLE + " integer)"

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<Feed> = object : Parcelable.Creator<Feed> {
            override fun createFromParcel(parcel: Parcel): Feed {
                return Feed(
                        id = parcel.readInt(),
                        title = parcel.readString() ?: "",
                        url = parcel.readString() ?: "",
                        iconPath = parcel.readString() ?: "",
                        siteUrl = parcel.readString() ?: "",
                        unreadAriticlesCount = parcel.readInt()
                )
            }

            override fun newArray(size: Int): Array<Feed?> {
                return arrayOfNulls(size)
            }
        }
    }
}
