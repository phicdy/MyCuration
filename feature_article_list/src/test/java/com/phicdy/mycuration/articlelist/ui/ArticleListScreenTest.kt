package com.phicdy.mycuration.articlelist.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.recyclerview.widget.ItemTouchHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

@OptIn(ExperimentalMaterial3Api::class)
class ArticleListScreenTest {

    @Test
    fun `swipe values are converted to ItemTouchHelper directions in LTR`() {
        assertThat(SwipeToDismissBoxValue.StartToEnd.toItemTouchHelperDirection(LayoutDirection.Ltr))
            .isEqualTo(ItemTouchHelper.RIGHT)
        assertThat(SwipeToDismissBoxValue.EndToStart.toItemTouchHelperDirection(LayoutDirection.Ltr))
            .isEqualTo(ItemTouchHelper.LEFT)
        assertThat(SwipeToDismissBoxValue.Settled.toItemTouchHelperDirection(LayoutDirection.Ltr))
            .isNull()
    }

    @Test
    fun `swipe values are converted to absolute ItemTouchHelper directions in RTL`() {
        assertThat(SwipeToDismissBoxValue.StartToEnd.toItemTouchHelperDirection(LayoutDirection.Rtl))
            .isEqualTo(ItemTouchHelper.LEFT)
        assertThat(SwipeToDismissBoxValue.EndToStart.toItemTouchHelperDirection(LayoutDirection.Rtl))
            .isEqualTo(ItemTouchHelper.RIGHT)
    }
}
