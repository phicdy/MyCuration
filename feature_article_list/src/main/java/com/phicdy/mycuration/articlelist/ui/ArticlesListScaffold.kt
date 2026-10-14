package com.phicdy.mycuration.articlelist.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import com.phicdy.mycuration.articlelist.R

/**
 * Screen frame of ArticlesListActivity: top app bar with the "All read" and search actions,
 * the FAB that scrolls down and marks articles as read, and a snackbar host.
 */
@Composable
internal fun ArticlesListScaffold(
    title: String,
    snackbarHostState: SnackbarHostState,
    onNavigateUp: () -> Unit,
    onReadAllClick: () -> Unit,
    onSearch: (query: String) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier,
    searchState: ArticlesListSearchState = rememberArticlesListSearchState(),
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        scaffoldState = rememberScaffoldState(snackbarHostState = snackbarHostState),
        topBar = {
            ArticlesListTopBar(
                title = title,
                searchState = searchState,
                onNavigateUp = onNavigateUp,
                onReadAllClick = onReadAllClick,
                onSearch = onSearch,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                backgroundColor = colorResource(R.color.accent),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_down),
                    contentDescription = stringResource(R.string.scroll_article_list),
                    alpha = ICON_ALPHA,
                )
            }
        },
        content = content,
    )
}

/**
 * Top app bar. The search action replaces the title with a text field, like the former
 * collapsible SearchView.
 */
@Composable
private fun ArticlesListTopBar(
    title: String,
    searchState: ArticlesListSearchState,
    onNavigateUp: () -> Unit,
    onReadAllClick: () -> Unit,
    onSearch: (query: String) -> Unit,
) {
    BackHandler(enabled = searchState.isSearching, onBack = searchState::cancelSearch)
    TopAppBar(
        title = {
            if (searchState.isSearching) {
                SearchField(
                    query = searchState.query,
                    onQueryChange = searchState::updateQuery,
                    onSubmit = {
                        if (searchState.query.isNotBlank()) {
                            onSearch(searchState.query)
                            searchState.cancelSearch()
                        }
                    },
                )
            } else {
                Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        navigationIcon = {
            IconButton(onClick = if (searchState.isSearching) searchState::cancelSearch else onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_up),
                )
            }
        },
        actions = {
            if (!searchState.isSearching) {
                ToolbarAction(R.drawable.ic_check, R.string.all_read, onReadAllClick)
                ToolbarAction(R.drawable.ic_search, R.string.search_article, searchState::startSearch)
            }
        },
        backgroundColor = colorResource(R.color.background_toolbar),
        contentColor = colorResource(R.color.text_primary),
    )
}

/**
 * Whether the top app bar shows the search field, and the query being typed.
 */
@Stable
internal class ArticlesListSearchState(isSearching: Boolean, query: String) {
    var isSearching: Boolean by mutableStateOf(isSearching)
        private set
    var query: String by mutableStateOf(query)
        private set

    fun startSearch() {
        isSearching = true
    }

    fun cancelSearch() {
        isSearching = false
        query = ""
    }

    fun updateQuery(newQuery: String) {
        query = newQuery
    }

    companion object {
        val Saver: Saver<ArticlesListSearchState, Any> = listSaver(
            save = { listOf(it.isSearching, it.query) },
            restore = { ArticlesListSearchState(isSearching = it[0] as Boolean, query = it[1] as String) },
        )
    }
}

@Composable
internal fun rememberArticlesListSearchState(
    isSearching: Boolean = false,
    query: String = "",
): ArticlesListSearchState = rememberSaveable(saver = ArticlesListSearchState.Saver) {
    ArticlesListSearchState(isSearching = isSearching, query = query)
}

@Composable
private fun ToolbarAction(
    @DrawableRes icon: Int,
    @StringRes description: Int,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Image(
            painter = painterResource(icon),
            contentDescription = stringResource(description),
            alpha = ICON_ALPHA,
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    // The former SearchView used colorPrimary for the text and the hint
    val textColor = colorResource(R.color.primary)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        textStyle = MaterialTheme.typography.body1.copy(color = textColor),
        singleLine = true,
        cursorBrush = SolidColor(textColor),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
        decorationBox = { innerTextField ->
            Box {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_article),
                        style = MaterialTheme.typography.body1,
                        color = textColor.copy(alpha = ContentAlpha.medium),
                    )
                }
                innerTextField()
            }
        },
    )
}

// Compose ignores android:alpha on the <vector> root of the toolbar and FAB icons
private const val ICON_ALPHA: Float = 0.6f
