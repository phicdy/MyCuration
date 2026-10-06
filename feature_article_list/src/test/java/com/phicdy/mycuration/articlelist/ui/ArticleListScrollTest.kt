package com.phicdy.mycuration.articlelist.ui

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.recyclerview.widget.ItemTouchHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class ArticleListScrollTest {

    private fun item(index: Int, offset: Int, size: Int): LazyListItemInfo = mock {
        on { this.index } doReturn index
        on { this.offset } doReturn offset
        on { this.size } doReturn size
    }

    @Test
    fun `first visible item is the first partially visible item`() {
        val items = listOf(item(3, -50, 40), item(4, -10, 40), item(5, 30, 40))
        assertThat(firstVisibleItemIndex(items, viewportStartOffset = 0)).isEqualTo(4)
    }

    @Test
    fun `first visible item is -1 when nothing is visible`() {
        assertThat(firstVisibleItemIndex(emptyList(), viewportStartOffset = 0)).isEqualTo(-1)
    }

    @Test
    fun `last completely visible item ignores the partially visible last item`() {
        val items = listOf(item(0, 0, 40), item(1, 40, 40), item(2, 80, 40))
        assertThat(
            lastCompletelyVisibleItemIndex(items, viewportStartOffset = 0, viewportEndOffset = 100)
        ).isEqualTo(1)
    }

    @Test
    fun `last completely visible item includes an item that ends at the viewport end`() {
        val items = listOf(item(0, 0, 50), item(1, 50, 50))
        assertThat(
            lastCompletelyVisibleItemIndex(items, viewportStartOffset = 0, viewportEndOffset = 100)
        ).isEqualTo(1)
    }

    @Test
    fun `last completely visible item is -1 when one item is larger than the viewport`() {
        val items = listOf(item(0, -10, 200))
        assertThat(
            lastCompletelyVisibleItemIndex(items, viewportStartOffset = 0, viewportEndOffset = 100)
        ).isEqualTo(-1)
    }

    @Test
    fun `item below the viewport is aligned to the bottom`() {
        assertThat(
            distanceToShowCompletely(itemOffset = 90, itemSize = 40, viewportStartOffset = 0, viewportEndOffset = 100)
        ).isEqualTo(30)
    }

    @Test
    fun `item above the viewport is aligned to the top`() {
        assertThat(
            distanceToShowCompletely(itemOffset = -15, itemSize = 40, viewportStartOffset = 0, viewportEndOffset = 100)
        ).isEqualTo(-15)
    }

    @Test
    fun `completely visible item does not move`() {
        assertThat(
            distanceToShowCompletely(itemOffset = 20, itemSize = 40, viewportStartOffset = 0, viewportEndOffset = 100)
        ).isEqualTo(0)
    }

    @Test
    fun `estimated distance covers the overflow of the last item and the items between`() {
        assertThat(
            estimatedDistanceToShowAtBottom(
                lastVisibleItemBottom = 120,
                viewportEndOffset = 100,
                itemsBetween = 3,
                averageItemSize = 40,
            )
        ).isEqualTo(140)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun `swipe values are converted to ItemTouchHelper directions in LTR`() {
        assertThat(SwipeToDismissBoxValue.StartToEnd.toItemTouchHelperDirection(LayoutDirection.Ltr))
            .isEqualTo(ItemTouchHelper.RIGHT)
        assertThat(SwipeToDismissBoxValue.EndToStart.toItemTouchHelperDirection(LayoutDirection.Ltr))
            .isEqualTo(ItemTouchHelper.LEFT)
        assertThat(SwipeToDismissBoxValue.Settled.toItemTouchHelperDirection(LayoutDirection.Ltr))
            .isNull()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun `swipe values are converted to absolute ItemTouchHelper directions in RTL`() {
        assertThat(SwipeToDismissBoxValue.StartToEnd.toItemTouchHelperDirection(LayoutDirection.Rtl))
            .isEqualTo(ItemTouchHelper.LEFT)
        assertThat(SwipeToDismissBoxValue.EndToStart.toItemTouchHelperDirection(LayoutDirection.Rtl))
            .isEqualTo(ItemTouchHelper.RIGHT)
    }

    @Test
    fun `swipe guard ignores confirmations within the interval`() {
        val guard = SwipeGuard(intervalMillis = 500)
        assertThat(guard.tryAcquire(1000)).isTrue()
        assertThat(guard.tryAcquire(1200)).isFalse()
        assertThat(guard.tryAcquire(1500)).isTrue()
    }
}
