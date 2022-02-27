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
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.phicdy.mycuration.articlelist.util.bitmapFrom
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.tracker.TrackerHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ArticlesListFragment : Fragment(), ArticleListAdapter.Listener {

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

    private lateinit var recyclerView: ArticleRecyclerView
    private lateinit var articlesListAdapter: ArticleListAdapter

    private lateinit var listener: OnArticlesListFragmentListener
    private lateinit var emptyView: TextView

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
            viewModel.binding.collect { uiBinding ->
                when (uiBinding) {
                    ArticleListUiBinding.Init -> {
                        // do nothing
                    }
                    is ArticleListUiBinding.Loaded -> {
                        if (uiBinding.list.isEmpty()) {
                            showEmptyView()
                        } else {
                            articlesListAdapter.submitList(uiBinding.list)
                        }
                    }
                    is ArticleListUiBinding.Searched -> {
                        if (uiBinding.list.isEmpty()) {
                            showNoSearchResult()
                        } else {
                            articlesListAdapter.submitList(uiBinding.list)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.interationChannel.collect { interation ->
                when (interation) {
                    is Interation.Scroll -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            val manager = recyclerView.layoutManager as LinearLayoutManager
                            val firstPositionBeforeScroll = manager.findFirstVisibleItemPosition()
                            val num = interation.positionAfterScroll - firstPositionBeforeScroll + 1
                            scrollTo(interation.positionAfterScroll)
                            delay(250) // Wait for scroll
                            articlesListAdapter.notifyItemRangeChanged(manager.findFirstVisibleItemPosition(), num)
                            runFinishActionCreator()
                        }
                    }
                    is Interation.OpenInternalWebBrowser -> openInternalWebView(interation.url)
                    is Interation.OpenExternalWebBrowser -> openExternalWebView(interation.url)
                    is Interation.Share -> showShareUi(interation.url)
                    is Interation.ReadArticle -> articlesListAdapter.notifyItemChanged(interation.position)
                    is Interation.SwipeArtilce -> {
                        articlesListAdapter.notifyItemChanged(interation.position)
                        runFinishActionCreator()
                    }
                    Interation.ReadAllOfArticles -> {
                        notifyListView()
                        runFinishActionCreator()
                    }
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
            finishStateActionCreator.run(articlesListAdapter.currentList)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as OnArticlesListFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement Article list listener")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_articles_list, container, false)
        recyclerView = view.findViewById(R.id.rv_article) as ArticleRecyclerView
        emptyView = view.findViewById(R.id.emptyViewArticle) as TextView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        articlesListAdapter = ArticleListAdapter(viewLifecycleOwner.lifecycleScope, this, adProvider, updateFavoriteStatusActionCreator)
        recyclerView.adapter = articlesListAdapter
        setAllListener()
        return view
    }

    private fun setAllListener() {
        val helper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                return makeFlag(ACTION_STATE_SWIPE, LEFT or RIGHT)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewLifecycleOwner.lifecycleScope.launch {
                    swipeActionCreator.run(viewHolder.adapterPosition, direction, articlesListAdapter.currentList)
                }
            }
        })
        helper.attachToRecyclerView(recyclerView)
        recyclerView.addItemDecoration(helper)
    }

    fun onFabButtonClicked() {
        val manager = recyclerView.layoutManager as LinearLayoutManager
        viewModel.onFabButtonClicked(
                manager.findFirstVisibleItemPosition(),
                manager.findLastCompletelyVisibleItemPosition(),
                articlesListAdapter.currentList
        )
    }

    fun handleAllRead() {
        viewLifecycleOwner.lifecycleScope.launch {
            readAllArticlesActionCreator.run(rssId, articlesListAdapter.currentList)
        }
    }

    private fun openInternalWebView(url: String) {
        TrackerHelper.sendButtonEvent(getString(R.string.tap_article_internal))
        val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, url)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
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

    private fun notifyListView() {
        articlesListAdapter.notifyDataSetChanged()
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

    private fun scrollTo(position: Int) {
        recyclerView.smoothScrollToPosition(position)
    }

    private fun showEmptyView() {
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        emptyView.text = getText(R.string.no_article)
    }

    private fun showNoSearchResult() {
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        emptyView.text = getText(R.string.no_search_result)
    }

    override fun onItemClicked(position: Int, articles: List<ArticleItem>) {
        viewLifecycleOwner.lifecycleScope.launch {
            readArticleActionCreator.run(position, articles)
            openUrlActionCreator.run(articles[position])
        }
    }

    override fun onItemLongClicked(position: Int, articles: List<ArticleItem>) {
        viewLifecycleOwner.lifecycleScope.launch {
            shareUrlActionCreator.run(position, articles)
        }
    }
}