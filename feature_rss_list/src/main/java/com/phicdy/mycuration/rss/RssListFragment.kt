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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.phicdy.mycuration.articlelist.ArticlesListActivity
import com.phicdy.mycuration.articlelist.FavoriteArticlesListActivity
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.RssListMode
import com.phicdy.mycuration.entity.RssUpdateIntervalCheckDate
import com.phicdy.mycuration.resource.MyCurationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class RssListFragment : Fragment() {

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

    @Inject
    lateinit var showDropdownMenuActionCreator: ShowDropdownMenuActionCreator

    @Inject
    lateinit var hideDropdownMenuActionCreator: HideDropdownMenuActionCreator

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MyCurationTheme {
                    RssListScreen(
                            store = rssListStateStore,
                            onRefresh = {
                                val state = rssListStateStore.state.value ?: return@RssListScreen
                                lifecycleScope.launchWhenStarted {
                                    updateAllRssListActionCreator.run(state.mode)
                                }
                            },
                            onRssClicked = { id ->
                                startActivity(ArticlesListActivity.createIntent(requireContext(), id))
                            },
                            onFavoriteClicked = {
                                startActivity(FavoriteArticlesListActivity.createIntent(requireContext()))
                            },
                            onFooterClicked = {
                                changeRssListMode()
                            }
                },
                onRssClicked = { id ->
                    startActivity(ArticlesListActivity.createIntent(requireContext(), id))
                },
                onRssLongClicked = { id ->
                    lifecycleScope.launchWhenStarted {
                        showDropdownMenuActionCreator.run(id)
                    }
                },
                onFavoriteClicked = {
                    startActivity(FavoriteArticlesListActivity.createIntent(requireContext()))
                },
                onFooterClicked = {
                    changeRssListMode()
                },
                onEditTitleMenuClicked = { id ->
                },
                onDeleteMenuClicked = { id ->
                },
                onDismissDropdownMenu = {
                    lifecycleScope.launchWhenStarted {
                        hideDropdownMenuActionCreator.run()
                    }
                }
                )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        registerForContextMenu(binding.recyclerview)
//        setAllListener()
//        rssListStateStore.state.observe(viewLifecycleOwner) { state ->
//            if (state.isInitializing) {
//                binding.progressbar.visibility = View.VISIBLE
//                hideRecyclerView()
//            } else {
//                binding.progressbar.visibility = View.GONE
//                binding.swiperefreshlayout.isRefreshing = false
//                if (state.item.isEmpty()) {
//                    hideRecyclerView()
//                    showEmptyView()
//                } else {
//                    init(state.item)
//                    hideEmptyView()
//                }
//                viewLifecycleOwner.lifecycleScope.launch {
//                    launchUpdateAllRssListActionCreator.run(
//                        state.mode,
//                        RssUpdateIntervalCheckDate(Date())
//                    )
//                }
//            }
//            if (state.isRefreshing) {
//                binding.swiperefreshlayout.isRefreshing = true
//            }
//            state.messageList.firstOrNull()?.let { message ->
//                when (message.type) {
//                    RssListMessage.Type.SUCCEED_TO_EDIT_RSS -> showEditFeedSuccessToast()
//                    RssListMessage.Type.ERROR_EMPTY_RSS_TITLE_EDIT -> showEditFeedTitleEmptyErrorToast()
//                    RssListMessage.Type.ERROR_SAVE_RSS_TITLE -> showEditFeedFailToast()
//                    RssListMessage.Type.SUCCEED_TO_DELETE_RSS -> showDeleteSuccessToast()
//                    RssListMessage.Type.ERROR_DELETE_RSS -> showDeleteFailToast()
//                }
//                lifecycleScope.launchWhenStarted {
//                    consumeRssListMessageActionCreator.run(message)
//                }
//            }
//        }
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

    fun onEditRssClicked(rssId: Int, feedTitle: String) {
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

    fun onDeleteRssClicked(rssId: Int, position: Int) {
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

    fun onAllUnreadClicked() {
        val intent = Intent(requireContext(), ArticlesListActivity::class.java)
        startActivity(intent)
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
fun RssListScreen(
        store: RSSListStateStore,
        onRefresh: () -> Unit = {},
        onRssClicked: (Int) -> Unit = {},
        onRssLongClicked: (Int) -> Unit = {},
        onFavoriteClicked: () -> Unit = {},
        onFooterClicked: () -> Unit = {},
        onEditTitleMenuClicked: (Int) -> Unit = {},
        onDeleteMenuClicked: (Int) -> Unit = {},
        onDismissDropdownMenu: () -> Unit = {},
) {
    val value = store.state.observeAsState().value ?: return
    RssListScreen(
            items = value.item,
            rawRssList = value.rawRssList,
            mode = value.mode,
            isInitializing = value.isInitializing,
            isRefreshing = value.isRefreshing,
            messageList = value.messageList,
            showDropdownMenuId = value.showDropdownMenuId,
            onRefresh = onRefresh,
            onRssClicked = onRssClicked,
            onRssLongClicked = onRssLongClicked,
            onFavoriteClicked = onFavoriteClicked,
            onFooterClicked = onFooterClicked,
            onEditTitleMenuClicked = onEditTitleMenuClicked,
            onDeleteMenuClicked = onDeleteMenuClicked,
            onDismissDropdownMenu = onDismissDropdownMenu,
    )
}

@Composable
fun RssListScreen(
        items: List<RssListItem> = emptyList(),
        rawRssList: List<Feed>,
        mode: RssListMode,
        isInitializing: Boolean,
        isRefreshing: Boolean,
        messageList: List<RssListMessage> = emptyList(),
        showDropdownMenuId: Int? = null,
        onRefresh: () -> Unit = {},
        onRssClicked: (Int) -> Unit = {},
        onRssLongClicked: (Int) -> Unit = {},
        onFavoriteClicked: () -> Unit = {},
        onFooterClicked: () -> Unit = {},
        onEditTitleMenuClicked: (Int) -> Unit = {},
        onDeleteMenuClicked: (Int) -> Unit = {},
        onDismissDropdownMenu: () -> Unit = {},
) {
    if (isInitializing) {
        CircularProgressIndicator()
    } else {
        if (items.isEmpty()) {
            RssEmptyText(
                modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
            )
        } else {
            SwipeRefreshRssList(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    items = items,
                    showDropdownMenuId = showDropdownMenuId,
                    onRssClicked = onRssClicked,
                    onRssLongClicked = onRssLongClicked,
                    onFavoriteClicked = onFavoriteClicked,
                    onFooterClicked = onFooterClicked,
                    onEditTitleMenuClicked = onEditTitleMenuClicked,
                    onDeleteMenuClicked = onDeleteMenuClicked,
                    onDismissDropdownMenu = onDismissDropdownMenu,
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
        showDropdownMenuId: Int?,
        onRssClicked: (Int) -> Unit = {},
        onRssLongClicked: (Int) -> Unit = {},
        onFavoriteClicked: () -> Unit = {},
        onFooterClicked: () -> Unit = {},
        onEditTitleMenuClicked: (Int) -> Unit = {},
        onDeleteMenuClicked: (Int) -> Unit = {},
        onDismissDropdownMenu: () -> Unit = {},
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
                            id = item.rssId,
                            title = item.rssTitle,
                            unreadCount = item.unreadCount,
                            onRssClicked = onRssClicked,
                            onRssLongClicked = onRssLongClicked,
                            showDropdownMenu = item.rssId == showDropdownMenuId,
                            onEditTitleMenuClicked = onEditTitleMenuClicked,
                            onDeleteMenuClicked = onDeleteMenuClicked,
                            onDismissDropdownMenu = onDismissDropdownMenu,
                    )
                    RssListItem.Favroite -> FavoriteContent(
                        onFavoriteClicked = onFavoriteClicked,
                    )
                    is RssListItem.Footer -> Footer(
                        footerState = item.state,
                        onFooterClicked = onFooterClicked
                    )
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
            modifier = Modifier
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
            contentDescription = ""
        )
        Text(
            text = stringResource(id = com.phicdy.mycuration.resource.R.string.all),
            fontSize = 18.sp,
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
@OptIn(ExperimentalFoundationApi::class)
fun RssContent(
        id: Int,
        @DrawableRes iconDrawable: Int = com.phicdy.mycuration.resource.R.drawable.ic_rss,
        title: String,
        unreadCount: Int,
        onRssClicked: (Int) -> Unit = {},
        onRssLongClicked: (Int) -> Unit = {},
        showDropdownMenu: Boolean,
        onEditTitleMenuClicked: (Int) -> Unit = {},
        onDeleteMenuClicked: (Int) -> Unit = {},
        onDismissDropdownMenu: () -> Unit = {},
) {
    Box {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                                onClick = { onRssClicked(id) },
                                onLongClick = { onRssLongClicked(id) }
                        )
        ) {
            Image(
                    painter = painterResource(id = iconDrawable),
                    modifier = Modifier
                            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 16.dp)
                            .width(32.dp)
                            .height(32.dp),
                    contentDescription = ""
            )
            Text(
                    text = title,
                    fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.weight(1.0f))
            Text(
                    text = unreadCount.toString(),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 16.dp)
            )
        }
        DropdownMenu(
                expanded = showDropdownMenu,
                onDismissRequest = { onDismissDropdownMenu() },
        ) {
            DropdownMenuItem(onClick = { onEditTitleMenuClicked(id) }) {
                Text(text = stringResource(id = R.string.edit_rss_title))
            }
            DropdownMenuItem(onClick = { onDeleteMenuClicked(id) }) {
                Text(text = stringResource(id = R.string.delete))
            }
        }
    }
}

@Composable
fun FavoriteContent(
    onFavoriteClicked: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
                .fillMaxWidth()
                .clickable { onFavoriteClicked() }
    ) {
        Image(
            painter = painterResource(id = com.phicdy.mycuration.resource.R.drawable.ic_favorite_off),
            modifier = Modifier
                    .width(32.dp)
                    .height(32.dp)
                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
            contentDescription = ""
        )
        Text(
            text = stringResource(id = com.phicdy.mycuration.resource.R.string.favorite),
            fontSize = 16.sp,
            modifier = Modifier
                .padding(start = 16.dp)
        )
    }
}

@Composable
fun Footer(
    footerState: RssListFooterState,
    onFooterClicked: () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val text = when (footerState) {
            RssListFooterState.ALL -> stringResource(id = com.phicdy.mycuration.resource.R.string.hide_rsses)
            RssListFooterState.UNREAD_ONLY -> stringResource(id = com.phicdy.mycuration.resource.R.string.show_all_rsses)
        }
        Text(
                text = text,
                fontSize = 16.sp,
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 72.dp, top = 20.dp, bottom = 20.dp)
                        .clickable { onFooterClicked() }
        )
        Divider(modifier = Modifier.padding(start = 72.dp))
    }
}

@Preview(name = "Loading Screen")
@Composable
fun PreviewLoadingRssListScreen() {
    RssListScreen(
        items = emptyList(),
        rawRssList = emptyList(),
        mode = RssListMode.UNREAD_ONLY,
        isInitializing = true,
        isRefreshing = false,
        messageList = emptyList()
    )
}

@Preview(name = "Empty Screen")
@Composable
fun PreviewEmptyRssListScreen() {
    RssListScreen(
        items = emptyList(),
        rawRssList = emptyList(),
        mode = RssListMode.UNREAD_ONLY,
        isInitializing = false,
        isRefreshing = false,
        messageList = emptyList()
    )
}

@Preview(name = "Header")
@Composable
fun PreviewAllRssHeader() {
    AllRssHeader(unreadCount = 10)
}

@Preview(name = "RSS Content")
@Composable
fun PreviewRssContent() {
    RssContent(id = 0, title = "title", unreadCount = 10, showDropdownMenu = true)
}

@Preview(name = "Footer")
@Composable
fun PreviewFooter() {
    Footer(footerState = RssListFooterState.UNREAD_ONLY)
}
