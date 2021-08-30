package com.phicdy.mycuration.entity


data class Curation(
        val id: Int,
        val name: String
) {

    companion object {
        const val TABLE_NAME = "curations"
        const val NAME = "name"
        const val ID = "_id"
    }
}
