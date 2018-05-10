package com.phicdy.mycuration.presentation.presenter

import android.view.KeyEvent
import com.phicdy.mycuration.presentation.view.InternalWebViewView
import com.phicdy.mycuration.util.UrlUtil

class InternalWebViewPresenter(private val view: InternalWebViewView, private val url: String) : Presenter {

    override fun create() {
        if (!UrlUtil.isCorrectUrl(url)) return
        view.initWebView()
        view.initToolbar()
        view.load(url)
    }

    override fun resume() {
    }

    override fun pause() {
    }

    fun onShareMenuClicked() {
        if (!UrlUtil.isCorrectUrl(url)) return
        view.share(url)
    }

    fun onKeyDown(keyCode: Int, event: KeyEvent, canGoBack: Boolean): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (canGoBack) {
                view.goBack()
                return true
            }
        }
        return view.parentOnKeyDown(keyCode, event)
    }
}