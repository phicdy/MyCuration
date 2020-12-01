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
        rssFeedListAdapter = RssListAdapter(mListener)
        binding.rvRss.visibility = View.VISIBLE
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
            launchWhenLoaded { state -> updateAllRssListActionCreator.run(state.mode, RssUpdateIntervalCheckDate(Date())) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRssListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(binding.rvRss)
        setAllListener()
        rssListStateStore.state.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                RssListState.Loading -> {
                    binding.progressbar.visibility = View.VISIBLE
                    hideRecyclerView()
                }
                is RssListState.Loaded -> {
                    binding.progressbar.visibility = View.GONE
                    if (state.item.isEmpty()) {
                        hideRecyclerView()
                        showEmptyView()
                    } else {
                        init(state.item)
                    }
                }
            }
        })
        rssListUpdateStateStore.state.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                RssListUpdateState.Started -> binding.srlContainer.isRefreshing = true
                is RssListUpdateState.Updating -> {
                    rssFeedListAdapter.submitList(state.rss)
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
            throw ClassCastException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            val mode = when (val value = rssListStateStore.state.value) {
                is RssListState.Loading, null -> RssListMode.UNREAD_ONLY
                is RssListState.Loaded -> value.mode
            }
            updateAllRssListActionCreator.run(mode, RssUpdateIntervalCheckDate(Date()))
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
        launchWhenLoaded { state -> changeRssTitleActionCreator.run(rssId, newTitle, state) }
    }

    fun removeRss(rssId: Int) {
        launchWhenLoaded { state -> deleteRssActionCreator.run(rssId, state) }
    }

    fun changeRssListMode() {
        launchWhenLoaded { state -> changeRssListModeActionCreator.run(state) }
    }

    private fun launchWhenLoaded(block: suspend (RssListState.Loaded) -> Unit) {
        when (val value = rssListStateStore.state.value) {
            is RssListState.Loaded ->
                viewLifecycleOwner.lifecycleScope.launch {
                    block.invoke(value)
                }
            RssListState.Loading, null -> return
        }
    }

    interface OnFeedListFragmentListener {
        fun onListClicked(feedId: Int)
        fun onEditRssClicked(rssId: Int, feedTitle: String)
        fun onDeleteRssClicked(rssId: Int, position: Int)
        fun onAllUnreadClicked()
        fun onFavoriteClicked()
        fun onFooterClicked()
    }
}
