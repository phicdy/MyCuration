package com.phicdy.mycuration.presentation.view

import android.view.KeyEvent

interface InternalWebViewView {
    fun initWebView()
    fun load(url: String)
    fun share(url: String)
    fun goBack()
    fun parentOnKeyDown(keyCode: Int, event: KeyEvent): Boolean
}