package com.phicdy.mycuration.presentation.view

interface FeedUrlHookView {
    fun showSuccessToast()
    fun showInvalidUrlErrorToast()
    fun showGenericErrorToast()
    fun finishView()
    fun trackFailedUrl(url: String)
}
