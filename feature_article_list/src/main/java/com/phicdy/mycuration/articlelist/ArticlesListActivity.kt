package com.phicdy.mycuration.articlelist

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.compose.AndroidFragment
import androidx.lifecycle.lifecycleScope
import com.phicdy.mycuration.articlelist.ui.ArticleDetailPane
import com.phicdy.mycuration.articlelist.ui.ArticlesListPanes
import com.phicdy.mycuration.articlelist.ui.ArticlesListScaffold
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.feature.util.changeTheme
import com.phicdy.mycuration.resource.MyCurationTheme
import com.phicdy.mycuration.tracker.TrackerHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ArticlesListActivity : AppCompatActivity(),
    ArticlesListFragment.OnArticlesListFragmentListener {

    companion object {
        private const val RSS_ID = "RSS_ID"

        fun createIntent(context: Context, rssId: Int): Intent =
            Intent(context, ArticlesListActivity::class.java).apply {
                putExtra(RSS_ID, rssId)
            }
    }

    @Inject
    lateinit var rssRepository: RssRepository

    private var toolbarTitle: String by mutableStateOf("")

    private val snackbarHostState = SnackbarHostState()

    /** The hosted list, available while it is attached. */
    private var articlesListFragment: ArticlesListFragment? = null

    private val detailRequests = MutableSharedFlow<SelectedArticle>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set feed id and url from main activity
        val feedId = intent.getIntExtra(RSS_ID, Feed.ALL_FEED_ID)

        setContent {
            MyCurationTheme {
                ArticlesListPanes(
                    isOpenInternal = PreferenceHelper.isOpenInternal,
                    detailRequests = detailRequests,
                    modifier = Modifier.systemBarsPadding(),
                    listPane = { isTwoPane, isLargeWindow, selectedArticle ->
                        // The fragment's own composition reads these, so pass them on every update
                        SideEffect {
                            articlesListFragment?.updatePaneState(isTwoPane, isLargeWindow, selectedArticle)
                        }
                        ArticlesListScaffold(
                            title = toolbarTitle,
                            snackbarHostState = snackbarHostState,
                            onNavigateUp = ::finish,
                            onReadAllClick = ::markAllAsRead,
                            onSearch = ::openSearchResult,
                            onFabClick = ::scrollDownAndMarkAsRead,
                        ) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                AndroidFragment<ArticlesListFragment>(
                                    arguments = bundleOf(ArticlesListFragment.RSS_ID to feedId),
                                    onUpdate = { fragment ->
                                        articlesListFragment = fragment
                                        fragment.updatePaneState(isTwoPane, isLargeWindow, selectedArticle)
                                    },
                                )
                            }
                        }
                    },
                    detailPane = { selectedArticle ->
                        ArticleDetailPane(
                            article = selectedArticle,
                            onShareClick = ::shareArticle,
                            onOpenInBrowserClick = ::openArticleInBrowser,
                        )
                    },
                )
            }
        }

        lifecycleScope.launch {
            val trackerTitle = when (feedId) {
                Feed.ALL_FEED_ID -> {
                    // All article
                    toolbarTitle = getString(R.string.all)
                    getString(R.string.all)
                }

                else -> {
                    // Select a feed
                    val prefMgr = PreferenceHelper
                    prefMgr.setSearchFeedId(feedId)
                    toolbarTitle = rssRepository.getFeedById(feedId)?.title.orEmpty()
                    getString(R.string.ga_not_all_title)
                }
            }
            TrackerHelper.sendUiEvent(trackerTitle)
        }
    }

    private fun markAllAsRead() {
        TrackerHelper.sendButtonEvent(getString(R.string.read_all_articles))
        articlesListFragment?.markAllAsRead()
    }

    private fun scrollDownAndMarkAsRead() {
        articlesListFragment?.scrollDownAndMarkAsRead()
        TrackerHelper.sendButtonEvent(getString(R.string.scroll_article_list))
    }

    private fun openSearchResult(query: String) {
        val intent = Intent(this, ArticleSearchResultActivity::class.java)
        intent.action = Intent.ACTION_SEARCH
        intent.putExtra(SearchManager.QUERY, query)
        startActivity(intent)
    }

    private fun ArticlesListFragment.updatePaneState(
        isTwoPane: Boolean,
        isLargeWindow: Boolean,
        selectedArticle: SelectedArticle?,
    ) {
        this.isTwoPane = isTwoPane
        this.isLargeWindow = isLargeWindow
        selectedArticleId = selectedArticle?.id
    }

    private fun shareArticle(article: SelectedArticle) {
        TrackerHelper.sendButtonEvent(getString(R.string.share_article))
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, article.url)
        startActivity(intent)
    }

    private fun openArticleInBrowser(article: SelectedArticle) {
        TrackerHelper.sendButtonEvent(getString(R.string.tap_article_external))
        startActivity(Intent(Intent.ACTION_VIEW, article.url.toUri()))
    }

    override fun showArticleInDetailPane(article: SelectedArticle) {
        detailRequests.tryEmit(article)
    }

    override fun showOpenInternalBrowserError() {
        lifecycleScope.launch {
            snackbarHostState.showSnackbar(getString(R.string.open_internal_browser_error))
        }
    }

    override fun onResume() {
        super.onResume()
        changeTheme()
    }
}