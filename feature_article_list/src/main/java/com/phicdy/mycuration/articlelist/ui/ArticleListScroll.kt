package com.phicdy.mycuration.articlelist.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
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
    val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull() ?: return
    if (target < firstVisible.index) {
        animateScrollToItem(target)
        return
    }

    // One continuous animation. Item sizes vary, so the remaining distance is measured again on
    // every frame: estimated while the target is below the viewport, exact once it is visible.
    // This avoids a second, separate correction scroll at the end.
    scroll {
        var traveled = 0f
        animate(initialValue = 0f, targetValue = 1f, animationSpec = fabScrollAnimationSpec) { progress, _ ->
            val step = scrollStepForProgress(
                progress = progress,
                traveled = traveled,
                remaining = remainingDistanceToShowAtBottom(target).toFloat(),
            )
            traveled += scrollBy(step)
        }
    }
}

/**
 * Distance left to scroll so that [target] is completely visible at the bottom: exact when the
 * item is visible, otherwise estimated from the average size of the visible items.
 */
private fun LazyListState.remainingDistanceToShowAtBottom(target: Int): Int {
    val visibleItems = layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return 0
    val item = visibleItems.firstOrNull { it.index == target }
    if (item != null) {
        return distanceToShowCompletely(
            itemOffset = item.offset,
            itemSize = item.size,
            viewportStartOffset = layoutInfo.viewportStartOffset,
            viewportEndOffset = layoutInfo.viewportEndOffset,
        )
    }
    val last = visibleItems.last()
    return estimatedDistanceToShowAtBottom(
        lastVisibleItemBottom = last.offset + last.size,
        viewportEndOffset = layoutInfo.viewportEndOffset,
        itemsBetween = target - last.index,
        averageItemSize = visibleItems.sumOf { it.size } / visibleItems.size,
    )
}

/**
 * Scroll amount for one animation frame: the eased [progress] of the whole distance, where the
 * whole distance is what has been [traveled] so far plus what is [remaining] now.
 */
internal fun scrollStepForProgress(progress: Float, traveled: Float, remaining: Float): Float =
    progress * (traveled + remaining) - traveled

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

/**
 * The default spring of animateScrollBy() moves a whole screen in a blink, so the FAB scroll
 * uses a slower tween that the eye can follow.
 */
private val fabScrollAnimationSpec: AnimationSpec<Float> =
    tween(durationMillis = FAB_SCROLL_DURATION_MILLIS, easing = FastOutSlowInEasing)

private const val FAB_SCROLL_DURATION_MILLIS: Int = 500
private const val NO_POSITION: Int = -1
