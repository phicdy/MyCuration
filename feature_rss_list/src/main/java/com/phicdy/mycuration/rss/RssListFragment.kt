package com.phicdy.mycuration.rss

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.phicdy.mycuration.articlelist.ArticlesListActivity
import com.phicdy.mycuration.articlelist.FavoriteArticlesListActivity
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.RssListMode
import com.phicdy.mycuration.entity.RssUpdateIntervalCheckDate
import com.phicdy.mycuration.rss.databinding.FragmentRssListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class RssListFragment : Fragment(), OnFeedListFragmentListener {

    private var _binding: FragmentRssListBinding? = null
    private val binding get() = _binding!!

    private lateinit var rssFeedListAdapter: RssListAdapter
    private var listener: OnFeedListFragmentListener? = null

    @Inject
    lateinit var fetchAllRssListActionCreator: FetchAllRssListActionCreator
    private val rssListStateStore: RSSListStateStore by viewModels()

    @Inject
    lateinit var updateAllRssListActionCreator: UpdateAllRssActionCreator

    @Inject
    lateinit var launchUpdateAllRssListActionCreator: LaunchUpdateAllRssActionCreator

    @Inject
    lateinit var changeRssListModeActionCreator: ChangeRssListModeActionCreator

    @Inject
    lateinit var changeRssTitleActionCreator: ChangeRssTitleActionCreator

    @Inject
    lateinit var deleteRssActionCreator: DeleteRssActionCreator

    @Inject
    lateinit var presenter: RssListFragmentPresenter

    private fun init(items: List<RssListItem>) {
        rssFeedListAdapter = RssListAdapter(listener)
        binding.recyclerview.visibility = View.VISIBLE
        binding.recyclerview.layoutManager = LinearLayoutManager(activity)
        binding.recyclerview.adapter = rssFeedListAdapter
        rssFeedListAdapter.submitList(items)
    }

    private fun hideRecyclerView() {
        binding.recyclerview.visibility = View.GONE
    }

    private fun showEmptyView() {
        binding.emptyView.visibility = View.VISIBLE
    }

    private fun hideEmptyView() {
        binding.emptyView.visibility = View.GONE
    }

    private fun setAllListener() {
        binding.swiperefreshlayout.setOnRefreshListener {
            launchWhenInitializedOrUpdated { _, mode -> updateAllRssListActionCreator.run(mode) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRssListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(binding.recyclerview)
        setAllListener()
        rssListStateStore.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                RssListState.Initializing -> {
                    binding.progressbar.visibility = View.VISIBLE
                    hideRecyclerView()
                }
                is RssListState.Initialized -> {
                    binding.progressbar.visibility = View.GONE
                    if (state.item.isEmpty()) {
                        hideRecyclerView()
                        showEmptyView()
                    } else {
                        init(state.item)
                        hideEmptyView()
                    }
                    viewLifecycleOwner.lifecycleScope.launch {
                        launchUpdateAllRssListActionCreator.run(
                            state.mode,
                            RssUpdateIntervalCheckDate(Date())
                        )
                    }
                }
                RssListState.StartUpdate -> {
                    binding.swiperefreshlayout.isRefreshing = true
                }
                is RssListState.Updated -> {
                    binding.progressbar.visibility = View.GONE
                    if (state.item.isEmpty()) {
                        hideRecyclerView()
                        showEmptyView()
                    } else {
                        init(state.item)
                        hideEmptyView()
                    }
                    binding.swiperefreshlayout.isRefreshing = false
                }
                RssListState.FailedToUpdate -> {
                    binding.swiperefreshlayout.isRefreshing = false
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            fetchAllRssListActionCreator.run(RssListMode.UNREAD_ONLY)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as OnFeedListFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            when (val state = rssListStateStore.state.value) {
                is RssListState.Initialized -> {
                    launchUpdateAllRssListActionCreator.run(state.mode, RssUpdateIntervalCheckDate(Date()))
                }
                is RssListState.Updated -> {
                    launchUpdateAllRssListActionCreator.run(state.mode, RssUpdateIntervalCheckDate(Date()))
                }
                RssListState.FailedToUpdate -> {
                    launchUpdateAllRssListActionCreator.run(RssListMode.UNREAD_ONLY, RssUpdateIntervalCheckDate(Date()))
                }
                RssListState.StartUpdate,
                RssListState.Initializing -> {
                    // loading
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateFeedTitle(rssId: Int, newTitle: String) {
        launchWhenInitializedOrUpdated { rawRssList, mode -> changeRssTitleActionCreator.run(rssId, newTitle, rawRssList, mode) }
    }

    fun removeRss(rssId: Int) {
        launchWhenInitializedOrUpdated { rawRssList, mode -> deleteRssActionCreator.run(rssId, rawRssList, mode) }
    }

    fun changeRssListMode() {
        launchWhenInitializedOrUpdated { rawRssList, mode -> changeRssListModeActionCreator.run(rawRssList, mode) }
    }

    private fun launchWhenInitializedOrUpdated(block: suspend (List<Feed>, RssListMode) -> Unit) {
        when (val value = rssListStateStore.state.value) {
            is RssListState.Updated -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    block.invoke(value.rawRssList, value.mode)
                }
            }
            is RssListState.Initialized -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    block.invoke(value.rawRssList, value.mode)
                }
            }
            RssListState.Initializing, null -> return
            RssListState.StartUpdate -> return
            RssListState.FailedToUpdate -> return
        }
    }

    fun reload() {
        viewLifecycleOwner.lifecycleScope.launch {
            fetchAllRssListActionCreator.run(RssListMode.UNREAD_ONLY)
        }
    }

    override fun onListClicked(feedId: Int) {
        startActivity(ArticlesListActivity.createIntent(requireContext(), feedId))
    }

    override fun onEditRssClicked(rssId: Int, feedTitle: String) {
        val addView = View.inflate(requireContext(), R.layout.edit_feed_title, null)
        val editTitleView = addView.findViewById(R.id.editFeedTitle) as EditText
        editTitleView.setText(feedTitle)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.edit_rss_title)
            .setView(addView)
            .setPositiveButton(R.string.save) { _, _ ->
                val newTitle = editTitleView.text.toString()
                lifecycleScope.launchWhenStarted {
                    presenter.onEditFeedOkButtonClicked(newTitle, rssId)
                }
            }.setNegativeButton(R.string.cancel, null).show()
    }

    override fun onDeleteRssClicked(rssId: Int, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_rss_alert)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launchWhenStarted {
                    presenter.onDeleteOkButtonClicked(rssId)
                }
            }
            .setNegativeButton(R.string.cancel, null).show()
    }

    override fun onAllUnreadClicked() {
        val intent = Intent(requireContext(), ArticlesListActivity::class.java)
        startActivity(intent)
    }

    override fun onFavoriteClicked() {
        startActivity(FavoriteArticlesListActivity.createIntent(requireContext()))
    }

    override fun onFooterClicked() {
        changeRssListMode()
    }

    fun showEditFeedTitleEmptyErrorToast() {
        Toast.makeText(requireContext(), getString(R.string.empty_title), Toast.LENGTH_SHORT).show()
    }

    fun showEditFeedFailToast() {
        Toast.makeText(
            requireContext(),
            getString(R.string.edit_rss_title_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun showEditFeedSuccessToast() {
        Toast.makeText(
            requireContext(),
            getString(R.string.edit_rss_title_success),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun showDeleteSuccessToast() {
        Toast.makeText(
            requireContext(),
            getString(R.string.finish_delete_rss_success),
            Toast.LENGTH_SHORT
        )
            .show()
    }

    fun showDeleteFailToast() {
        Toast.makeText(
            requireContext(),
            getString(R.string.finish_delete_rss_fail),
            Toast.LENGTH_SHORT
        ).show()
    }
}
