package com.phicdy.mycuration.articlelist

import android.app.PendingIntent
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.phicdy.mycuration.advertisement.AdProvider
import com.phicdy.mycuration.articlelist.action.FetchAllArticleListActionCreator
import com.phicdy.mycuration.articlelist.action.FetchArticleListOfRssActionCreator
import com.phicdy.mycuration.articlelist.action.FinishStateActionCreator
import com.phicdy.mycuration.articlelist.action.OpenUrlActionCreator
import com.phicdy.mycuration.articlelist.action.ReadAllArticlesActionCreator
import com.phicdy.mycuration.articlelist.action.ReadArticleActionCreator
import com.phicdy.mycuration.articlelist.action.ScrollActionCreator
import com.phicdy.mycuration.articlelist.action.SearchArticleListActionCreator
import com.phicdy.mycuration.articlelist.action.ShareUrlActionCreator
import com.phicdy.mycuration.articlelist.action.SwipeActionCreator
import com.phicdy.mycuration.articlelist.action.UpdateFavoriteStatusActionCreator
import com.phicdy.mycuration.articlelist.ui.ArticleListScreen
import com.phicdy.mycuration.articlelist.ui.firstVisibleItemPosition
import com.phicdy.mycuration.articlelist.ui.lastCompletelyVisibleItemPosition
import com.phicdy.mycuration.articlelist.util.bitmapFrom
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.resource.MyCurationTheme
import com.phicdy.mycuration.tracker.TrackerHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ArticlesListFragment : Fragment() {

    companion object {
        const val RSS_ID = "RSS_ID"

        fun newInstance(rssId: Int) = ArticlesListFragment().apply {
            arguments = Bundle().apply {
                putInt(RSS_ID, rssId)
            }
        }
    }

    private val rssId: Int by lazy {
        arguments?.getInt(RSS_ID, Feed.ALL_FEED_ID) ?: Feed.ALL_FEED_ID
    }

    @Inject
    lateinit var fetchArticleListOfRssActionCreator: FetchArticleListOfRssActionCreator

    @Inject
    lateinit var fetchAllArticleListArticleListActionCreator: FetchAllArticleListActionCreator

    @Inject
    lateinit var searchArticleListActionCreator: SearchArticleListActionCreator

    @Inject
    lateinit var updateFavoriteStatusActionCreator: UpdateFavoriteStatusActionCreator

    @Inject
    lateinit var finishStateActionCreator: FinishStateActionCreator

    @Inject
    lateinit var swipeActionCreator: SwipeActionCreator

    @Inject
    lateinit var scrollActionCreator: ScrollActionCreator

    @Inject
    lateinit var readAllArticlesActionCreator: ReadAllArticlesActionCreator

    @Inject
    lateinit var readArticleActionCreator: ReadArticleActionCreator

    @Inject
    lateinit var openUrlActionCreator: OpenUrlActionCreator

    @Inject
    lateinit var shareUrlActionCreator: ShareUrlActionCreator

    private val viewModel: ArticleListViewModel by viewModels()

    /** State of the LazyColumn, available while the list is composed. */
    private var listState: LazyListState? = null

    private val mutableScrollRequests = MutableSharedFlow<Int>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val scrollRequests: SharedFlow<Int> = mutableScrollRequests

    private lateinit var listener: OnArticlesListFragmentListener

    @Inject
    lateinit var adProvider: AdProvider

    interface OnArticlesListFragmentListener {
        fun finish()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set swipe direction
        val prefMgr = PreferenceHelper
        prefMgr.setSearchFeedId(rssId)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.interationChannel.collect { interation ->
                when (interation) {
                    // Read rows are re-rendered from uiState. The list scrolls and then
                    // runFinishActionCreator() is called when the scroll animation ends.
                    is Interation.Scroll -> mutableScrollRequests.tryEmit(interation.positionAfterScroll)
                    is Interation.OpenInternalWebBrowser -> openInternalWebView(interation.url)
                    is Interation.OpenExternalWebBrowser -> openExternalWebView(interation.url)
                    is Interation.Share -> showShareUi(interation.url)
                    // Changed rows are re-rendered from uiState
                    is Interation.ReadArticle -> Unit
                    is Interation.SwipeArtilce -> runFinishActionCreator()
                    Interation.ReadAllOfArticles -> runFinishActionCreator()
                    Interation.Finish -> finish()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            when {
                activity?.intent?.action == Intent.ACTION_SEARCH -> {
                    val query = activity?.intent?.getStringExtra(SearchManager.QUERY) ?: ""
                    searchArticleListActionCreator.run(query)
                }
                rssId == Feed.ALL_FEED_ID -> fetchAllArticleListArticleListActionCreator.run()
                else -> fetchArticleListOfRssActionCreator.run(rssId)
            }
        }
    }

    private fun runFinishActionCreator() {
        viewLifecycleOwner.lifecycleScope.launch {
            finishStateActionCreator.run(currentArticles())
        }
    }

    /** Raw list that corresponds to the rows of the LazyColumn, for position based action creators. */
    private fun currentArticles(): List<ArticleItem> = when (val binding = viewModel.binding.value) {
        ArticleListUiBinding.Init -> emptyList()
        is ArticleListUiBinding.Loaded -> binding.list
        is ArticleListUiBinding.Searched -> binding.list
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as OnArticlesListFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement Article list listener")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                val state = rememberLazyListState()
                SideEffect { listState = state }
                MyCurationTheme {
                    ArticleListScreen(
                        uiState = uiState,
                        listState = state,
                        adProvider = adProvider,
                        onItemClick = ::onItemClicked,
                        onItemLongClick = ::onItemLongClicked,
                        onFavoriteClick = ::onFavoriteClicked,
                        onSwipe = ::onSwiped,
                        scrollRequests = scrollRequests,
                        onScrollFinished = ::runFinishActionCreator,
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        listState = null
        super.onDestroyView()
    }

    fun onFabButtonClicked() {
        val state = listState ?: return
        viewModel.onFabButtonClicked(
                state.firstVisibleItemPosition(),
                state.lastCompletelyVisibleItemPosition(),
                currentArticles()
        )
    }

    fun handleAllRead() {
        viewLifecycleOwner.lifecycleScope.launch {
            readAllArticlesActionCreator.run(rssId, currentArticles())
        }
    }

    private fun openInternalWebView(url: String) {
        TrackerHelper.sendButtonEvent(getString(R.string.tap_article_internal))
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, url)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        activity?.let { activity ->
            val icon = bitmapFrom(activity, R.drawable.ic_share)
            icon?.let {
                try {
                    val customTabsIntent = CustomTabsIntent.Builder()
                            .setShowTitle(true)
                            .setToolbarColor(ContextCompat.getColor(activity, R.color.background_toolbar))
                            .setActionButton(icon, getString(R.string.share), pendingIntent)
                            .build()
                    customTabsIntent.intent.setPackage("com.android.chrome")
                    customTabsIntent.launchUrl(activity, Uri.parse(url))
                } catch (e: ActivityNotFoundException) {
                    Snackbar.make(activity.findViewById(R.id.fab_article_list), R.string.open_internal_browser_error, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openExternalWebView(url: String) {
        TrackerHelper.sendButtonEvent(getString(R.string.tap_article_external))
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    fun finish() {
        listener.finish()
    }

    private fun showShareUi(url: String) {
        if (isAdded) TrackerHelper.sendButtonEvent(getString(R.string.share_article))
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, url)
        startActivity(intent)
    }

    private fun onItemClicked(position: Int) {
        val articles = currentArticles()
        viewLifecycleOwner.lifecycleScope.launch {
            readArticleActionCreator.run(position, articles)
            openUrlActionCreator.run(articles[position])
        }
    }

    private fun onItemLongClicked(position: Int) {
        val articles = currentArticles()
        viewLifecycleOwner.lifecycleScope.launch {
            shareUrlActionCreator.run(position, articles)
        }
    }

    private fun onFavoriteClicked(position: Int) {
        val articles = currentArticles()
        viewLifecycleOwner.lifecycleScope.launch {
            updateFavoriteStatusActionCreator.run(position, articles)
        }
    }

    private fun onSwiped(position: Int, direction: Int) {
        val articles = currentArticles()
        viewLifecycleOwner.lifecycleScope.launch {
            swipeActionCreator.run(position, direction, articles)
        }
    }
}