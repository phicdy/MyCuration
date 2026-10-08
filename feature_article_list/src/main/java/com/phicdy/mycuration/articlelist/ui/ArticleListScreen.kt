package com.phicdy.mycuration.articlelist.ui

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalRippleConfiguration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.ItemTouchHelper
import com.phicdy.mycuration.advertisement.AdProvider
import com.phicdy.mycuration.articlelist.R
import kotlinx.coroutines.launch

/**
 * Article list rendered with Compose. Not used by production code yet; it will replace the
 * RecyclerView in ArticlesListFragment in a later step.
 *
 * LazyColumn item indices are equal to the indices of the raw ArticleItem list, so the
 * callbacks pass positions that the position based action creators can use as is.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ArticleListScreen(
    uiState: ArticleListUiState,
    listState: LazyListState,
    adProvider: AdProvider,
    onItemClick: (position: Int) -> Unit,
    onItemLongClick: (position: Int) -> Unit,
    onFavoriteClick: (position: Int) -> Unit,
    /** [direction] is ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, as SwipeActionCreator expects. */
    onSwipe: (position: Int, direction: Int) -> Unit,
) {
    CompositionLocalProvider(LocalRippleConfiguration provides rememberPlatformRippleConfiguration()) {
        when (uiState) {
            ArticleListUiState.Loading -> Box(modifier = Modifier.fillMaxSize())
            is ArticleListUiState.Empty -> EmptyArticleList(
                text = stringResource(
                    when (uiState.reason) {
                        ArticleListUiState.EmptyReason.NO_ARTICLE -> R.string.no_article
                        ArticleListUiState.EmptyReason.NO_SEARCH_RESULT -> R.string.no_search_result
                    }
                )
            )

            is ArticleListUiState.Loaded -> ArticleList(
                rows = uiState.rows,
                listState = listState,
                adProvider = adProvider,
                onItemClick = onItemClick,
                onItemLongClick = onItemLongClick,
                onFavoriteClick = onFavoriteClick,
                onSwipe = onSwipe,
            )
        }
    }
}

@Composable
private fun ArticleList(
    rows: List<ArticleRowUi>,
    listState: LazyListState,
    adProvider: AdProvider,
    onItemClick: (position: Int) -> Unit,
    onItemLongClick: (position: Int) -> Unit,
    onFavoriteClick: (position: Int) -> Unit,
    onSwipe: (position: Int, direction: Int) -> Unit,
) {
    val adViewCache = remember(adProvider) { AdViewCache(adProvider) }
    DisposableEffect(adViewCache) {
        onDispose { adViewCache.release() }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(
            items = rows,
            key = { _, row -> row.key },
            contentType = { _, row ->
                when (row) {
                    is ArticleRowUi.Content -> CONTENT_TYPE_ARTICLE
                    is ArticleRowUi.Advertisement -> CONTENT_TYPE_AD
                }
            },
        ) { index, row ->
            when (row) {
                is ArticleRowUi.Content -> SwipeableArticleRow(
                    row = row,
                    onClick = { onItemClick(index) },
                    onLongClick = { onItemLongClick(index) },
                    onFavoriteClick = { onFavoriteClick(index) },
                    onSwipe = { direction -> onSwipe(index, direction) },
                )

                is ArticleRowUi.Advertisement -> AdRow(adViewCache)
            }
        }
    }
}

/**
 * Article row that can be swiped in both directions. The row is never removed: after a swipe
 * [onSwipe] is called and the row goes back to its position, like the former ItemTouchHelper.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableArticleRow(
    row: ArticleRowUi.Content,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onSwipe: (direction: Int) -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val coroutineScope = rememberCoroutineScope()
    val layoutDirection = LocalLayoutDirection.current
    SwipeToDismissBox(
        state = dismissState,
        // ItemTouchHelper did not draw anything behind the swiped row
        backgroundContent = {},
        onDismiss = { value ->
            value.toItemTouchHelperDirection(layoutDirection)?.let(onSwipe)
            coroutineScope.launch { dismissState.reset() }
        },
    ) {
        ArticleRow(
            row = row,
            onClick = onClick,
            onLongClick = onLongClick,
            onFavoriteClick = onFavoriteClick,
        )
    }
}

/**
 * Converts a swipe value to the absolute ItemTouchHelper direction that SwipeActionCreator
 * expects, or null when the row is not swiped.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal fun SwipeToDismissBoxValue.toItemTouchHelperDirection(layoutDirection: LayoutDirection): Int? =
    when (this) {
        SwipeToDismissBoxValue.StartToEnd ->
            if (layoutDirection == LayoutDirection.Ltr) ItemTouchHelper.RIGHT else ItemTouchHelper.LEFT

        SwipeToDismissBoxValue.EndToStart ->
            if (layoutDirection == LayoutDirection.Ltr) ItemTouchHelper.LEFT else ItemTouchHelper.RIGHT

        SwipeToDismissBoxValue.Settled -> null
    }

/**
 * Advertisement row that hosts the item view of [AdProvider.newViewHolderInstance].
 */
@Composable
private fun AdRow(adViewCache: AdViewCache) {
    AndroidView(
        factory = { context -> adViewCache.obtain(context) },
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * Keeps the advertisement view while the list is shown, so that the ad is created and loaded
 * only once and is not reloaded when the row is scrolled out and back in.
 */
private class AdViewCache(private val adProvider: AdProvider) {
    private var container: FrameLayout? = null

    fun obtain(context: Context): FrameLayout {
        container?.let { current ->
            (current.parent as? ViewGroup)?.removeView(current)
            return current
        }
        return FrameLayout(context).also { newContainer ->
            val holder = adProvider.newViewHolderInstance(newContainer)
            newContainer.addView(holder.itemView)
            holder.bind()
            container = newContainer
        }
    }

    fun release() {
        container?.let { (it.parent as? ViewGroup)?.removeView(it) }
        container = null
    }
}

private const val CONTENT_TYPE_ARTICLE: String = "article"
private const val CONTENT_TYPE_AD: String = "ad"
