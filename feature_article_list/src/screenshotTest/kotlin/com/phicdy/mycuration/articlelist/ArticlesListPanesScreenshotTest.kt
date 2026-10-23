package com.phicdy.mycuration.articlelist

import android.content.Context
import android.content.res.Configuration
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.phicdy.mycuration.advertisement.AdFragment
import com.phicdy.mycuration.advertisement.AdProvider
import com.phicdy.mycuration.advertisement.AdViewHolder
import com.phicdy.mycuration.articlelist.ui.ArticleDetailPane
import com.phicdy.mycuration.articlelist.ui.ArticleListScreen
import com.phicdy.mycuration.articlelist.ui.ArticleListUiState
import com.phicdy.mycuration.articlelist.ui.ArticleRowUi
import com.phicdy.mycuration.articlelist.ui.ArticlesListPanes
import com.phicdy.mycuration.articlelist.ui.ArticlesListScaffold
import com.phicdy.mycuration.resource.MyCurationTheme
import kotlinx.coroutines.flow.emptyFlow

/**
 * Screenshots of the two-pane article list before an article is selected. The fragment can't
 * render here, so the list pane shows ArticleListScreen with sample rows directly.
 */
@PreviewTest
@Preview(name = "Light", widthDp = 900, heightDp = 600, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 900, heightDp = 600, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ArticlesListPanesTwoPanePreview() {
    MyCurationTheme {
        ArticlesListPanes(
            isOpenInternal = true,
            detailRequests = emptyFlow(),
            modifier = Modifier.background(windowBackgroundColor(LocalContext.current)),
            listPane = { _, _, selectedArticle ->
                ArticlesListScaffold(
                    title = stringResource(R.string.all),
                    snackbarHostState = remember { SnackbarHostState() },
                    onNavigateUp = {},
                    onReadAllClick = {},
                    onSearch = {},
                    onFabClick = {},
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ArticleListScreen(
                            uiState = ArticleListUiState.Loaded(samplePaneRows),
                            listState = rememberLazyListState(),
                            adProvider = NoPaneAdProvider,
                            onItemClick = {},
                            onItemLongClick = {},
                            onFavoriteClick = {},
                            onSwipe = { _, _ -> },
                            selectedArticleId = selectedArticle?.id,
                        )
                    }
                }
            },
            detailPane = { selectedArticle ->
                ArticleDetailPane(
                    article = selectedArticle,
                    onShareClick = {},
                    onOpenInBrowserClick = {},
                )
            },
        )
    }
}

/** The sample rows have no ad row, so the provider is never called. */
private object NoPaneAdProvider : AdProvider {
    override fun init(context: Context) {}
    override fun newViewHolderInstance(parent: ViewGroup): AdViewHolder = error("No ad in screenshots")
    override fun newFragmentInstance(): AdFragment = error("No ad in screenshots")
}

private val samplePaneRows: List<ArticleRowUi> = (1..4).map { id ->
    ArticleRowUi.Content(
        id = id,
        title = "Article title $id",
        url = "https://example.com/articles/$id",
        isRead = id % 2 == 0,
        isFavorite = false,
        // 2019/01/01 03:00:00 UTC
        postedDate = 1546311600000L,
        point = "$id",
        feedId = 1,
        feedTitle = "Sample RSS feed",
        feedIconPath = null,
    )
}
