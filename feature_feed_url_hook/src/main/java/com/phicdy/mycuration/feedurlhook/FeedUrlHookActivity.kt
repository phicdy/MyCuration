package com.phicdy.mycuration.feedurlhook

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import com.phicdy.mycuration.domain.rss.RssUrlHookIntentData
import com.phicdy.mycuration.feature.util.changeTheme
import com.phicdy.mycuration.tracker.TrackerHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class FeedUrlHookActivity : AppCompatActivity(), FeedUrlHookView,
    CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    @Inject
    lateinit var presenter: FeedUrlHookPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_url_hook)
        launch { presenter.create() }
    }

    override fun onResume() {
        super.onResume()
        changeTheme()
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

    @Module
    @InstallIn(ActivityComponent::class)
    object FeedUrlHookModule {

        @ActivityScoped
        @Provides
        fun provideFeedUrlHookView(@ActivityContext activity: Context): FeedUrlHookView =
            activity as FeedUrlHookView

        @ActivityScoped
        @Provides
        fun provideRssUrlHookIntentData(@ActivityContext activity: Context): RssUrlHookIntentData =
                RssUrlHookIntentData(
                        action = (activity as AppCompatActivity).intent.action ?: "",
                        dataString = activity.intent.dataString ?: "",
                        extrasText = activity.intent.extras?.getCharSequence(Intent.EXTRA_TEXT, "")
                                ?: ""
                )
    }
}
