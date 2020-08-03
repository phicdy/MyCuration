package com.phicdy.mycuration.curatedarticlelist

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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.phicdy.mycuration.advertisement.AdProvider
import com.phicdy.mycuration.curatedarticlelist.action.FetchCuratedArticleListActionCreator
import com.phicdy.mycuration.curatedarticlelist.action.FinishStateActionCreator
import com.phicdy.mycuration.curatedarticlelist.action.OpenUrlActionCreator
import com.phicdy.mycuration.curatedarticlelist.action.ReadAllCuratedArticlesActionCreator
import com.phicdy.mycuration.curatedarticlelist.action.ReadCuratedArticleActionCreator
import com.phicdy.mycuration.curatedarticlelist.action.ScrollActionCreator
import com.phicdy.mycuration.curatedarticlelist.action.ShareUrlActionCreator
import com.phicdy.mycuration.curatedarticlelist.action.SwipeActionCreator
import com.phicdy.mycuration.curatedarticlelist.store.CuratedArticleListStore
import com.phicdy.mycuration.curatedarticlelist.store.FinishCuratedArticleStateStore
import com.phicdy.mycuration.curatedarticlelist.store.OpenCuratedArticleWithExternalWebBrowserStateStore
import com.phicdy.mycuration.curatedarticlelist.store.OpenCuratedArticleWithInternalWebBrowserStateStore
import com.phicdy.mycuration.curatedarticlelist.store.ReadAllCuratedArticlesStateStore
import com.phicdy.mycuration.curatedarticlelist.store.ReadCuratedArticlePositionStore
import com.phicdy.mycuration.curatedarticlelist.store.ScrollCuratedArticlePositionStore
import com.phicdy.mycuration.curatedarticlelist.store.ShareCuratedArticleUrlStore
import com.phicdy.mycuration.curatedarticlelist.store.SwipeCuratedArticlePositionStore
import com.phicdy.mycuration.curatedarticlelist.util.bitmapFrom
import com.phicdy.mycuration.feature_curated_article_list.R
import com.phicdy.mycuration.tracker.TrackerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.CoroutineContext


class CuratedArticlesListFragment : Fragment(), CoroutineScope, CuratedArticleListAdapter.Listener {

    companion object {
        const val CURATION_ID = "CURATION_ID"
        const val DEFAULT_CURATION_ID = -1

        fun newInstance(curationId: Int) = CuratedArticlesListFragment().apply {
            arguments = Bundle().apply {
                putInt(CURATION_ID, curationId)
            }
        }
    }

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val curationId: Int by lazy {
        arguments?.getInt(CURATION_ID, DEFAULT_CURATION_ID) ?: DEFAULT_CURATION_ID
    }

    private val fetchArticleListOfCurationActionCreator by currentScope.inject<FetchCuratedArticleListActionCreator> {
        parametersOf(curationId)
    }

    private val curatedArticleListStore: CuratedArticleListStore by currentScope.inject()
    private val finishCuratedArticleStateStore: FinishCuratedArticleStateStore by currentScope.inject()
    private val readCuratedArticlePositionStore: ReadCuratedArticlePositionStore by currentScope.inject()
    private val openCuratedArticleWithInternalWebBrowserStateStore: OpenCuratedArticleWithInternalWebBrowserStateStore by currentScope.inject()
    private val openCuratedArticleWithExternalWebBrowserStateStore: OpenCuratedArticleWithExternalWebBrowserStateStore by currentScope.inject()
    private val scrollCuratedArticlePositionStore: ScrollCuratedArticlePositionStore by currentScope.inject()
    private val swipeCuratedArticlePositionStore: SwipeCuratedArticlePositionStore by currentScope.inject()
    private val readAllCuratedArticlesStateStore: ReadAllCuratedArticlesStateStore by currentScope.inject()
    private val shareCuratedArticleUrlStore: ShareCuratedArticleUrlStore by currentScope.inject()

    private lateinit var recyclerView: CuratedArticleRecyclerView
    private lateinit var articlesListAdapter: CuratedArticleListAdapter

    private lateinit var listener: OnArticlesListFragmentListener
    private lateinit var emptyView: TextView

    private val adProvider by inject<AdProvider>()

    interface OnArticlesListFragmentListener {
        fun finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

        curatedArticleListStore.state.observe(viewLifecycleOwner, Observer<List<CuratedArticleItem>> {
            if (it.isEmpty()) {
                showEmptyView()
            } else {
                articlesListAdapter.submitList(it)
            }
        })
        readCuratedArticlePositionStore.state.observe(viewLifecycleOwner, Observer<Int> {
            articlesListAdapter.notifyItemChanged(it)
        })
        finishCuratedArticleStateStore.state.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it) listener.finish()
        })
        openCuratedArticleWithInternalWebBrowserStateStore.state.observe(viewLifecycleOwner, Observer<String> { url ->
            openInternalWebView(url)
        })
        openCuratedArticleWithExternalWebBrowserStateStore.state.observe(viewLifecycleOwner, Observer<String> {
            openExternalWebView(it)
        })
        scrollCuratedArticlePositionStore.state.observe(viewLifecycleOwner, Observer<Int> { positionAfterScroll ->
            launch {
                val manager = recyclerView.layoutManager as LinearLayoutManager
                val firstPositionBeforeScroll = manager.findFirstVisibleItemPosition()
                val num = positionAfterScroll - firstPositionBeforeScroll + 1
                scrollTo(positionAfterScroll)
                delay(250) // Wait for scroll
                articlesListAdapter.notifyItemRangeChanged(manager.findFirstVisibleItemPosition(), num)
                runFinishActionCreator()
            }
        })
        swipeCuratedArticlePositionStore.state.observe(viewLifecycleOwner, Observer<Int> {
            articlesListAdapter.notifyItemChanged(it)
            runFinishActionCreator()
        })
        readAllCuratedArticlesStateStore.state.observe(viewLifecycleOwner, Observer<Unit> {
            notifyListView()
            runFinishActionCreator()
        })
        shareCuratedArticleUrlStore.state.observe(viewLifecycleOwner, Observer<String> {
            showShareUi(it)
        })
    }

    private fun runFinishActionCreator() {
        launch {
            FinishStateActionCreator(
                    dispatcher = get(),
                    preferenceHelper = get(),
                    items = articlesListAdapter.currentList
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
        val view = inflater.inflate(R.layout.fragment_curated_articles_list, container, false)
        recyclerView = view.findViewById(R.id.rv_article) as CuratedArticleRecyclerView
        emptyView = view.findViewById(R.id.emptyViewArticle) as TextView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        articlesListAdapter = CuratedArticleListAdapter(this, this, adProvider)
        recyclerView.adapter = articlesListAdapter
        setAllListener()
        launch {
            fetchArticleListOfCurationActionCreator.run()
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
                        rssRepository = get(),
                        preferenceHelper = get(),
                        position = viewHolder.adapterPosition,
                        direction = direction,
                        items = articlesListAdapter.currentList
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
                    rssRepository = get(),
                    firstVisiblePosition = manager.findFirstVisibleItemPosition(),
                    lastVisiblePosition = manager.findLastCompletelyVisibleItemPosition(),
                    items = articlesListAdapter.currentList
            ).run()
        }
    }

    fun handleAllRead() {
        launch {
            ReadAllCuratedArticlesActionCreator(
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

    override fun onItemClicked(position: Int, articles: List<CuratedArticleItem>) {
        val actionCreator = ReadCuratedArticleActionCreator(
                dispatcher = get(),
                articleRepository = get(),
                rssRepository = get(),
                position = position,
                items = articles
        )
        launch {
            actionCreator.run()
        }
        val openUrlActionCreator = OpenUrlActionCreator(
                dispatcher = get(),
                preferenceHelper = get(),
                item = articles[position]
        )
        launch {
            openUrlActionCreator.run()
        }
    }

    override fun onItemLongClicked(position: Int, articles: List<CuratedArticleItem>) {
        launch {
            ShareUrlActionCreator(
                    dispatcher = get(),
                    position = position,
                    items = articles
            ).run()
        }
    }
}