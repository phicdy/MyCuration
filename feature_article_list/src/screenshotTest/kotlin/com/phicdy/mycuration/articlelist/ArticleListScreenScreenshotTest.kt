package com.phicdy.mycuration.articlelist

import android.content.Context
import android.content.res.Configuration
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.phicdy.mycuration.advertisement.AdFragment
import com.phicdy.mycuration.advertisement.AdProvider
import com.phicdy.mycuration.advertisement.AdViewHolder
import com.phicdy.mycuration.articlelist.ui.ArticleListScreen
import com.phicdy.mycuration.articlelist.ui.ArticleListUiState
import com.phicdy.mycuration.articlelist.ui.ArticleRowUi
import com.phicdy.mycuration.resource.MyCurationTheme

/**
 * Screenshots of the Compose article list. The ad row is left out because the real ad view
 * cannot be rendered here.
 */
@PreviewTest
@Preview(name = "Light", widthDp = 360, heightDp = 400, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 360, heightDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ArticleListScreenPreview() {
    ArticleListScreenContainer(uiState = ArticleListUiState.Loaded(sampleListRows))
}

@PreviewTest
@Preview(name = "Light", widthDp = 360, heightDp = 200, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 360, heightDp = 200, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ArticleListScreenEmptyPreview() {
    ArticleListScreenContainer(
        uiState = ArticleListUiState.Empty(ArticleListUiState.EmptyReason.NO_ARTICLE),
    )
}

@Composable
private fun ArticleListScreenContainer(uiState: ArticleListUiState) {
    MyCurationTheme {
        Box(modifier = Modifier.background(windowBackgroundColor(LocalContext.current))) {
            ArticleListScreen(
                uiState = uiState,
                listState = rememberLazyListState(),
                adProvider = NoAdProvider,
                onItemClick = {},
                onItemLongClick = {},
                onFavoriteClick = {},
                onSwipe = { _, _ -> },
            )
        }
    }
}

/** The sample rows have no ad row, so the provider is never called. */
private object NoAdProvider : AdProvider {
    override fun init(context: Context) {}
    override fun newViewHolderInstance(parent: ViewGroup): AdViewHolder = error("No ad in screenshots")
    override fun newFragmentInstance(): AdFragment = error("No ad in screenshots")
}

// 2019/01/01 03:00:00 UTC
private const val SAMPLE_LIST_POSTED_DATE: Long = 1546311600000L

private val sampleListRows: List<ArticleRowUi> = listOf(
    ArticleRowUi.Content(
        id = 1,
        title = "Unread article title that is long enough to wrap onto a second line in the list",
        url = "https://example.com/articles/1",
        isRead = false,
        isFavorite = false,
        postedDate = SAMPLE_LIST_POSTED_DATE,
        point = "128",
        feedId = 1,
        feedTitle = "Sample RSS feed",
        feedIconPath = null,
    ),
    ArticleRowUi.Content(
        id = 2,
        title = "Read favorite article title",
        url = "https://example.com/articles/2",
        isRead = true,
        isFavorite = true,
        postedDate = SAMPLE_LIST_POSTED_DATE,
        point = null,
        feedId = 1,
        feedTitle = "Sample RSS feed",
        feedIconPath = null,
    ),
    ArticleRowUi.Content(
        id = 3,
        title = "Article without feed",
        url = "https://example.com/articles/3",
        isRead = false,
        isFavorite = false,
        postedDate = SAMPLE_LIST_POSTED_DATE,
        point = "3",
        feedId = 2,
        feedTitle = null,
        feedIconPath = null,
    ),
)
