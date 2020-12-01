package com.phicdy.mycuration.rss

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.phicdy.mycuration.entity.RssListMode
import com.phicdy.mycuration.entity.RssUpdateIntervalCheckDate
import com.phicdy.mycuration.rss.databinding.FragmentRssListBinding
import kotlinx.coroutines.launch
import org.koin.android.scope.currentScope
import java.util.Date

class RssListFragment : Fragment() {

    private var _binding: FragmentRssListBinding? = null
    private val binding get() = _binding!!

    private lateinit var rssFeedListAdapter: RssListAdapter
    private var mListener: OnFeedListFragmentListener? = null

    private val fetchAllRssListActionCreator: FetchAllRssListActionCreator by currentScope.inject()
    private val rssListStateStore: RSSListStateStore by currentScope.inject()

    private val updateAllRssListActionCreator: UpdateAllRssActionCreator by currentScope.inject()
    private val rssListUpdateStateStore: RssListUpdateStateStore by currentScope.inject()

    private val changeRssListModeActionCreator: ChangeRssListModeActionCreator by currentScope.inject()

    private val changeRssTitleActionCreator: ChangeRssTitleActionCreator by currentScope.inject()

    private val deleteRssActionCreator: DeleteRssActionCreator by currentScope.inject()

    private fun init(items: List<RssListItem>) {
        rssFeedListAdapter = RssListAdapter(viewLifecycleOwner.lifecycleScope, changeRssListModeActionCreator, rssListStateStore, mListener)
        binding.rvRss.layoutManager = LinearLayoutManager(activity)
        binding.rvRss.adapter = rssFeedListAdapter
        rssFeedListAdapter.submitList(items)
    }

    private fun onRefreshCompleted() {
        binding.srlContainer.isRefreshing = false
    }

    private fun hideRecyclerView() {
        binding.rvRss.visibility = View.GONE
    }

    private fun showEmptyView() {
        binding.emptyView.visibility = View.VISIBLE
    }

    private fun setAllListener() {
        binding.srlContainer.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch {
                rssListStateStore.state.value?.let { value ->
                    updateAllRssListActionCreator.run(value.mode, RssUpdateIntervalCheckDate(Date()))
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRssListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(binding.rvRss)
        setAllListener()
        rssListStateStore.state.observe(viewLifecycleOwner, Observer {
            if (it.item.isEmpty()) {
                hideRecyclerView()
                showEmptyView()
            } else {
                init(it.item)
            }
        })
        rssListUpdateStateStore.state.observe(viewLifecycleOwner, Observer {
            when (it) {
                RssListUpdateState.Started -> binding.srlContainer.isRefreshing = true
                is RssListUpdateState.Updating -> {
                    rssFeedListAdapter.submitList(it.rss)
                }
                RssListUpdateState.Finished -> {
                    onRefreshCompleted()
                }
                else -> {
                }
            }
        })
        viewLifecycleOwner.lifecycleScope.launch {
            fetchAllRssListActionCreator.run(RssListMode.UNREAD_ONLY)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as OnFeedListFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement OnFragmentInteractionListener")
        }

    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            rssListStateStore.state.value?.let { value ->
                updateAllRssListActionCreator.run(value.mode, RssUpdateIntervalCheckDate(Date()))
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateFeedTitle(rssId: Int, newTitle: String) {
        rssListStateStore.state.value?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                changeRssTitleActionCreator.run(rssId, newTitle, it)
            }
        }
    }

    fun removeRss(rssId: Int) {
        rssListStateStore.state.value?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                deleteRssActionCreator.run(rssId, it)
            }
        }
    }

    interface OnFeedListFragmentListener {
        fun onListClicked(feedId: Int)
        fun onEditRssClicked(rssId: Int, feedTitle: String)
        fun onDeleteRssClicked(rssId: Int, position: Int)
        fun onAllUnreadClicked()
        fun onFavoriteClicked()
    }
}
