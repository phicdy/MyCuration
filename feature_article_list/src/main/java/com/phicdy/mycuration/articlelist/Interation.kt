package com.phicdy.mycuration.articlelist

sealed class Interation {
    data class Scroll(val positionAfterScroll: Int) : Interation()
    data class OpenInternalWebBrowser(val url: String) : Interation()
    data class OpenExternalWebBrowser(val url: String) : Interation()
    data class Share(val url: String) : Interation()
    data class ReadArticle(val position: Int) : Interation()
    data class SwipeArtilce(val position: Int) : Interation()
    object ReadAllOfArticles : Interation()
    object Finish : Interation()
}
