package com.phicdy.mycuration.articlelist

sealed class Interation {
    data class Scroll(val positionAfterScroll: Int): Interation()
}
