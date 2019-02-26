package com.phicdy.mycuration.presentation.view.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.di.GlideApp
import com.phicdy.mycuration.presentation.presenter.RssListPresenter
import com.phicdy.mycuration.presentation.view.RssItemView
import com.phicdy.mycuration.presentation.view.RssListView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.ext.android.bindScope
import org.koin.android.scope.ext.android.getOrCreateScope
import org.koin.core.parameter.parametersOf
import java.security.InvalidParameterException
import kotlin.coroutines.CoroutineContext

class RssListFragment : Fragment(), RssListView, CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val presenter: RssListPresenter by inject { parametersOf(this) }
    private lateinit var tvAllUnreadArticleCount: TextView
    private lateinit var allUnread: ConstraintLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView

    private lateinit var rssFeedListAdapter: RssFeedListAdapter
    private var mListener: OnFeedListFragmentListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindScope(getOrCreateScope("rss_list"))
        presenter.create()
    }

    override fun onResume() {
        super.onResume()
        launch(context = coroutineContext) {
            presenter.resume()
        }
    }

    override fun showEditTitleDialog(rssId: Int, feedTitle: String) {
        mListener?.onListLongClicked(rssId, feedTitle)
    }

    override fun setRefreshing(doScroll: Boolean) {
        swipeRefreshLayout.isRefreshing = doScroll
    }

    override fun init(feeds: ArrayList<Feed>) {
        rssFeedListAdapter = RssFeedListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = rssFeedListAdapter
        rssFeedListAdapter.notifyDataSetChanged()
    }

    override fun setTotalUnreadCount(count: Int) {
        tvAllUnreadArticleCount.text = count.toString()
    }

    override fun onRefreshCompleted() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun showDeleteSuccessToast() {
        Toast.makeText(activity, getString(R.string.finish_delete_rss_success), Toast.LENGTH_SHORT).show()
    }

    override fun showDeleteFailToast() {
        Toast.makeText(activity, getString(R.string.finish_delete_rss_fail), Toast.LENGTH_SHORT).show()
    }

    override fun showAddFeedSuccessToast() {
        Toast.makeText(activity, R.string.add_rss_success, Toast.LENGTH_SHORT).show()
    }

    override fun showGenericAddFeedErrorToast() {
        Toast.makeText(activity, R.string.add_rss_error_generic, Toast.LENGTH_SHORT).show()
    }

    override fun showInvalidUrlAddFeedErrorToast() {
        Toast.makeText(activity, R.string.add_rss_error_invalid_url, Toast.LENGTH_SHORT).show()
    }

    override fun notifyDataSetChanged() {
        rssFeedListAdapter.notifyDataSetChanged()
    }

    override fun showAllUnreadView() {
        allUnread.visibility = View.VISIBLE
    }

    override fun hideAllUnreadView() {
        allUnread.visibility = View.GONE
    }

    override fun showRecyclerView() {
        recyclerView.visibility = View.VISIBLE
    }

    override fun hideRecyclerView() {
        recyclerView.visibility = View.GONE
    }

    override fun showEmptyView() {
        emptyView.visibility = View.VISIBLE
    }

    override fun hideEmptyView() {
        emptyView.visibility = View.GONE
    }

    override fun showDeleteFeedAlertDialog(position: Int) {
        AlertDialog.Builder(activity)
                .setTitle(R.string.delete_rss_alert)
                .setPositiveButton(R.string.delete) { _, _ ->
                    launch(context = coroutineContext) {
                        presenter.onDeleteOkButtonClicked(position)
                    }
                }
                .setNegativeButton(R.string.cancel, null).show()
    }

    override fun onPause() {
        super.onPause()
        presenter.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun setAllListener() {
        swipeRefreshLayout.setOnRefreshListener {
            launch(context = coroutineContext) {
                presenter.onRefresh()
            }
        }
        allUnread.setOnClickListener {
            mListener?.onAllUnreadClicked()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rss_list, container, false)
        recyclerView = view.findViewById(R.id.rv_rss)
        emptyView = view.findViewById(R.id.emptyView) as TextView
        swipeRefreshLayout = view.findViewById(R.id.srl_container) as androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        tvAllUnreadArticleCount = view.findViewById(R.id.allUnreadCount) as TextView
        allUnread = view.findViewById(R.id.cl_all_unread) as ConstraintLayout
        registerForContextMenu(recyclerView)
        setAllListener()
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as OnFeedListFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement OnFragmentInteractionListener")
        }

    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    fun updateFeedTitle(rssId: Int, newTitle: String) {
        presenter.updateFeedTitle(rssId, newTitle)
    }

    interface OnFeedListFragmentListener {
        fun onListClicked(feedId: Int)
        fun onListLongClicked(rssId: Int, feedTitle: String)
        fun onAllUnreadClicked()
    }

    private inner class RssFeedListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                VIEW_TYPE_RSS -> {
                    val view = LayoutInflater.from(parent.context)
                            .inflate(R.layout.feeds_list, parent, false)
                    RssViewHolder(view)
                }
                VIEW_TYPE_FOOTER -> {
                    val view = LayoutInflater.from(parent.context)
                            .inflate(R.layout.list_item_rss_footer, parent, false)
                    RssFooterView(view)
                }
                else -> throw InvalidParameterException("Invalid view type")
            }
        }

        override fun getItemCount(): Int {
            return presenter.getItemCount()
        }

        override fun getItemViewType(position: Int): Int {
            return presenter.onGetItemViewType(position)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is RssViewHolder) {
                holder.itemView.setOnClickListener {
                    presenter.onRssItemClicked(position, mListener)
                }
                holder.itemView.setOnCreateContextMenuListener { menu, _, _ ->
                    menu.add(0, EDIT_FEED_TITLE_MENU_ID, 0, R.string.edit_rss_title).setOnMenuItemClickListener {
                        presenter.onEditFeedMenuClicked(position)
                        true
                    }
                    menu.add(0, DELETE_FEED_MENU_ID, 1, R.string.delete_rss).setOnMenuItemClickListener {
                        presenter.onDeleteFeedMenuClicked(position)
                        true
                    }
                }
                presenter.onBindRssViewHolder(position, holder)
            } else if (holder is RssFooterView) {
                holder.itemView.setOnClickListener {
                    presenter.onRssFooterClicked()
                }
                presenter.onBindRssFooterViewHolder(holder)
            }
        }

        private inner class RssViewHolder(
                itemView: View
        ) : RecyclerView.ViewHolder(itemView), RssItemView.Content {
            private val feedIcon = itemView.findViewById(R.id.feedIcon) as ImageView
            private val feedTitle = itemView.findViewById(R.id.feedTitle) as TextView
            private val feedCount = itemView.findViewById(R.id.feedCount) as TextView

            override fun showDefaultIcon() {
                feedIcon.setImageResource(R.drawable.ic_rss)
            }

            override fun showIcon(iconPath: String) {
                GlideApp.with(this@RssListFragment)
                        .load(iconPath)
                        .placeholder(R.drawable.ic_rss)
                        .circleCrop()
                        .error(R.drawable.ic_rss)
                        .into(feedIcon)
            }

            override fun updateTitle(title: String) {
                feedTitle.text = title
            }

            override fun updateUnreadCount(count: String) {
                feedCount.text = count
            }
        }

        private inner class RssFooterView(
                itemView: View
        ) : RecyclerView.ViewHolder(itemView), RssItemView.Footer {
            internal val title = itemView.findViewById<TextView>(R.id.tv_rss_footer_title)
            override fun showAllView() {
                title.setText(R.string.show_all_rsses)
            }

            override fun showHideView() {
                title.setText(R.string.hide_rsses)
            }
        }
    }

    companion object {

        private const val DELETE_FEED_MENU_ID = 1000
        private const val EDIT_FEED_TITLE_MENU_ID = 1001

        const val VIEW_TYPE_RSS = 0
        const val VIEW_TYPE_FOOTER = 1
    }
}
