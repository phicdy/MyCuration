package com.phicdy.mycuration.feedurlhook

interface FeedUrlHookView {
    fun showSuccessToast()
    fun showInvalidUrlErrorToast()
    fun showGenericErrorToast()
    fun finishView()
    fun trackFailedUrl(url: String)
}
