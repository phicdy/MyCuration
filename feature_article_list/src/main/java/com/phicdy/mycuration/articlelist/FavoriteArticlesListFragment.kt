package com.phicdy.mycuration.articlelist

import android.app.PendingIntent
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
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.phicdy.mycuration.advertisement.AdProvider
import com.phicdy.mycuration.articlelist.action.FetchFavoriteArticleListActionCreator
import com.phicdy.mycuration.articlelist.action.FinishStateActionCreator
import com.phicdy.mycuration.articlelist.action.OpenUrlActionCreator
import com.phicdy.mycuration.articlelist.action.ReadAllFavoriteArticlesActionCreator
import com.phicdy.mycuration.articlelist.action.ReadArticleActionCreator
import com.phicdy.mycuration.articlelist.action.ScrollActionCreator
import com.phicdy.mycuration.articlelist.action.ShareUrlActionCreator
import com.phicdy.mycuration.articlelist.action.SwipeActionCreator
import com.phicdy.mycuration.articlelist.action.UpdateFavoriteStatusActionCreator
import com.phicdy.mycuration.articlelist.store.ArticleListStore
import com.phicdy.mycuration.articlelist.store.FinishStateStore
import com.phicdy.mycuration.articlelist.store.OpenExternalWebBrowserStateStore
import com.phicdy.mycuration.articlelist.store.OpenInternalWebBrowserStateStore
import com.phicdy.mycuration.articlelist.store.ReadAllArticlesStateStore
import com.phicdy.mycuration.articlelist.store.ReadArticlePositionStore
import com.phicdy.mycuration.articlelist.store.ScrollPositionStore
import com.phicdy.mycuration.articlelist.store.ShareUrlStore
import com.phicdy.mycuration.articlelist.store.SwipePositionStore
import com.phicdy.mycuration.articlelist.util.bitmapFrom
import com.phicdy.mycuration.tracker.TrackerHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope


class FavoriteArticlesListFragment : Fragment(), ArticleListAdapter.Listener {

    companion object {
        fun newInstance() = FavoriteArticlesListFragment()
    }

    private val fetchFavoriteArticleListActionCreator: FetchFavoriteArticleListActionCreator by currentScope.inject()
    private val updateFavoriteStatusActionCreator: UpdateFavoriteStatusActionCreator by currentScope.inject()

    private val articleListStore: ArticleListStore by currentScope.inject()
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

    private val adProvider by inject<AdProvider>()

    interface OnArticlesListFragmentListener {
        fun finish()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        articleListStore.state.observe(viewLifecycleOwner, Observer<List<ArticleItem>> {
            if (it.isEmpty()) {
                showEmptyView()
            } else {
                articlesListAdapter.submitList(it)
            }
        })
        readArticlePositionStore.state.observe(viewLifecycleOwner, Observer<Int> {
            articlesListAdapter.notifyItemChanged(it)
        })
        finishStateStore.state.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it) listener.finish()
        })
        openInternalWebBrowserStateStore.state.observe(viewLifecycleOwner, Observer<String> { url ->
            openInternalWebView(url)
        })
        openExternalWebBrowserStateStore.state.observe(viewLifecycleOwner, Observer<String> {
            openExternalWebView(it)
        })
        scrollPositionStore.state.observe(viewLifecycleOwner, Observer<Int> { positionAfterScroll ->
            viewLifecycleOwner.lifecycleScope.launch {
                val manager = recyclerView.layoutManager as LinearLayoutManager
                val firstPositionBeforeScroll = manager.findFirstVisibleItemPosition()
                val num = positionAfterScroll - firstPositionBeforeScroll + 1
                scrollTo(positionAfterScroll)
                delay(250) // Wait for scroll
                articlesListAdapter.notifyItemRangeChanged(manager.findFirstVisibleItemPosition(), num)
                runFinishActionCreator()
            }
        })
        swipePositionStore.state.observe(viewLifecycleOwner, Observer<Int> {
            articlesListAdapter.notifyItemChanged(it)
            runFinishActionCreator()
        })
        readAllArticlesStateStore.state.observe(viewLifecycleOwner, Observer {
            notifyListView()
            runFinishActionCreator()
        })
        shareUrlStore.state.observe(viewLifecycleOwner, Observer<String> {
            showShareUi(it)
        })
    }

    private fun runFinishActionCreator() {
        viewLifecycleOwner.lifecycleScope.launch {
            FinishStateActionCreator(
                    dispatcher = get(),
                    preferenceHelper = get()
            ).run(articlesListAdapter.currentList)
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
        viewLifecycleOwner.lifecycleScope.launch {
            fetchFavoriteArticleListActionCreator.run()
        }
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
                val actionCreator = SwipeActionCreator(
                        dispatcher = get(),
                        articleRepository = get(),
                        rssRepository = get(),
                        preferenceHelper = get()
                )
                viewLifecycleOwner.lifecycleScope.launch {
                    actionCreator.run(viewHolder.adapterPosition, direction, articlesListAdapter.currentList)
                }
            }
        })
        helper.attachToRecyclerView(recyclerView)
        recyclerView.addItemDecoration(helper)
    }

    fun onFabButtonClicked() {
        viewLifecycleOwner.lifecycleScope.launch {
            val manager = recyclerView.layoutManager as LinearLayoutManager
            ScrollActionCreator(
                    dispatcher = get(),
                    articleRepository = get(),
                    rssRepository = get()
            ).run(
                    firstVisiblePosition = manager.findFirstVisibleItemPosition(),
                    lastVisiblePosition = manager.findLastCompletelyVisibleItemPosition(),
                    items = articlesListAdapter.currentList
            )
        }
    }

    fun handleAllRead() {
        viewLifecycleOwner.lifecycleScope.launch {
            ReadAllFavoriteArticlesActionCreator(
                    dispatcher = get(),
                    articleRepository = get(),
                    rssRepository = get(),
                    items = articlesListAdapter.currentList
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

    override fun onItemClicked(position: Int, articles: List<ArticleItem>) {
        val actionCreator = ReadArticleActionCreator(
                dispatcher = get(),
                articleRepository = get(),
                rssRepository = get()
        )
        viewLifecycleOwner.lifecycleScope.launch {
            actionCreator.run(
                    position = position,
                    items = articles
            )
        }
        val openUrlActionCreator = OpenUrlActionCreator(
                dispatcher = get(),
                preferenceHelper = get()
        )
        viewLifecycleOwner.lifecycleScope.launch {
            openUrlActionCreator.run(item = articles[position])
        }
    }

    override fun onItemLongClicked(position: Int, articles: List<ArticleItem>) {
        viewLifecycleOwner.lifecycleScope.launch {
            ShareUrlActionCreator(
                    dispatcher = get()
            ).run(
                    position = position,
                    items = articles
            )
        }
    }
}