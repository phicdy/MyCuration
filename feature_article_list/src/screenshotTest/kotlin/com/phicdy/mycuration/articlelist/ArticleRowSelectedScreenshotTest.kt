package com.phicdy.mycuration.articlelist

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.phicdy.mycuration.articlelist.ui.ArticleRow
import com.phicdy.mycuration.articlelist.ui.ArticleRowUi
import com.phicdy.mycuration.resource.MyCurationTheme

/** The row of the article shown in the detail pane is highlighted. */
@PreviewTest
@Preview(name = "Light", widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ArticleRowSelectedPreview() {
    MyCurationTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(windowBackgroundColor(LocalContext.current)),
        ) {
            listOf(false, true, false).forEachIndexed { index, isSelected ->
                ArticleRow(
                    row = ArticleRowUi.Content(
                        id = index,
                        title = if (isSelected) "Selected article" else "Article $index",
                        url = "https://example.com/articles/$index",
                        isRead = false,
                        isFavorite = false,
                        // 2019/01/01 03:00:00 UTC
                        postedDate = 1546311600000L,
                        point = "1",
                        feedId = 1,
                        feedTitle = "Sample RSS feed",
                        feedIconPath = null,
                    ),
                    onClick = {},
                    onLongClick = {},
                    onFavoriteClick = {},
                    isSelected = isSelected,
                )
            }
        }
    }
}
