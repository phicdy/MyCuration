package com.phicdy.mycuration.articlelist.ui

import android.content.Context
import android.os.SystemClock
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalRippleConfiguration
import androidx.compose.material.RippleConfiguration
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.phicdy.mycuration.advertisement.AdProvider
import com.phicdy.mycuration.articlelist.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive

/**
 * Article list rendered with Compose. It reproduces the look of the former RecyclerView rows
 * (R.layout.articles_list) and the empty view of R.layout.fragment_articles_list.
 *
 * Item indices of the LazyColumn are equal to indices of the raw [com.phicdy.mycuration.articlelist.ArticleItem]
 * list, so callbacks pass positions that position based action creators can use as is.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ArticleListScreen(
    uiState: ArticleListUiState,
    listState: LazyListState,
    adProvider: AdProvider,
    onItemClick: (position: Int) -> Unit,
    onItemLongClick: (position: Int) -> Unit,
    onFavoriteClick: (position: Int) -> Unit,
    /** [direction] is ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT as SwipeActionCreator expects. */
    onSwipe: (position: Int, direction: Int) -> Unit,
    /** Positions to scroll to, emitted after ScrollActionCreator marked visible articles as read. */
    scrollRequests: Flow<Int>,
    onScrollFinished: () -> Unit,
) {
    val currentOnScrollFinished by rememberUpdatedState(onScrollFinished)
    LaunchedEffect(listState, scrollRequests) {
        scrollRequests.collect { position ->
            try {
                listState.animateScrollToShowAtBottom(position)
            } catch (e: CancellationException) {
                // The animation is cancelled when the user touches the list. Keep collecting
                // unless this effect itself is cancelled.
                if (!currentCoroutineContext().isActive) throw e
            }
            currentOnScrollFinished()
        }
    }
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
 * Same as the TextView in R.layout.fragment_articles_list: full width, centered in the parent,
 * default text appearance (14sp) and the theme text color.
 */
@Composable
private fun EmptyArticleList(text: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        BasicText(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            style = viewTextStyle(fontSize = 14.sp, color = colorResource(R.color.text_primary))
                .copy(textAlign = TextAlign.Center),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableArticleRow(
    row: ArticleRowUi.Content,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onSwipe: (direction: Int) -> Unit,
) {
    val currentOnSwipe by rememberUpdatedState(onSwipe)
    val layoutDirection = LocalLayoutDirection.current
    val currentLayoutDirection by rememberUpdatedState(layoutDirection)
    val swipeGuard = remember { SwipeGuard() }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            value.toItemTouchHelperDirection(currentLayoutDirection)?.let { direction ->
                // Fire once per swipe even if the state asks for confirmation more than once
                if (swipeGuard.tryAcquire(SystemClock.uptimeMillis())) currentOnSwipe(direction)
            }
            // Never dismiss. The row stays in the list and snaps back like the former
            // ItemTouchHelper + notifyItemChanged behavior.
            value == SwipeToDismissBoxValue.Settled
        },
        // Same as the default swipe threshold of ItemTouchHelper (half of the item width)
        positionalThreshold = { totalDistance -> totalDistance * ITEM_TOUCH_HELPER_SWIPE_THRESHOLD },
    )
    SwipeToDismissBox(
        state = dismissState,
        // ItemTouchHelper did not draw anything behind the swiped row
        backgroundContent = {},
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
 * Ignores swipe confirmations that arrive shortly after the previous one.
 */
internal class SwipeGuard(private val intervalMillis: Long = SWIPE_GUARD_INTERVAL_MILLIS) {
    private var lastAcquiredAt: Long? = null

    fun tryAcquire(now: Long): Boolean {
        val last = lastAcquiredAt
        if (last != null && now - last < intervalMillis) return false
        lastAcquiredAt = now
        return true
    }
}

/**
 * Compose version of R.layout.articles_list. A single custom layout reproduces the constraints
 * of the former ConstraintLayout, so that overflowing children (feed title centered on the 12dp
 * icon, 32dp favorite star centered on the date) are placed and touchable exactly as before.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ArticleRow(
    row: ArticleRowUi.Content,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    val textColor = colorResource(if (row.isRead) R.color.text_read else R.color.text_primary)
    // Toggle the star immediately like the former adapter did, before the new list arrives
    var isFavorite by remember(row.isFavorite) { mutableStateOf(row.isFavorite) }
    val hasFeed = row.feedTitle != null
    Layout(
        content = {
            // 0: title
            BasicText(
                text = row.title,
                style = viewTextStyle(fontSize = 14.sp, color = textColor),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            // 1: url
            BasicText(
                text = row.url,
                style = viewTextStyle(fontSize = 12.sp, color = colorResource(R.color.text_url)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // 2: posted date
            BasicText(
                text = remember(row.postedDate) { formatPostedDate(row.postedDate) },
                style = viewTextStyle(fontSize = 12.sp, color = textColor),
            )
            // 3: hatena icon
            Image(
                painter = painterResource(R.drawable.hatena),
                contentDescription = stringResource(R.string.hatena),
            )
            // 4: hatena point
            BasicText(
                text = row.point ?: stringResource(R.string.not_get_hatena_point),
                style = viewTextStyle(fontSize = 12.sp, color = textColor),
            )
            // 5: favorite star
            Image(
                painter = painterResource(
                    if (isFavorite) R.drawable.ic_favorite_on else R.drawable.ic_favorite_off
                ),
                contentDescription = null,
                modifier = Modifier
                    .clickable {
                        isFavorite = !isFavorite
                        onFavoriteClick()
                    }
                    .padding(4.dp),
                // Compose ignores android:alpha on the <vector> root of these drawables
                alpha = FAVORITE_ICON_ALPHA,
            )
            // 6: divider (style Divider: background_base_divider with alpha 0.12)
            Box(
                modifier = Modifier.background(
                    colorResource(R.color.background_base_divider).copy(alpha = DIVIDER_ALPHA)
                ),
            )
            // In ConstraintLayout, gone views and their own margins collapse to zero
            if (hasFeed) {
                // 7: feed icon
                FeedIcon(iconPath = row.feedIconPath)
                // 8: feed title
                BasicText(
                    text = row.feedTitle.orEmpty(),
                    style = viewTextStyle(fontSize = 12.sp, color = textColor),
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp)
            // ViewGroup clips children to its padding by default (clipToPadding)
            .clipToBounds(),
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val loose = Constraints(maxWidth = width)
        val margin4 = 4.dp.roundToPx()
        val margin8 = 8.dp.roundToPx()

        val title = measurables[0].measure(Constraints.fixedWidth(width))
        val titleTop = margin8
        var anchorBottom = titleTop + title.height

        val icon = if (hasFeed) measurables[7].measure(loose) else null
        val iconTop = anchorBottom + margin8
        val feedTitle = if (hasFeed && icon != null) {
            // wrap_content in ConstraintLayout is limited by the parent width only
            measurables[8].measure(loose)
        } else {
            null
        }
        if (icon != null) anchorBottom = iconTop + icon.height

        val url = measurables[1].measure(loose)
        val urlTop = anchorBottom + margin8

        val date = measurables[2].measure(loose)
        val dateTop = urlTop + url.height + margin4
        val hatena = measurables[3].measure(loose)
        val point = measurables[4].measure(loose)
        val favorite = measurables[5].measure(loose)

        val dividerTop = dateTop + date.height + margin8
        val divider = measurables[6].measure(Constraints.fixed(width, 1.dp.roundToPx()))

        val favoriteTop = dateTop + centerOn(date.height, favorite.height)
        val feedTitleTop = if (icon != null && feedTitle != null) {
            iconTop + centerOn(icon.height, feedTitle.height)
        } else {
            0
        }
        // wrap_content height of ConstraintLayout covers the bottom of every child
        val height = maxOf(
            dividerTop + divider.height,
            favoriteTop + favorite.height,
            feedTitleTop + (feedTitle?.height ?: 0),
        )

        layout(width, height) {
            title.placeRelative(0, titleTop)
            if (icon != null && feedTitle != null) {
                icon.placeRelative(0, iconTop)
                feedTitle.placeRelative(icon.width + margin4, feedTitleTop)
            }
            url.placeRelative(0, urlTop)
            date.placeRelative(0, dateTop)
            val hatenaX = date.width + margin8
            hatena.placeRelative(hatenaX, dateTop + centerOn(date.height, hatena.height))
            point.placeRelative(
                hatenaX + hatena.width + margin4,
                dateTop + centerOn(date.height, point.height),
            )
            favorite.placeRelative(width - favorite.width, favoriteTop)
            divider.placeRelative(0, dividerTop)
        }
    }
}

/**
 * Feed icon loaded by Glide into an ImageView, same as the former adapter.
 */
@Composable
private fun FeedIcon(iconPath: String?) {
    val description = stringResource(R.string.icon)
    AndroidView(
        factory = { context ->
            ImageView(context).apply { contentDescription = description }
        },
        modifier = Modifier.size(12.dp),
        // Glide ignores an equivalent request for the same target, so re-binding is cheap
        update = { imageView -> bindFeedIcon(imageView, iconPath) },
    )
}

private fun bindFeedIcon(imageView: ImageView, iconPath: String?) {
    if (iconPath == null) {
        Glide.with(imageView).clear(imageView)
        imageView.setImageResource(R.drawable.ic_rss)
    } else {
        Glide.with(imageView)
            .load(iconPath)
            .placeholder(R.drawable.ic_rss)
            .circleCrop()
            .error(R.drawable.ic_rss)
            .into(imageView)
    }
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
 * only once and not reloaded when the row is scrolled out and back in.
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

/**
 * Ripple that looks like ?android:attr/selectableItemBackground: the color and alpha of
 * android:colorControlHighlight of the current theme (light or dark).
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun rememberPlatformRippleConfiguration(): RippleConfiguration {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(context, configuration) {
        val highlight = resolveColorControlHighlight(context)
        RippleConfiguration(
            color = highlight.copy(alpha = 1f),
            rippleAlpha = RippleAlpha(
                draggedAlpha = highlight.alpha,
                focusedAlpha = highlight.alpha,
                hoveredAlpha = highlight.alpha,
                pressedAlpha = highlight.alpha,
            ),
        )
    }
}

private fun resolveColorControlHighlight(context: Context): Color {
    val typedValue = TypedValue()
    if (!context.theme.resolveAttribute(android.R.attr.colorControlHighlight, typedValue, true)) {
        return DEFAULT_HIGHLIGHT
    }
    return when {
        typedValue.type in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT ->
            Color(typedValue.data)

        typedValue.resourceId != 0 -> Color(ContextCompat.getColor(context, typedValue.resourceId))
        else -> DEFAULT_HIGHLIGHT
    }
}

/**
 * Text style equivalent to a plain TextView: no letter spacing and font padding included.
 */
@Suppress("DEPRECATION")
private fun viewTextStyle(fontSize: TextUnit, color: Color) = TextStyle(
    color = color,
    fontSize = fontSize,
    letterSpacing = 0.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = true),
)

/** Vertical offset that centers a child of [size] on an anchor of [anchorSize], like ConstraintLayout. */
private fun centerOn(anchorSize: Int, size: Int): Int = (anchorSize - size) / 2

internal fun formatPostedDate(postedDate: Long): String =
    SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US).format(Date(postedDate))

private const val CONTENT_TYPE_ARTICLE = "article"
private const val CONTENT_TYPE_AD = "ad"
private const val ITEM_TOUCH_HELPER_SWIPE_THRESHOLD = 0.5f
private const val SWIPE_GUARD_INTERVAL_MILLIS = 500L
private const val DIVIDER_ALPHA = 0.12f
private const val FAVORITE_ICON_ALPHA = 0.6f
private val DEFAULT_HIGHLIGHT = Color(0x1F000000)
