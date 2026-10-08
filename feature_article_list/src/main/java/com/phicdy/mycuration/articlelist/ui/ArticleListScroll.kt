package com.phicdy.mycuration.articlelist.ui

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState

/*
 * Scroll helpers for the FAB of the article list. They give the same positions that
 * ScrollActionCreator received from LinearLayoutManager and scroll like
 * RecyclerView.smoothScrollToPosition().
 */

/**
 * Equivalent of LinearLayoutManager.findFirstVisibleItemPosition(): the first item that is at
 * least partially visible, or -1 when nothing is visible.
 */
internal fun firstVisibleItemIndex(visibleItems: List<LazyListItemInfo>, viewportStartOffset: Int): Int =
    visibleItems.firstOrNull { it.offset + it.size > viewportStartOffset }?.index ?: NO_POSITION

/**
 * Equivalent of LinearLayoutManager.findLastCompletelyVisibleItemPosition(): the last item that
 * is completely visible, or -1 when there is no such item.
 */
internal fun lastCompletelyVisibleItemIndex(
    visibleItems: List<LazyListItemInfo>,
    viewportStartOffset: Int,
    viewportEndOffset: Int,
): Int = visibleItems.lastOrNull {
    it.offset >= viewportStartOffset && it.offset + it.size <= viewportEndOffset
}?.index ?: NO_POSITION

internal fun LazyListState.firstVisibleItemPosition(): Int =
    firstVisibleItemIndex(layoutInfo.visibleItemsInfo, layoutInfo.viewportStartOffset)

internal fun LazyListState.lastCompletelyVisibleItemPosition(): Int = lastCompletelyVisibleItemIndex(
    layoutInfo.visibleItemsInfo,
    layoutInfo.viewportStartOffset,
    layoutInfo.viewportEndOffset,
)

/**
 * Smooth scroll that ends like RecyclerView.smoothScrollToPosition() with LinearLayoutManager:
 * an item below the viewport ends up completely visible at the bottom, an item above the
 * viewport ends up at the top and a completely visible item does not move.
 */
internal suspend fun LazyListState.animateScrollToShowAtBottom(position: Int) {
    val itemCount = layoutInfo.totalItemsCount
    if (itemCount == 0 || position < 0) return
    val target = position.coerceAtMost(itemCount - 1)

    // Bring the target into the viewport. Item sizes vary, so scroll by an estimated distance
    // and repeat a few times if the estimation was too short.
    repeat(MAX_ESTIMATED_SCROLLS) {
        val visibleItems = layoutInfo.visibleItemsInfo
        if (visibleItems.isEmpty() || visibleItems.any { it.index == target }) return@repeat
        val first = visibleItems.first()
        val last = visibleItems.last()
        if (target < first.index) {
            animateScrollToItem(target)
            return
        }
        val averageSize = visibleItems.sumOf { it.size } / visibleItems.size
        val distance = estimatedDistanceToShowAtBottom(
            lastVisibleItemBottom = last.offset + last.size,
            viewportEndOffset = layoutInfo.viewportEndOffset,
            itemsBetween = target - last.index,
            averageItemSize = averageSize,
        )
        animateScrollBy(distance.toFloat())
    }

    // Align exactly with the real size of the target
    val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == target } ?: return
    val delta = distanceToShowCompletely(
        itemOffset = item.offset,
        itemSize = item.size,
        viewportStartOffset = layoutInfo.viewportStartOffset,
        viewportEndOffset = layoutInfo.viewportEndOffset,
    )
    if (delta != 0) animateScrollBy(delta.toFloat())
}

/**
 * Estimated forward scroll distance that brings the item [itemsBetween] items after the last
 * visible item to the bottom of the viewport.
 */
internal fun estimatedDistanceToShowAtBottom(
    lastVisibleItemBottom: Int,
    viewportEndOffset: Int,
    itemsBetween: Int,
    averageItemSize: Int,
): Int = (lastVisibleItemBottom - viewportEndOffset) + itemsBetween * averageItemSize

/**
 * Scroll distance that makes an item completely visible with the least scroll, like
 * LinearSmoothScroller.SNAP_TO_ANY: an item below the viewport is aligned to the bottom, an
 * item above the viewport is aligned to the top and a completely visible item does not move.
 */
internal fun distanceToShowCompletely(
    itemOffset: Int,
    itemSize: Int,
    viewportStartOffset: Int,
    viewportEndOffset: Int,
): Int = when {
    itemOffset < viewportStartOffset -> itemOffset - viewportStartOffset
    itemOffset + itemSize > viewportEndOffset -> itemOffset + itemSize - viewportEndOffset
    else -> 0
}

private const val NO_POSITION: Int = -1
private const val MAX_ESTIMATED_SCROLLS: Int = 3
