package com.phicdy.mycuration.presentation.view.fragment

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.presentation.presenter.ArticleListPresenter
import com.phicdy.mycuration.presentation.view.activity.InternalWebViewActivity
import com.phicdy.mycuration.presentation.view.activity.TopActivity
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.domain.rss.UnreadCountManager
import com.phicdy.mycuration.tracker.TrackerHelper
import com.phicdy.mycuration.util.PreferenceHelper
import com.phicdy.mycuration.presentation.view.ArticleListView
import com.phicdy.mycuration.presentation.view.ArticleRecyclerView

import java.security.InvalidParameterException

import android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE
import android.support.v7.widget.helper.ItemTouchHelper.LEFT
import android.support.v7.widget.helper.ItemTouchHelper.RIGHT

class ArticlesListFragment : Fragment(), ArticleListView {

    companion object {
        const val VIEW_TYPE_ARTICLE = 0
        const val VIEW_TYPE_FOOTER = 1
    }
    private lateinit var presenter: ArticleListPresenter

    private lateinit var recyclerView: ArticleRecyclerView
    private lateinit var articlesListAdapter: SimpleItemRecyclerViewAdapter


    private lateinit var listener: OnArticlesListFragmentListener
    private lateinit var emptyView: TextView

    override val firstVisiblePosition: Int
        get() {
            val manager = recyclerView.layoutManager as LinearLayoutManager
            return manager.findFirstVisibleItemPosition()
        }

    override val lastVisiblePosition: Int
        get() {
            val manager = recyclerView.layoutManager as LinearLayoutManager
            return manager.findLastCompletelyVisibleItemPosition()
        }

    override val isBottomVisible: Boolean
        get() {
            val isLastItemVisible = recyclerView.adapter?.let { lastVisiblePosition == it.itemCount - 1 } ?: false
            val chilidCount = recyclerView.childCount
            if (chilidCount < 1) return false
            val lastItem = recyclerView.getChildAt(chilidCount - 1) ?: return false
            val isLastItemBottomVisible = lastItem.bottom == recyclerView.height
            return isLastItemVisible && isLastItemBottomVisible
        }

    interface OnArticlesListFragmentListener {
        fun finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbAdapter = DatabaseAdapter.getInstance()

        // Set feed id and url from main activity
        val intent = activity?.intent
        val feedId = intent?.getIntExtra(TopActivity.FEED_ID, Feed.ALL_FEED_ID) ?: Feed.ALL_FEED_ID
        val curationId = intent?.getIntExtra(TopActivity.CURATION_ID,
                ArticleListPresenter.DEFAULT_CURATION_ID) ?: ArticleListPresenter.DEFAULT_CURATION_ID
        intent?.putExtra(TopActivity.FEED_ID, feedId)

        // Set swipe direction
        val prefMgr = PreferenceHelper
        prefMgr.setSearchFeedId(feedId)
        val swipeDirectionOption = prefMgr.swipeDirection
        val isOpenInternal = prefMgr.isOpenInternal
        val isAllReadBack = prefMgr.allReadBack
        val isNewestArticleTop = prefMgr.sortNewArticleTop
        val query = intent?.getStringExtra(SearchManager.QUERY) ?: ""
        presenter = ArticleListPresenter(feedId, curationId, dbAdapter, UnreadCountManager,
                isOpenInternal, isAllReadBack, isNewestArticleTop, swipeDirectionOption, query, intent?.action ?: "")
        presenter.setView(this)
        presenter.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            listener = context as OnArticlesListFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement Article list listener")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_articles_list, container, false)
        recyclerView = view.findViewById(R.id.rv_article) as ArticleRecyclerView
        emptyView = view.findViewById(R.id.emptyViewArticle) as TextView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        articlesListAdapter = SimpleItemRecyclerViewAdapter()
        recyclerView.adapter = articlesListAdapter
        setAllListener()
        presenter.createView()
        return view
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    private fun setAllListener() {
        val helper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                return ItemTouchHelper.Callback.makeFlag(ACTION_STATE_SWIPE, LEFT or RIGHT)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                presenter.onSwiped(direction, viewHolder.adapterPosition)
            }
        })
        helper.attachToRecyclerView(recyclerView)
        recyclerView.addItemDecoration(helper)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val manager = recyclerView.layoutManager as LinearLayoutManager
                val lastItemPosition = manager.findLastVisibleItemPosition()
                presenter.onScrolled(lastItemPosition)
            }
        })

    }

    fun onFabButtonClicked() {
        presenter.onFabButtonClicked()
    }

    fun handleAllRead() {
        presenter.handleAllRead()
    }

    override fun openInternalWebView(url: String, rssTitle: String) {
        TrackerHelper.sendButtonEvent(getString(R.string.tap_article_internal))
        val intent = Intent(activity, InternalWebViewActivity::class.java)
        intent.putExtra(InternalWebViewActivity.KEY_OPEN_URL, url)
        intent.putExtra(InternalWebViewActivity.KEY_RSS_TITLE, rssTitle)
        startActivity(intent)
    }

    override fun openExternalWebView(url: String) {
        TrackerHelper.sendButtonEvent(getString(R.string.tap_article_external))
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    override fun notifyListView() {
        articlesListAdapter.notifyDataSetChanged()
    }

    override fun finish() {
        listener.finish()
    }

    override fun showShareUi(url: String) {
        if (isAdded) TrackerHelper.sendButtonEvent(getString(R.string.share_article))
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, url)
        startActivity(intent)
    }

    override fun scrollTo(position: Int) {
        recyclerView.smoothScrollToPosition(position)
    }

    override fun showEmptyView() {
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        emptyView.text = getText(R.string.no_article)
    }

    override fun showNoSearchResult() {
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        emptyView.text = getText(R.string.no_search_result)
    }

    inner class SimpleItemRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val holder: RecyclerView.ViewHolder
            holder = when (viewType) {
                VIEW_TYPE_FOOTER -> {
                    val footer = LayoutInflater.from(parent.context)
                            .inflate(R.layout.footer_article_list_activity, parent, false)
                    FooterViewHolder(footer)
                }
                VIEW_TYPE_ARTICLE -> {
                    val view = LayoutInflater.from(parent.context)
                            .inflate(R.layout.articles_list, parent, false)
                    ArticleViewHolder(view)
                }
                else -> throw InvalidParameterException("Invalid view type for article list")
            }
            return holder
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ArticleViewHolder) {
                holder.mView.setOnClickListener { presenter.onListItemClicked(holder.getAdapterPosition()) }
                holder.mView.setOnLongClickListener {
                    presenter.onListItemLongClicked(holder.getAdapterPosition())
                    true
                }
                presenter.onBindViewHolder(holder, position)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return presenter.onGetItemViewType(position)
        }

        override fun getItemCount(): Int {
            return presenter.articleSize()
        }

        internal inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
        inner class ArticleViewHolder internal constructor(internal val mView: View) : RecyclerView.ViewHolder(mView) {
            internal val articleTitle: TextView = mView.findViewById(R.id.articleTitle) as TextView
            internal val articlePostedTime: TextView = mView.findViewById(R.id.articlePostedTime) as TextView
            internal val articlePoint: TextView = mView.findViewById(R.id.articlePoint) as TextView
            private val articleUrl: TextView = mView.findViewById(R.id.tv_articleUrl) as TextView
            private val feedTitleView: TextView = mView.findViewById(R.id.feedTitle) as TextView
            private val feedIconView: ImageView = mView.findViewById(R.id.iv_feed_icon) as ImageView

            fun setArticleTitle(title: String) {
                articleTitle.text = title
            }

            fun setArticleUrl(url: String) {
                articleUrl.text = url
            }

            fun setArticlePostedTime(time: String) {
                articlePostedTime.text = time
            }

            fun setNotGetPoint() {
                articlePoint.text = getString(R.string.not_get_hatena_point)
            }

            fun setArticlePoint(point: String) {
                articlePoint.text = point
            }

            fun hideRssInfo() {
                feedTitleView.visibility = View.GONE
                feedIconView.visibility = View.GONE
            }

            fun setRssTitle(title: String) {
                feedTitleView.text = title
                feedTitleView.setTextColor(Color.BLACK)
            }

            fun setRssIcon(path: String) {
                val bmp = BitmapFactory.decodeFile(path)
                feedIconView.setImageBitmap(bmp)
            }

            fun setDefaultRssIcon() {
                feedIconView.setImageResource(R.drawable.no_icon)
            }

            fun changeColorToRead() {
                articleTitle.setTextColor(Color.GRAY)
                articlePostedTime.setTextColor(Color.GRAY)
                articlePoint.setTextColor(Color.GRAY)
                feedTitleView.setTextColor(Color.GRAY)
            }

            fun changeColorToUnread() {
                articleTitle.setTextColor(Color.BLACK)
                articlePostedTime.setTextColor(Color.BLACK)
                articlePoint.setTextColor(Color.BLACK)
                feedTitleView.setTextColor(Color.BLACK)
            }
        }
    }
}