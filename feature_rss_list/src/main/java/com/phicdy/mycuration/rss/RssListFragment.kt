package com.phicdy.mycuration.rss

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.phicdy.mycuration.articlelist.ArticlesListActivity
import com.phicdy.mycuration.articlelist.FavoriteArticlesListActivity
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.RssListMode
import com.phicdy.mycuration.entity.RssUpdateIntervalCheckDate
import com.phicdy.mycuration.rss.databinding.FragmentRssListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RssListFragment : Fragment(), OnFeedListFragmentListener {

    private var _binding: FragmentRssListBinding? = null
    private val binding get() = _binding!!

    private val rssFeedListAdapter = RssListAdapter(this)

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
    lateinit var deleteRssActionCreator: DeleteRssActionCreator

    @Inject
    lateinit var editRssTitleActionCreator: EditRssTitleActionCreator

    @Inject
    lateinit var consumeRssListMessageActionCreator: ConsumeRssListMessageActionCreator

    private fun init(items: List<RssListItem>) {
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
            val state = rssListStateStore.state.value ?: return@setOnRefreshListener
            lifecycleScope.launchWhenStarted {
                updateAllRssListActionCreator.run(state.mode)
            }
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
            if (state.isInitializing) {
                binding.progressbar.visibility = View.VISIBLE
                hideRecyclerView()
            } else {
                binding.progressbar.visibility = View.GONE
                binding.swiperefreshlayout.isRefreshing = false
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
            if (state.isRefreshing) {
                binding.swiperefreshlayout.isRefreshing = true
            }
            state.messageList.firstOrNull()?.let { message ->
                when (message.type) {
                    RssListMessage.Type.SUCCEED_TO_EDIT_RSS -> showEditFeedSuccessToast()
                    RssListMessage.Type.ERROR_EMPTY_RSS_TITLE_EDIT -> showEditFeedTitleEmptyErrorToast()
                    RssListMessage.Type.ERROR_SAVE_RSS_TITLE -> showEditFeedFailToast()
                    RssListMessage.Type.SUCCEED_TO_DELETE_RSS -> showDeleteSuccessToast()
                    RssListMessage.Type.ERROR_DELETE_RSS -> showDeleteFailToast()
                }
                lifecycleScope.launchWhenStarted {
                    consumeRssListMessageActionCreator.run(message)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            fetchAllRssListActionCreator.run(RssListMode.UNREAD_ONLY)
        }
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            val state = rssListStateStore.state.value ?: return@launch
            launchUpdateAllRssListActionCreator.run(
                state.mode,
                RssUpdateIntervalCheckDate(Date())
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun changeRssListMode() {
        val state = rssListStateStore.state.value ?: return
        lifecycleScope.launchWhenStarted {
            changeRssListModeActionCreator.run(
                state.rawRssList,
                state.mode
            )
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
                    editRssTitleActionCreator.run(newTitle, rssId)
                }
            }.setNegativeButton(R.string.cancel, null).show()
    }

    override fun onDeleteRssClicked(rssId: Int, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_rss_alert)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launchWhenStarted {
                    val state = rssListStateStore.state.value ?: return@launchWhenStarted
                    lifecycleScope.launchWhenStarted {
                        deleteRssActionCreator.run(
                            rssId,
                            state.rawRssList,
                            state.mode
                        )
                    }
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

    private fun showEditFeedTitleEmptyErrorToast() {
        Toast.makeText(requireContext(), getString(R.string.empty_title), Toast.LENGTH_SHORT).show()
    }

    private fun showEditFeedFailToast() {
        Toast.makeText(
            requireContext(),
            getString(R.string.edit_rss_title_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showEditFeedSuccessToast() {
        Toast.makeText(
            requireContext(),
            getString(R.string.edit_rss_title_success),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showDeleteSuccessToast() {
        Toast.makeText(
            requireContext(),
            getString(R.string.finish_delete_rss_success),
            Toast.LENGTH_SHORT
        )
            .show()
    }

    private fun showDeleteFailToast() {
        Toast.makeText(
            requireContext(),
            getString(R.string.finish_delete_rss_fail),
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun RssListScreen(store: RSSListStateStore) {
    val value = store.state.observeAsState().value ?: return
    RssListScreen(
        item = value.item,
        rawRssList = value.rawRssList,
        mode = value.mode,
        isInitializing = value.isInitializing,
        isRefreshing = value.isRefreshing,
        messageList = value.messageList
    )
}

@Composable
fun RssListScreen(
    item: List<RssListItem>,
    rawRssList: List<Feed>,
    mode: RssListMode,
    isInitializing: Boolean,
    isRefreshing: Boolean,
    messageList: List<RssListMessage> = emptyList(),
    onRefresh: () -> Unit = {},
) {
    if (isInitializing) {
        CircularProgressIndicator()
    } else {
        if (item.isEmpty()) {
            RssEmptyText(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            )
        } else {
            SwipeRefreshRssList(
                isRefreshing = isRefreshing,
            )
        }
    }
}

@Composable
fun RssEmptyText(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = com.phicdy.mycuration.resource.R.string.no_rss_message),
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
fun SwipeRefreshRssList(
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    onRefresh: () -> Unit = {},
    items: List<RssListItem> = emptyList(),
    onRssClicked: () -> Unit = {}
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        LazyColumn {
            items(items) { item ->
                when (item) {
                    is RssListItem.All -> AllRssHeader(unreadCount = item.unreadCount)
                    is RssListItem.Content -> RssContent(
                        title = item.rssTitle,
                        unreadCount = item.unreadCount,
                        onRssClicked = onRssClicked
                    )
                    RssListItem.Favroite -> TODO()
                    is RssListItem.Footer -> TODO()
                }
            }
        }
    }
}

@Composable
fun AllRssHeader(unreadCount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_view_headline_black_24dp),
            contentDescription = ""
        )
        Text(
            text = stringResource(id = com.phicdy.mycuration.resource.R.string.all),
            fontSize = 18.sp,
            modifier = Modifier
                .padding(start = 16.dp)
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Text(
            text = unreadCount.toString(),
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Composable
fun RssContent(
    @DrawableRes iconDrawable: Int = com.phicdy.mycuration.resource.R.drawable.ic_rss,
    title: String,
    unreadCount: Int,
    onRssClicked: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRssClicked() }
    ) {
        Image(
            painter = painterResource(id = iconDrawable),
            modifier = Modifier
                .width(32.dp)
                .height(32.dp)
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
            contentDescription = ""
        )
        Text(
            text = title,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(start = 16.dp)
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Text(
            text = unreadCount.toString(),
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Preview
@Composable
fun PreviewLoadingRssListScreen() {
    RssListScreen(
        item = emptyList(),
        rawRssList = emptyList(),
        mode = RssListMode.UNREAD_ONLY,
        isInitializing = true,
        isRefreshing = false,
        messageList = emptyList()
    )
}

@Preview
@Composable
fun PreviewEmptyRssListScreen() {
    RssListScreen(
        item = emptyList(),
        rawRssList = emptyList(),
        mode = RssListMode.UNREAD_ONLY,
        isInitializing = false,
        isRefreshing = false,
        messageList = emptyList()
    )
}

@Preview
@Composable
fun PreviewAllRssHeader() {
    AllRssHeader(unreadCount = 10)
}

@Preview
@Composable
fun PreviewRssContent() {
    RssContent(title = "title", unreadCount = 10)
}
