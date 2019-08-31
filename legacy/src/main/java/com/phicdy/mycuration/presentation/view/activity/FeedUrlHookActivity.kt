package com.phicdy.mycuration.presentation.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import com.phicdy.mycuration.feature.util.changeTheme
import com.phicdy.mycuration.legacy.R
import com.phicdy.mycuration.presentation.presenter.FeedUrlHookPresenter
import com.phicdy.mycuration.presentation.view.FeedUrlHookView
import com.phicdy.mycuration.tracker.TrackerHelper
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.scope.currentScope
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.CoroutineContext

class FeedUrlHookActivity : AppCompatActivity(), FeedUrlHookView, CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val presenter: FeedUrlHookPresenter by currentScope.inject {
        parametersOf(
                this,
                if (intent.action == null) "" else intent.action,
                if (intent.dataString == null) "" else intent.dataString,
                if (intent.extras == null) "" else intent.extras?.getCharSequence(Intent.EXTRA_TEXT, "")
                        ?: "",
                this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_url_hook)
        launch { presenter.create() }
    }

    override fun onResume() {
        super.onResume()
        changeTheme()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
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
        TrackerHelper.sendFailedParseUrl(getString(R.string.add_rss_from_intent_error), url)
    }

    override fun showSuccessToast() {
        showToastOnUiThread(R.string.add_rss_success, Toast.LENGTH_SHORT)
    }

    @UiThread
    private fun showToastOnUiThread(@StringRes res: Int, toastLength: Int) {
        runOnUiThread { Toast.makeText(applicationContext, res, toastLength).show() }
    }
}
