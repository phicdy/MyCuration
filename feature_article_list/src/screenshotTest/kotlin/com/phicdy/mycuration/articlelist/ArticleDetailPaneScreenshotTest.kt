package com.phicdy.mycuration.articlelist

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.phicdy.mycuration.articlelist.ui.ArticleDetailPane
import com.phicdy.mycuration.resource.MyCurationTheme

/**
 * Screenshots of the detail pane of the two-pane article list. WebView does not render in the
 * preview renderer, so the page is a placeholder.
 */
@PreviewTest
@Preview(name = "Light", widthDp = 540, heightDp = 600, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 540, heightDp = 600, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ArticleDetailPaneEmptyPreview() {
    ArticleDetailPaneSample(article = null)
}

@PreviewTest
@Preview(name = "Light", widthDp = 540, heightDp = 600, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 540, heightDp = 600, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ArticleDetailPaneSelectedPreview() {
    ArticleDetailPaneSample(
        article = SelectedArticle(
            id = 1,
            title = "Selected article title that is long enough to be ellipsized in the top bar",
            url = "https://example.com/articles/1",
        ),
    )
}

@Composable
private fun ArticleDetailPaneSample(article: SelectedArticle?) {
    MyCurationTheme {
        ArticleDetailPane(
            article = article,
            onShareClick = {},
            onOpenInBrowserClick = {},
            modifier = Modifier.background(windowBackgroundColor(LocalContext.current)),
            webContent = { selected ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = selected.url)
                }
            },
        )
    }
}
