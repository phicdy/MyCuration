package com.phicdy.mycuration.articlelist.ui

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import android.widget.ImageView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalRippleConfiguration
import androidx.compose.material.RippleConfiguration
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.phicdy.mycuration.articlelist.R
import com.phicdy.mycuration.resource.MyCurationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * Compose versions of the article list row (R.layout.articles_list) and the empty view of
 * R.layout.fragment_articles_list. They are not used by production code yet and will replace
 * the RecyclerView in ArticlesListFragment in a later step.
 */

/**
 * Compose version of R.layout.articles_list.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ArticleRow(
    row: ArticleRowUi.Content,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    val textColor = colorResource(if (row.isRead) R.color.text_read else R.color.text_primary)
    val smallTextStyle = viewTextStyle(fontSize = 12.sp, color = textColor)
    // Toggle the star immediately like the former adapter did, before the new list arrives
    var isFavorite by remember(row.isFavorite) { mutableStateOf(row.isFavorite) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp)
            // ViewGroup clips children to its padding by default (clipToPadding)
            .clipToBounds(),
    ) {
        BasicText(
            text = row.title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            style = viewTextStyle(fontSize = 14.sp, color = textColor),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        if (row.feedTitle != null) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FeedIcon(iconPath = row.feedIconPath)
                BasicText(
                    text = row.feedTitle,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .overflowVertically(),
                    style = smallTextStyle,
                )
            }
        }
        BasicText(
            text = row.url,
            modifier = Modifier.padding(top = 8.dp),
            style = viewTextStyle(fontSize = 12.sp, color = colorResource(R.color.text_url)),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = remember(row.postedDate) { formatPostedDate(row.postedDate) },
                style = smallTextStyle,
            )
            Image(
                painter = painterResource(R.drawable.hatena),
                contentDescription = stringResource(R.string.hatena),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .overflowVertically(),
            )
            BasicText(
                text = row.point ?: stringResource(R.string.not_get_hatena_point),
                modifier = Modifier.padding(start = 4.dp),
                style = smallTextStyle,
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(
                    if (isFavorite) R.drawable.ic_favorite_on else R.drawable.ic_favorite_off
                ),
                contentDescription = null,
                modifier = Modifier
                    .overflowVertically()
                    .clickable {
                        isFavorite = !isFavorite
                        onFavoriteClick()
                    }
                    .padding(4.dp),
                // Compose ignores android:alpha on the <vector> root of these drawables
                alpha = FAVORITE_ICON_ALPHA,
            )
        }
        // Divider style: background_base_divider with alpha 0.12
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(colorResource(R.color.background_base_divider).copy(alpha = DIVIDER_ALPHA)),
        )
    }
}

/**
 * Measures the child at its full height but reports zero height and centers the child on that
 * point, so a child taller than its row overflows it without changing the row height. This is
 * how ConstraintLayout placed the favorite star on the date and the feed title on the icon.
 */
private fun Modifier.overflowVertically(): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
    layout(placeable.width, 0) {
        placeable.placeRelative(0, -placeable.height / 2)
    }
}

/**
 * Feed icon loaded by Glide into an ImageView, same as the former adapter.
 */
@Composable
internal fun FeedIcon(iconPath: String?) {
    val description = stringResource(R.string.icon)
    AndroidView(
        factory = { context ->
            ImageView(context).apply { contentDescription = description }
        },
        modifier = Modifier.size(12.dp),
        // Glide ignores an equivalent request for the same target, so re-binding is cheap
        update = { imageView -> bindFeedIcon(imageView, iconPath) },
    )
}

private fun bindFeedIcon(imageView: ImageView, iconPath: String?) {
    if (iconPath == null) {
        Glide.with(imageView).clear(imageView)
        imageView.setImageResource(R.drawable.ic_rss)
    } else {
        Glide.with(imageView)
            .load(iconPath)
            .placeholder(R.drawable.ic_rss)
            .circleCrop()
            .error(R.drawable.ic_rss)
            .into(imageView)
    }
}

/**
 * Same as the TextView in R.layout.fragment_articles_list: full width, centered in the parent,
 * default text appearance (14sp) and the theme text color.
 */
@Composable
internal fun EmptyArticleList(text: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        BasicText(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            style = viewTextStyle(fontSize = 14.sp, color = colorResource(R.color.text_primary))
                .copy(textAlign = TextAlign.Center),
        )
    }
}

/**
 * Ripple that looks like ?android:attr/selectableItemBackground: the color and alpha of
 * android:colorControlHighlight of the current theme (light or dark).
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun rememberPlatformRippleConfiguration(): RippleConfiguration {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(context, configuration) {
        val highlight = resolveColorControlHighlight(context)
        RippleConfiguration(
            color = highlight.copy(alpha = 1f),
            rippleAlpha = RippleAlpha(
                draggedAlpha = highlight.alpha,
                focusedAlpha = highlight.alpha,
                hoveredAlpha = highlight.alpha,
                pressedAlpha = highlight.alpha,
            ),
        )
    }
}

private fun resolveColorControlHighlight(context: Context): Color {
    val typedValue = TypedValue()
    if (!context.theme.resolveAttribute(android.R.attr.colorControlHighlight, typedValue, true)) {
        return DEFAULT_HIGHLIGHT
    }
    return when {
        typedValue.type in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT ->
            Color(typedValue.data)

        typedValue.resourceId != 0 -> Color(ContextCompat.getColor(context, typedValue.resourceId))
        else -> DEFAULT_HIGHLIGHT
    }
}

/**
 * Text style equivalent to a plain TextView: no letter spacing and font padding included.
 */
@Suppress("DEPRECATION")
private fun viewTextStyle(fontSize: TextUnit, color: Color) = TextStyle(
    color = color,
    fontSize = fontSize,
    letterSpacing = 0.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = true),
)

internal fun formatPostedDate(postedDate: Long): String =
    SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US).format(Date(postedDate))

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PreviewContainer(content: @Composable () -> Unit) {
    MyCurationTheme {
        CompositionLocalProvider(
            LocalRippleConfiguration provides rememberPlatformRippleConfiguration(),
        ) {
            Box(modifier = Modifier.background(colorResource(R.color.background_toolbar))) {
                content()
            }
        }
    }
}

private fun previewRow(
    id: Int,
    title: String = "Article title",
    isRead: Boolean = false,
    isFavorite: Boolean = false,
    point: String? = "100",
    feedTitle: String? = "Feed title",
) = ArticleRowUi.Content(
    id = id,
    title = title,
    url = "https://example.com/articles/$id",
    isRead = isRead,
    isFavorite = isFavorite,
    postedDate = PREVIEW_POSTED_DATE,
    point = point,
    feedId = 1,
    feedTitle = feedTitle,
    feedIconPath = null,
)

@Preview(name = "Article rows", showBackground = true)
@Preview(name = "Article rows (dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewArticleRows() {
    PreviewContainer {
        Column {
            listOf(
                previewRow(id = 1),
                previewRow(id = 2, isRead = true, isFavorite = true),
                previewRow(id = 3, point = null, feedTitle = null),
                previewRow(id = 4, title = "Very long article title ".repeat(12)),
            ).forEach { row ->
                ArticleRow(row = row, onClick = {}, onLongClick = {}, onFavoriteClick = {})
            }
        }
    }
}

@Preview(name = "Empty article list", showBackground = true, heightDp = 200)
@Preview(name = "Empty article list (dark)", showBackground = true, heightDp = 200, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewEmptyArticleList() {
    PreviewContainer {
        EmptyArticleList(text = stringResource(R.string.no_article))
    }
}

@Preview(name = "Feed icon", showBackground = true)
@Composable
private fun PreviewFeedIcon() {
    PreviewContainer {
        FeedIcon(iconPath = null)
    }
}

private const val DIVIDER_ALPHA = 0.12f
private const val FAVORITE_ICON_ALPHA = 0.6f
private val DEFAULT_HIGHLIGHT = Color(0x1F000000)

// 2019/01/01 12:00:00 UTC
private const val PREVIEW_POSTED_DATE = 1546344000000L
