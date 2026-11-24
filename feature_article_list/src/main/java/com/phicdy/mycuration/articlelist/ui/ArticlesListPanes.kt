package com.phicdy.mycuration.articlelist.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.currentWindowDpSize
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import com.phicdy.mycuration.articlelist.SelectedArticle
import kotlinx.coroutines.flow.Flow

/**
 * List and detail panes of ArticlesListActivity. The detail pane is shown next to the list only
 * when [TwoPanePolicy] allows it; otherwise only the list is shown, as before.
 *
 * The selected article is the content key of the navigator, so it survives configuration
 * changes. When the window becomes too small for two panes, the selection is cleared and only
 * the list is shown; when it becomes large again, the detail pane shows the empty state.
 *
 * @param detailRequests articles to show in the detail pane, sent by the list
 * @param listPane the list; it gets whether two panes are shown, whether the window is large
 * (see [TwoPanePolicy.isLargeWindow]) and the selected article
 * @param detailPane the detail; it gets the selected article, or null before a selection
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun ArticlesListPanes(
    isOpenInternal: Boolean,
    detailRequests: Flow<SelectedArticle>,
    modifier: Modifier = Modifier,
    listPane: @Composable (isTwoPane: Boolean, isLargeWindow: Boolean, selectedArticle: SelectedArticle?) -> Unit,
    detailPane: @Composable (selectedArticle: SelectedArticle?) -> Unit,
) {
    val windowSize = currentWindowDpSize()
    val isTwoPane = windowSize.canShowTwoPanes(isOpenInternal)
    val isLargeWindow = windowSize.isLargeWindow()
    val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2())
        .toArticlesListDirective(windowSize, isTwoPane)
    val navigator = rememberListDetailPaneScaffoldNavigator<SelectedArticle>(scaffoldDirective = directive)

    LaunchedEffect(navigator) {
        detailRequests.collect { article ->
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, article)
        }
    }
    LaunchedEffect(navigator, isTwoPane) {
        if (!isTwoPane) {
            // Go back to the list only and forget the selection
            while (navigator.canNavigateBack(BackNavigationBehavior.PopLatest)) {
                navigator.navigateBack(BackNavigationBehavior.PopLatest)
            }
        }
    }

    val selectedArticle = navigator.currentDestination?.contentKey
    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        modifier = modifier,
        listPane = {
            AnimatedPane { listPane(isTwoPane, isLargeWindow, selectedArticle) }
        },
        detailPane = {
            AnimatedPane { detailPane(selectedArticle) }
        },
    )
}

private fun DpSize.canShowTwoPanes(isOpenInternal: Boolean): Boolean = TwoPanePolicy.canShowTwoPanes(
    widthDp = width.value.toInt(),
    heightDp = height.value.toInt(),
    isOpenInternal = isOpenInternal,
)

private fun DpSize.isLargeWindow(): Boolean = TwoPanePolicy.isLargeWindow(
    widthDp = width.value.toInt(),
    heightDp = height.value.toInt(),
)

/** Two panes only when eligible, with the list taking about 40% of the width. */
private fun PaneScaffoldDirective.toArticlesListDirective(
    windowSize: DpSize,
    isTwoPane: Boolean,
): PaneScaffoldDirective = copy(
    maxHorizontalPartitions = if (isTwoPane) 2 else 1,
    defaultPanePreferredWidth = windowSize.width * LIST_PANE_WIDTH_FRACTION,
)

private const val LIST_PANE_WIDTH_FRACTION: Float = 0.4f
