package com.phicdy.mycuration.articlelist

import android.app.PendingIntent
import android.app.SearchManager
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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.phicdy.mycuration.articlelist.action.FetchAllArticleListActionCreator
import com.phicdy.mycuration.articlelist.action.FetchArticleListOfCurationActionCreator
import com.phicdy.mycuration.articlelist.action.FetchArticleListOfRssActionCreator
import com.phicdy.mycuration.articlelist.action.FinishStateActionCreator
import com.phicdy.mycuration.articlelist.action.OpenUrlActionCreator
import com.phicdy.mycuration.articlelist.action.ReadAllArticlesActionCreator
import com.phicdy.mycuration.articlelist.action.ReadArticleActionCreator
import com.phicdy.mycuration.articlelist.action.ScrollActionCreator
import com.phicdy.mycuration.articlelist.action.SearchArticleListActionCreator
import com.phicdy.mycuration.articlelist.action.ShareUrlActionCreator
import com.phicdy.mycuration.articlelist.action.SwipeActionCreator
import com.phicdy.mycuration.articlelist.store.ArticleListStore
import com.phicdy.mycuration.articlelist.store.FinishStateStore
import com.phicdy.mycuration.articlelist.store.OpenExternalWebBrowserStateStore
import com.phicdy.mycuration.articlelist.store.OpenInternalWebBrowserStateStore
import com.phicdy.mycuration.articlelist.store.ReadAllArticlesStateStore
import com.phicdy.mycuration.articlelist.store.ReadArticlePositionStore
import com.phicdy.mycuration.articlelist.store.ScrollPositionStore
import com.phicdy.mycuration.articlelist.store.SearchResultStore
import com.phicdy.mycuration.articlelist.store.ShareUrlStore
import com.phicdy.mycuration.articlelist.store.SwipePositionStore
import com.phicdy.mycuration.articlelist.util.bitmapFrom
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.tracker.TrackerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.scope.currentScope
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.CoroutineContext


class ArticlesListFragment : Fragment(), CoroutineScope, ArticleListAdapter.Listener {

    companion object {
        const val RSS_ID = "RSS_ID"
        const val CURATION_ID = "CURATION_ID"
        const val DEFAULT_CURATION_ID = -1

        fun newInstance(rssId: Int, curationId: Int) = ArticlesListFragment().apply {
            arguments = Bundle().apply {
                putInt(RSS_ID, rssId)
                putInt(CURATION_ID, curationId)
            }
        }
    }

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val rssId: Int  by lazy {
        arguments?.getInt(RSS_ID, Feed.ALL_FEED_ID) ?: Feed.ALL_FEED_ID
    }
    private val curationId: Int by lazy {
        arguments?.getInt(CURATION_ID, DEFAULT_CURATION_ID) ?: DEFAULT_CURATION_ID
    }

    private val fetchArticleListOfRssActionCreator by currentScope.inject<FetchArticleListOfRssActionCreator> {
        parametersOf(rssId)
    }

    private val fetchArticleListOfCurationActionCreator by currentScope.inject<FetchArticleListOfCurationActionCreator> {
        parametersOf(curationId)
    }

    private val fetchAllArticleListArticleListActionCreator by currentScope.inject<FetchAllArticleListActionCreator> {
        parametersOf()
    }

    private val searchArticleListActionCreator by currentScope.inject<SearchArticleListActionCreator> {
        val query = activity?.intent?.getStringExtra(SearchManager.QUERY) ?: ""
        parametersOf(query)
    }

    private val articleListStore: ArticleListStore by currentScope.inject()
    private val searchResultStore: SearchResultStore by currentScope.inject()
    private val finishStateStore: FinishStateStore by currentScope.inject()
    private val readArticlePositionStore: ReadArticlePositionStore by currentScope.inject()
    private val openInternalWebBrowserStateStore: OpenInternalWebBrowserStateStore by currentScope.inject()
    private val openExternalWebBrowserStateStore: OpenExternalWebBrowserStateStore by currentScope.inject()
    private val scrollPositionStore: ScrollPositionStore by currentScope.inject()
    private val swipePositionStore: SwipePositionStore by currentScope.inject()
    private val readAllArticlesStateStore: ReadAllArticlesStateStore by currentScope.inject()
    private val shareUrlStore: ShareUrlStore by currentScope.inject()

    private lateinit var recyclerView: ArticleRecyclerView
    private lateinit var articlesListAdapter: ArticleListAdapter

    private lateinit var listener: OnArticlesListFragmentListener
    private lateinit var emptyView: TextView

    interface OnArticlesListFragmentListener {
        fun finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

        // Set swipe direction
        val prefMgr = PreferenceHelper
        prefMgr.setSearchFeedId(rssId)

        articleListStore.state.observe(this, Observer<List<Article>> {
            if (it.isEmpty()) {
                showEmptyView()
            } else {
                articlesListAdapter.submitList(it)
            }
        })
        searchResultStore.state.observe(this, Observer<List<Article>> {
            if (it.isEmpty()) {
                showNoSearchResult()
            } else {
                articlesListAdapter.submitList(it)
            }
        })
        readArticlePositionStore.state.observe(this, Observer<Int> {
            articlesListAdapter.notifyItemChanged(it)
        })
        finishStateStore.state.observe(this, Observer<Boolean> {
            if (it) listener.finish()
        })
        openInternalWebBrowserStateStore.state.observe(this, Observer<Article> {
            openInternalWebView(it.url)
        })
        openExternalWebBrowserStateStore.state.observe(this, Observer<String> {
            openExternalWebView(it)
        })
        scrollPositionStore.state.observe(this, Observer<Int> {
            launch {
                scrollTo(it)
                delay(200) // Wait for scroll
                val manager = recyclerView.layoutManager as LinearLayoutManager
                articlesListAdapter.notifyItemRangeChanged(manager.findFirstVisibleItemPosition(), it)
                runFinishActionCreator()
            }
        })
        swipePositionStore.state.observe(this, Observer<Int> {
            articlesListAdapter.notifyItemChanged(it)
            runFinishActionCreator()
        })
        readAllArticlesStateStore.state.observe(this, Observer<Unit> {
            notifyListView()
            runFinishActionCreator()
        })
        shareUrlStore.state.observe(this, Observer<String> {
            showShareUi(it)
        })
    }

    private fun runFinishActionCreator() {
        launch {
            FinishStateActionCreator(
                    dispatcher = get(),
                    preferenceHelper = get(),
                    articles = articlesListAdapter.currentList
            ).run()
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
        articlesListAdapter = ArticleListAdapter(this, this)
        recyclerView.adapter = articlesListAdapter
        setAllListener()
        launch {
            when {
                activity?.intent?.action == Intent.ACTION_SEARCH -> searchArticleListActionCreator.run()
                curationId != -1 -> fetchArticleListOfCurationActionCreator.run()
                rssId == Feed.ALL_FEED_ID -> fetchAllArticleListArticleListActionCreator.run()
                else -> fetchArticleListOfRssActionCreator.run()
            }
        }
        return view
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
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
                val actionCreator = SwipeActionCreator(
                        dispatcher = get(),
                        articleRepository = get(),
                        unreadCountRepository = get(),
                        preferenceHelper = get(),
                        position = viewHolder.adapterPosition,
                        direction = direction,
                        articles = articlesListAdapter.currentList
                )
                launch {
                    actionCreator.run()
                }
            }
        })
        helper.attachToRecyclerView(recyclerView)
        recyclerView.addItemDecoration(helper)
    }

    fun onFabButtonClicked() {
        launch {
            val manager = recyclerView.layoutManager as LinearLayoutManager
            ScrollActionCreator(
                    dispatcher = get(),
                    articleRepository = get(),
                    unreadCountRepository = get(),
                    firstVisiblePosition = manager.findFirstVisibleItemPosition(),
                    lastVisiblePosition = manager.findLastCompletelyVisibleItemPosition(),
                    allArticles = articlesListAdapter.currentList
            ).run()
        }
    }

    fun handleAllRead() {
        launch {
            ReadAllArticlesActionCreator(
                    dispatcher = get(),
                    articleRepository = get(),
                    unreadCountRepository = get(),
                    feedId = rssId,
                    allArticles = articlesListAdapter.currentList
            ).run()
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
                val customTabsIntent = CustomTabsIntent.Builder()
                        .setShowTitle(true)
                        .setToolbarColor(ContextCompat.getColor(activity, R.color.background_toolbar))
                        .setActionButton(icon, getString(R.string.share), pendingIntent)
                        .build()
                customTabsIntent.launchUrl(activity, Uri.parse(url))
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

    override fun onItemClicked(position: Int, articles: List<Article>) {
        val actionCreator = ReadArticleActionCreator(
                dispatcher = get(),
                articleRepository = get(),
                unreadCountRepository = get(),
                position = position,
                articles = articles
        )
        launch {
            actionCreator.run()
        }
        val openUrlActionCreator = OpenUrlActionCreator(
                dispatcher = get(),
                preferenceHelper = get(),
                feedId = rssId,
                article = articles[position],
                rssRepository = get()
        )
        launch {
            openUrlActionCreator.run()
        }
    }

    override fun onItemLongClicked(position: Int, articles: List<Article>) {
        launch {
            ShareUrlActionCreator(
                    dispatcher = get(),
                    position = position,
                    articles = articles
            ).run()
        }
    }
}