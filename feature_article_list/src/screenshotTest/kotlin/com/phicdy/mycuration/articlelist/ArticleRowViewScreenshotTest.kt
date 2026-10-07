package com.phicdy.mycuration.articlelist

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.android.tools.screenshot.PreviewTest
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.FavoritableArticle
import com.phicdy.mycuration.entity.Feed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Baseline screenshots of the current View based article row (articles_list.xml).
 * Used to check visual parity while the article list is migrated to Compose.
 */
@PreviewTest
@Preview(name = "Light", widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ArticleRowViewPreview() {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            val themedContext = ContextThemeWrapper(context, R.style.AppTheme)
            val container = LinearLayout(themedContext).apply {
                orientation = LinearLayout.VERTICAL
                background = windowBackground(themedContext)
            }
            sampleArticles.forEach { article ->
                val row = LayoutInflater.from(themedContext)
                    .inflate(R.layout.articles_list, container, false)
                bindArticleRow(row, article)
                container.addView(row)
            }
            container
        }
    )
}

private fun windowBackground(context: Context): Drawable? {
    val typedArray = context.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
    val drawable = typedArray.getDrawable(0)
    typedArray.recycle()
    return drawable
}

/**
 * Mirrors ArticleListAdapter.onBindViewHolder for an article row.
 * The feed icon always uses the default drawable because Glide can not load images in layoutlib.
 */
private fun bindArticleRow(row: View, article: FavoritableArticle) {
    val context = row.context
    val articleTitle: TextView = row.findViewById(R.id.articleTitle)
    val articlePostedTime: TextView = row.findViewById(R.id.articlePostedTime)
    val articlePoint: TextView = row.findViewById(R.id.articlePoint)
    val articleUrl: TextView = row.findViewById(R.id.tv_articleUrl)
    val feedTitleView: TextView = row.findViewById(R.id.feedTitle)
    val feedIconView: ImageView = row.findViewById(R.id.iv_feed_icon)
    val favoriteOnIcon: ImageView = row.findViewById(R.id.favoriteOn)
    val favoriteOffIcon: ImageView = row.findViewById(R.id.favoriteOff)

    articleTitle.text = article.title
    articleUrl.text = article.url

    // Default time zone, same as the adapter and the Compose row, so both screenshots match
    articlePostedTime.text = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
        .format(Date(article.postedDate))

    articlePoint.text = if (article.point == Article.DEDAULT_HATENA_POINT) {
        context.getString(R.string.not_get_hatena_point)
    } else {
        article.point
    }

    if (article.feedTitle.isEmpty()) {
        feedTitleView.visibility = View.GONE
        feedIconView.visibility = View.GONE
    } else {
        feedTitleView.text = article.feedTitle
        feedIconView.setImageResource(R.drawable.ic_rss)
    }

    val color = if (article.status == Article.READ) {
        ContextCompat.getColor(context, R.color.text_read)
    } else {
        ContextCompat.getColor(context, R.color.text_primary)
    }
    articleTitle.setTextColor(color)
    articlePostedTime.setTextColor(color)
    articlePoint.setTextColor(color)
    feedTitleView.setTextColor(color)

    favoriteOnIcon.visibility = if (article.isFavorite) View.VISIBLE else View.GONE
    favoriteOffIcon.visibility = if (article.isFavorite) View.GONE else View.VISIBLE
}

// 2019/01/01 03:00:00 UTC
private const val SAMPLE_POSTED_DATE = 1546311600000L

private val sampleArticles: List<FavoritableArticle> = listOf(
    FavoritableArticle(
        id = 1,
        title = "Unread article title that is long enough to wrap onto a second line in the list",
        url = "https://example.com/articles/1",
        status = Article.UNREAD,
        point = "128",
        postedDate = SAMPLE_POSTED_DATE,
        feedId = 1,
        feedTitle = "Sample RSS feed",
        feedIconPath = Feed.DEDAULT_ICON_PATH,
        isFavorite = false
    ),
    FavoritableArticle(
        id = 2,
        title = "Read article title",
        url = "https://example.com/articles/2",
        status = Article.READ,
        point = Article.DEDAULT_HATENA_POINT,
        postedDate = SAMPLE_POSTED_DATE,
        feedId = 1,
        feedTitle = "Sample RSS feed",
        feedIconPath = Feed.DEDAULT_ICON_PATH,
        isFavorite = false
    ),
    FavoritableArticle(
        id = 3,
        title = "Article without feed",
        url = "https://example.com/articles/3",
        status = Article.UNREAD,
        point = "3",
        postedDate = SAMPLE_POSTED_DATE,
        feedId = 2,
        feedTitle = "",
        feedIconPath = Feed.DEDAULT_ICON_PATH,
        isFavorite = false
    )
)
