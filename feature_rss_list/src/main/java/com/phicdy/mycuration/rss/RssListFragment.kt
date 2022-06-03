package com.phicdy.mycuration.rss

import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.phicdy.mycuration.articlelist.ArticlesListActivity
import com.phicdy.mycuration.articlelist.FavoriteArticlesListActivity
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

    @Inject
    lateinit var showDeleteRssAlertDialogActionCreator: ShowDeleteRssAlertDialogActionCreator

    @Inject
    lateinit var hideDeleteRssAlertDialogActionCreator: HideDeleteRssAlertDialogActionCreator

    @Inject
    lateinit var showEditRssTitleAlertDialogActionCreator: ShowEditRssTitleAlertDialogActionCreator

    @Inject
    lateinit var hideEditRssTitleAlertDialogActionCreator: HideEditRssTitleAlertDialogActionCreator

    @Inject
    lateinit var newRssTitleChangeActionCreator: NewRssTitleChangeActionCreator

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
                        onHeaderClicked = {
                            val intent = Intent(requireContext(), ArticlesListActivity::class.java)
                            startActivity(intent)
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
                        onDeleteMenuClicked = { id ->
                            lifecycleScope.launchWhenStarted {
                                hideDropdownMenuActionCreator.run()
                                showDeleteRssAlertDialogActionCreator.run(id)
                            }
                        },
                        onDismissDropdownMenu = {
                            lifecycleScope.launchWhenStarted {
                                hideDropdownMenuActionCreator.run()
                            }
                        },
                        onDismissDeleteRssDialog = {
                            lifecycleScope.launchWhenStarted {
                                hideDeleteRssAlertDialogActionCreator.run()
                            }
                        },
                        onDeleteRssClicked = { id ->
                            lifecycleScope.launchWhenStarted {
                                val state =
                                    rssListStateStore.state.value
                                        ?: return@launchWhenStarted
                                deleteRssActionCreator.run(
                                    id,
                                    state.rawRssList,
                                    state.mode
                                )
                                hideDeleteRssAlertDialogActionCreator.run()
                            }
                        },
                        onCancelDeleteRssClicked = {
                            lifecycleScope.launchWhenStarted {
                                hideDeleteRssAlertDialogActionCreator.run()
                            }
                        },
                        onEditTitleMenuClicked = { id, title ->
                            lifecycleScope.launchWhenStarted {
                                hideDropdownMenuActionCreator.run()
                                showEditRssTitleAlertDialogActionCreator.run(id, title)
                            }
                        },
                        onDismissEditRssTitleDialog = {
                            lifecycleScope.launchWhenStarted {
                                hideEditRssTitleAlertDialogActionCreator.run()
                            }
                        },
                        onEditRssTitleClicked = { newTitle, rssId ->
                            lifecycleScope.launchWhenStarted {
                                hideEditRssTitleAlertDialogActionCreator.run()
                                editRssTitleActionCreator.run(newTitle, rssId)
                            }
                        },
                        onCancelEditRssTitleClicked = {
                            lifecycleScope.launchWhenStarted {
                                hideEditRssTitleAlertDialogActionCreator.run()
                            }
                        },
                        onNewRssTitleChanged = { newTitle ->
                            lifecycleScope.launchWhenStarted {
                                newRssTitleChangeActionCreator.run(newTitle)
                            }
                        },
                        onMessageConsumed = { message ->
                            lifecycleScope.launchWhenStarted {
                                consumeRssListMessageActionCreator.run(message)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
}

@Composable
fun RssListScreen(
    store: RSSListStateStore,
    onRefresh: () -> Unit = {},
    onHeaderClicked: () -> Unit = {},
    onRssClicked: (Int) -> Unit = {},
    onRssLongClicked: (Int) -> Unit = {},
    onFavoriteClicked: () -> Unit = {},
    onFooterClicked: () -> Unit = {},
    onEditTitleMenuClicked: (Int, String) -> Unit = { _, _ -> },
    onDeleteMenuClicked: (Int) -> Unit = {},
    onDismissDropdownMenu: () -> Unit = {},
    onDismissDeleteRssDialog: () -> Unit = {},
    onDeleteRssClicked: (Int) -> Unit = {},
    onCancelDeleteRssClicked: () -> Unit = {},
    onDismissEditRssTitleDialog: () -> Unit = {},
    onEditRssTitleClicked: (String, Int) -> Unit = { _, _ -> },
    onCancelEditRssTitleClicked: () -> Unit = {},
    onNewRssTitleChanged: (String) -> Unit = {},
    onMessageConsumed: (RssListMessage) -> Unit = {}
) {
    val value = store.state.observeAsState().value ?: return
    RssListScreen(
        items = value.item,
        isInitializing = value.isInitializing,
        isRefreshing = value.isRefreshing,
        messageList = value.messageList,
        showDropdownMenuId = value.showDropdownMenuId,
        onRefresh = onRefresh,
        onHeaderClicked = onHeaderClicked,
        onRssClicked = onRssClicked,
        onRssLongClicked = onRssLongClicked,
        onFavoriteClicked = onFavoriteClicked,
        onFooterClicked = onFooterClicked,
        onEditTitleMenuClicked = onEditTitleMenuClicked,
        onDeleteMenuClicked = onDeleteMenuClicked,
        onDismissDropdownMenu = onDismissDropdownMenu,
        showDeleteRssDialogId = value.showDeleteRssDialogId,
        onDismissDeleteRssDialog = onDismissDeleteRssDialog,
        onDeleteRssClicked = onDeleteRssClicked,
        onCancelDeleteRssClicked = onCancelDeleteRssClicked,
        showEditRssTitleDialogId = value.showEditRssTitleDialogId,
        showEditRssTitleDialogTitle = value.showEditRssTitleDialogTitle,
        onDismissEditRssTitleDialog = onDismissEditRssTitleDialog,
        onEditRssTitleClicked = onEditRssTitleClicked,
        onCancelEditRssTitleClicked = onCancelEditRssTitleClicked,
        onNewRssTitleChanged = onNewRssTitleChanged,
        onMessageConsumed = onMessageConsumed
    )
}

@Composable
fun RssListScreen(
    items: List<RssListItem> = emptyList(),
    isInitializing: Boolean,
    isRefreshing: Boolean,
    messageList: List<RssListMessage> = emptyList(),
    showDropdownMenuId: Int? = null,
    onRefresh: () -> Unit = {},
    onHeaderClicked: () -> Unit = {},
    onRssClicked: (Int) -> Unit = {},
    onRssLongClicked: (Int) -> Unit = {},
    onFavoriteClicked: () -> Unit = {},
    onFooterClicked: () -> Unit = {},
    onEditTitleMenuClicked: (Int, String) -> Unit = { _, _ -> },
    onDeleteMenuClicked: (Int) -> Unit = {},
    onDismissDropdownMenu: () -> Unit = {},
    showDeleteRssDialogId: Int? = null,
    onDismissDeleteRssDialog: () -> Unit = {},
    onDeleteRssClicked: (Int) -> Unit = {},
    onCancelDeleteRssClicked: () -> Unit = {},
    showEditRssTitleDialogId: Int? = null,
    showEditRssTitleDialogTitle: String? = null,
    onDismissEditRssTitleDialog: () -> Unit = {},
    onEditRssTitleClicked: (String, Int) -> Unit = { _, _ -> },
    onCancelEditRssTitleClicked: () -> Unit = {},
    onNewRssTitleChanged: (String) -> Unit = {},
    onMessageConsumed: (RssListMessage) -> Unit = {}
) {
    if (isInitializing) {
        CircularProgressIndicator()
    } else {
        if (items.isEmpty()) {
            Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
            ) {
                RssEmptyText(
                        modifier = Modifier
                )
            }
        } else {
            SwipeRefreshRssList(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    items = items,
                    showDropdownMenuId = showDropdownMenuId,
                    onHeaderClicked = onHeaderClicked,
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
    if (showDeleteRssDialogId != null) {
        DeleteRssAlertDialog(
            rssId = showDeleteRssDialogId,
            onDismissDeleteRssDialog = onDismissDeleteRssDialog,
            onDeleteRssClicked = onDeleteRssClicked,
            onCancelDeleteRssClicked = onCancelDeleteRssClicked,
        )
    }
    if (showEditRssTitleDialogId != null && showEditRssTitleDialogTitle != null) {
        EditRssTitleAlertDialog(
            rssId = showEditRssTitleDialogId,
            title = showEditRssTitleDialogTitle,
            onDismissEditRssTitleDialog = onDismissEditRssTitleDialog,
            onEditRssTitleClicked = onEditRssTitleClicked,
            onCancelEditRssTitleClicked = onCancelEditRssTitleClicked,
            onNewRssTitleChanged = onNewRssTitleChanged
        )
    }
    for (message in messageList) {
        val context = LocalContext.current
        val toastText = when (message.type) {
            RssListMessage.Type.SUCCEED_TO_EDIT_RSS -> {
                context.getString(R.string.edit_rss_title_success)
            }
            RssListMessage.Type.SUCCEED_TO_DELETE_RSS -> {
                context.getString(R.string.finish_delete_rss_success)
            }
            RssListMessage.Type.ERROR_EMPTY_RSS_TITLE_EDIT -> {
                context.getString(R.string.empty_title)
            }
            RssListMessage.Type.ERROR_SAVE_RSS_TITLE -> {
                context.getString(R.string.edit_rss_title_error)
            }
            RssListMessage.Type.ERROR_DELETE_RSS -> {
                context.getString(R.string.finish_delete_rss_fail)
            }
        }
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
        onMessageConsumed(message)
    }
}

@Composable
fun RssEmptyText(modifier: Modifier = Modifier) {
    RssListText(
            text = stringResource(id = com.phicdy.mycuration.resource.R.string.no_rss_message),
            fontSize = 14.sp,
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
    onHeaderClicked: () -> Unit = {},
    onRssClicked: (Int) -> Unit = {},
    onRssLongClicked: (Int) -> Unit = {},
    onFavoriteClicked: () -> Unit = {},
    onFooterClicked: () -> Unit = {},
    onEditTitleMenuClicked: (Int, String) -> Unit = { _, _ -> },
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
                    is RssListItem.All -> AllRssHeader(
                            unreadCount = item.unreadCount,
                            onHeaderClicked = onHeaderClicked
                    )
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
fun AllRssHeader(
        unreadCount: Int,
        onHeaderClicked: () -> Unit = {}
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onHeaderClicked() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_view_headline_black_24dp),
                modifier = Modifier
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp)
                    .width(32.dp)
                    .height(32.dp),
                contentDescription = ""
            )
            RssListText(
                    text = stringResource(id = com.phicdy.mycuration.resource.R.string.all),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.weight(1.0f))
            RssListText(
                    text = unreadCount.toString(),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 16.dp),
                    textAlign = TextAlign.Center
            )
        }
        RssListDivider()
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
    onEditTitleMenuClicked: (Int, String) -> Unit = { _, _ -> },
    onDeleteMenuClicked: (Int) -> Unit = {},
    onDismissDropdownMenu: () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
                        .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 16.dp)
                        .width(32.dp)
                        .height(32.dp),
                    contentDescription = ""
                )
                RssListText(
                        text = title,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.weight(1.0f))
                RssListText(
                        text = unreadCount.toString(),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 16.dp),
                        textAlign = TextAlign.Center
                )
            }
            DropdownMenu(
                    expanded = showDropdownMenu,
                    onDismissRequest = { onDismissDropdownMenu() },
            ) {
                DropdownMenuItem(onClick = { onEditTitleMenuClicked(id, title) }) {
                    Text(text = stringResource(id = R.string.edit_rss_title))
                }
                DropdownMenuItem(onClick = { onDeleteMenuClicked(id) }) {
                    Text(text = stringResource(id = R.string.delete))
                }
            }
        }
        RssListDivider()
    }
}

@Composable
fun FavoriteContent(
        onFavoriteClicked: () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFavoriteClicked() }
        ) {
            Image(
                painter = painterResource(id = com.phicdy.mycuration.resource.R.drawable.ic_favorite_off),
                modifier = Modifier
                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 16.dp)
                    .width(32.dp)
                    .height(32.dp),
                contentDescription = ""
            )
            RssListText(
                    text = stringResource(id = com.phicdy.mycuration.resource.R.string.favorite),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
            )
        }
        RssListDivider()
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
        RssListText(
            text = text,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 64.dp, top = 20.dp, bottom = 20.dp)
                .clickable { onFooterClicked() },
        )
        RssListDivider()
    }
}

@Composable
fun RssListDivider() {
    Divider(modifier = Modifier.padding(start = 64.dp))
}

@Composable
fun DeleteRssAlertDialog(
        rssId: Int,
        onDismissDeleteRssDialog: () -> Unit = {},
        onDeleteRssClicked: (Int) -> Unit = {},
        onCancelDeleteRssClicked: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = { onDismissDeleteRssDialog() },
        confirmButton = {
            TextButton(
                onClick = { onDeleteRssClicked(rssId) },
            ) {
                Text(text = stringResource(id = R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = { onCancelDeleteRssClicked() }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.delete_rss_alert))
        }
    )
}

@Composable
fun EditRssTitleAlertDialog(
    rssId: Int,
    title: String,
    onDismissEditRssTitleDialog: () -> Unit = {},
    onEditRssTitleClicked: (String, Int) -> Unit = { _, _ -> },
    onCancelEditRssTitleClicked: () -> Unit = {},
    onNewRssTitleChanged: (String) -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = { onDismissEditRssTitleDialog() },
        confirmButton = {
            TextButton(
                onClick = { onEditRssTitleClicked(title, rssId) },
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = { onCancelEditRssTitleClicked() }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        title = null,
        text = {
            Column {
                Text(
                    text = stringResource(id = R.string.edit_rss_title),
                    style = MaterialTheme.typography.subtitle1
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(id = R.string.new_title))
                    OutlinedTextField(
                        value = title,
                        onValueChange = onNewRssTitleChanged,
                        modifier = Modifier.padding(start = 8.dp),
                        maxLines = 1
                    )
                }
            }
        }
    )
}

@Composable
fun RssListText(
    modifier: Modifier = Modifier,
    text: String,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        color = MaterialTheme.colors.primary,
        fontSize = fontSize,
        modifier = modifier,
        textAlign = textAlign
    )
}

@Preview(name = "Loading Screen")
@Composable
fun PreviewLoadingRssListScreen() {
    MyCurationTheme {
        RssListScreen(
            items = emptyList(),
            isInitializing = true,
            isRefreshing = false,
            messageList = emptyList()
        )
    }
}

@Preview(name = "Empty Screen")
@Composable
fun PreviewEmptyRssListScreen() {
    MyCurationTheme {
        RssListScreen(
            items = emptyList(),
            isInitializing = false,
            isRefreshing = false,
            messageList = emptyList()
        )
    }
}

@Preview(name = "Header")
@Composable
fun PreviewAllRssHeader() {
    MyCurationTheme {
        AllRssHeader(unreadCount = 10)
    }
}

@Preview(name = "Favorite")
@Composable
fun PreviewFavorite() {
    MyCurationTheme {
        FavoriteContent()
    }
}

@Preview(name = "RSS Content")
@Composable
fun PreviewRssContent() {
    MyCurationTheme {
        RssContent(id = 0, title = "title", unreadCount = 10, showDropdownMenu = true)
    }
}

@Preview(name = "Footer")
@Composable
fun PreviewFooter() {
    MyCurationTheme {
        Footer(footerState = RssListFooterState.UNREAD_ONLY)
    }
}

@Preview(name = "Delete alert dialog")
@Composable
fun PreviewDeleteAlertDialog() {
    DeleteRssAlertDialog(rssId = 0)
}

@Preview(name = "Edit title alert dialog in dark", uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "Edit title alert dialog in light", uiMode = UI_MODE_NIGHT_NO)
@Composable
fun PreviewEditRssTitleAlertDialog() {
    MyCurationTheme {
        EditRssTitleAlertDialog(rssId = 0, title = "title")
    }
}
