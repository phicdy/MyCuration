package com.phicdy.mycuration.articlelist

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.phicdy.mycuration.articlelist.ui.ArticleRow
import com.phicdy.mycuration.articlelist.ui.ArticleRowUi
import com.phicdy.mycuration.resource.MyCurationTheme

/**
 * Screenshots of the Compose article row with the same sample data and background as
 * ArticleRowViewScreenshotTest, so that the two images can be compared side by side.
 */
@PreviewTest
@Preview(name = "Light", widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ArticleRowComposePreview() {
    MyCurationTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(windowBackgroundColor(LocalContext.current)),
        ) {
            sampleRows.forEach { row ->
                ArticleRow(row = row, onClick = {}, onLongClick = {}, onFavoriteClick = {})
            }
        }
    }
}

/** Same background as the activity window, which the View based row is drawn on. */
internal fun windowBackgroundColor(context: Context): Color {
    val themedContext = ContextThemeWrapper(context, R.style.AppTheme)
    val typedArray = themedContext.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
    val drawable = typedArray.getDrawable(0)
    typedArray.recycle()
    return if (drawable is ColorDrawable) Color(drawable.color) else Color.Transparent
}

// Same values as ArticleRowViewScreenshotTest (2019/01/01 03:00:00 UTC)
private const val SAMPLE_POSTED_DATE: Long = 1546311600000L

private val sampleRows: List<ArticleRowUi.Content> = listOf(
    ArticleRowUi.Content(
        id = 1,
        title = "Unread article title that is long enough to wrap onto a second line in the list",
        url = "https://example.com/articles/1",
        isRead = false,
        isFavorite = false,
        postedDate = SAMPLE_POSTED_DATE,
        point = "128",
        feedId = 1,
        feedTitle = "Sample RSS feed",
        feedIconPath = null,
    ),
    ArticleRowUi.Content(
        id = 2,
        title = "Read article title",
        url = "https://example.com/articles/2",
        isRead = true,
        isFavorite = false,
        postedDate = SAMPLE_POSTED_DATE,
        point = null,
        feedId = 1,
        feedTitle = "Sample RSS feed",
        feedIconPath = null,
    ),
    ArticleRowUi.Content(
        id = 3,
        title = "Article without feed",
        url = "https://example.com/articles/3",
        isRead = false,
        isFavorite = false,
        postedDate = SAMPLE_POSTED_DATE,
        point = "3",
        feedId = 2,
        feedTitle = null,
        feedIconPath = null,
    ),
)
