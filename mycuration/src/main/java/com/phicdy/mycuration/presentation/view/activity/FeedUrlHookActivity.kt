package com.phicdy.mycuration.presentation.view.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.annotation.UiThread
import android.widget.Toast

import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.presentation.presenter.FeedUrlHookPresenter
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.domain.rss.UnreadCountManager
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.tracker.GATrackerHelper
import com.phicdy.mycuration.presentation.view.FeedUrlHookView

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class FeedUrlHookActivity : Activity(), FeedUrlHookView {

    private lateinit var presenter: FeedUrlHookPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_url_hook)

        val dbAdapter = DatabaseAdapter.getInstance()
        val unreadCountManager = UnreadCountManager.getInstance()
        val networkTaskManager = NetworkTaskManager
        val parser = RssParser()
        val intent = intent
        val action = if (intent.action == null) "" else intent.action
        val dataString = if (intent.dataString == null) "" else intent.dataString
        val extrasText = if (intent.extras == null) "" else intent.extras.getCharSequence(Intent.EXTRA_TEXT, "")
        presenter = FeedUrlHookPresenter(action, dataString, extrasText,
                dbAdapter, unreadCountManager, networkTaskManager, parser)
        presenter.setView(this)
        presenter.create()
        GATrackerHelper.sendScreen(getString(R.string.add_rss_from_intent))
    }

    override fun onPause() {
        super.onPause()
        presenter.pause()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun showInvalidUrlErrorToast() {
        showToastOnUiThread(R.string.add_rss_error_invalid_url, Toast.LENGTH_SHORT)
    }

    override fun showGenericErrorToast() {
        showToastOnUiThread(R.string.add_rss_error_generic, Toast.LENGTH_SHORT)
    }

    override fun finishView() {
        finish()
    }

    override fun trackFailedUrl(url: String) {
        GATrackerHelper.sendEvent(getString(R.string.add_rss_from_intent_error), url)
    }

    override fun showSuccessToast() {
        showToastOnUiThread(R.string.add_rss_success, Toast.LENGTH_SHORT)
    }

    @UiThread
    private fun showToastOnUiThread(@StringRes res: Int, toastLength: Int) {
        runOnUiThread { Toast.makeText(applicationContext, res, toastLength).show() }
    }
}
