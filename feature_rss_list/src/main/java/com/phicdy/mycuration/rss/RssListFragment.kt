package com.phicdy.mycuration.rss

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.phicdy.mycuration.entity.RssListMode
import kotlinx.coroutines.launch
import org.koin.android.scope.currentScope
import org.koin.core.parameter.parametersOf

class RssListFragment : Fragment(), RssListView {

    private val presenter: RssListPresenter by currentScope.inject { parametersOf(this) }
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView

    private lateinit var rssFeedListAdapter: RssListAdapter
    private var mListener: OnFeedListFragmentListener? = null

    private val fetchAllRssListActionCreator: FetchAllRssListActionCreator by currentScope.inject()
    private val rssListStateStore: RSSListStateStore by currentScope.inject()

    override fun setRefreshing(doScroll: Boolean) {
        swipeRefreshLayout.isRefreshing = doScroll
    }

    override fun init(items: List<RssListItem>) {
        rssFeedListAdapter = RssListAdapter(presenter, mListener)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = rssFeedListAdapter
        rssFeedListAdapter.submitList(items)
    }

    override fun onRefreshCompleted() {
        swipeRefreshLayout.isRefreshing = false
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

    override fun notifyDataSetChanged(items: List<RssListItem>) {
        rssFeedListAdapter.submitList(items)
    }

    override fun hideRecyclerView() {
        recyclerView.visibility = View.GONE
    }

    override fun showEmptyView() {
        emptyView.visibility = View.VISIBLE
    }

    override fun showDeleteFeedAlertDialog(rssId: Int, position: Int) {
        mListener?.onDeleteRssClicked(rssId, position)
    }

    override fun onPause() {
        super.onPause()
        presenter.pause()
    }

    private fun setAllListener() {
        swipeRefreshLayout.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch {
                presenter.onRefresh()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rss_list, container, false)
        recyclerView = view.findViewById(R.id.rv_rss)
        emptyView = view.findViewById(R.id.emptyView) as TextView
        swipeRefreshLayout = view.findViewById(R.id.srl_container) as SwipeRefreshLayout
        registerForContextMenu(recyclerView)
        setAllListener()
        rssListStateStore.state.observe(viewLifecycleOwner, Observer {
            if (it.item.isEmpty()) {
                hideRecyclerView()
                showEmptyView()
            } else {
                init(it.item)
            }
        })
        viewLifecycleOwner.lifecycleScope.launch {
            presenter.onCreateView()
            fetchAllRssListActionCreator.run(RssListMode.UNREAD_ONLY)
        }
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

    fun removeRss(rssId: Int) {
        presenter.removeRss(rssId)
    }

    interface OnFeedListFragmentListener {
        fun onListClicked(feedId: Int)
        fun onEditRssClicked(rssId: Int, feedTitle: String)
        fun onDeleteRssClicked(rssId: Int, position: Int)
        fun onAllUnreadClicked()
        fun onFavoriteClicked()
    }
}
