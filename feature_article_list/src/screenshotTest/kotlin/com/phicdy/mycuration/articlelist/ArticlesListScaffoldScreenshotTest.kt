package com.phicdy.mycuration.articlelist

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.phicdy.mycuration.articlelist.ui.ArticlesListScaffold
import com.phicdy.mycuration.articlelist.ui.EmptyArticleList
import com.phicdy.mycuration.resource.MyCurationTheme

/**
 * Screenshots of the Compose frame of ArticlesListActivity: top app bar, FAB and content area.
 */
@PreviewTest
@Preview(name = "Light", widthDp = 360, heightDp = 400, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 360, heightDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ArticlesListScaffoldPreview() {
    MyCurationTheme {
        ArticlesListScaffold(
            title = stringResource(R.string.all),
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateUp = {},
            onReadAllClick = {},
            onSearch = {},
            onFabClick = {},
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                EmptyArticleList(text = stringResource(R.string.no_article))
            }
        }
    }
}
