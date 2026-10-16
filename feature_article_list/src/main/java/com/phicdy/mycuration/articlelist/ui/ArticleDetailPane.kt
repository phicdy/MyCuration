package com.phicdy.mycuration.articlelist.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.phicdy.mycuration.articlelist.R
import com.phicdy.mycuration.articlelist.SelectedArticle

/**
 * Detail pane of the two-pane article list: the selected article in a WebView with a top bar,
 * or a hint to select an article.
 *
 * [webContent] shows the page; it can be replaced in previews, where WebView does not render.
 */
@Composable
internal fun ArticleDetailPane(
    article: SelectedArticle?,
    onShareClick: (SelectedArticle) -> Unit,
    onOpenInBrowserClick: (SelectedArticle) -> Unit,
    modifier: Modifier = Modifier,
    webContent: @Composable (SelectedArticle) -> Unit = { ArticleWebView(url = it.url) },
) {
    if (article == null) {
        NoSelectedArticle(modifier = modifier)
        return
    }
    Column(modifier = modifier.fillMaxSize()) {
        ArticleDetailTopBar(
            title = article.title,
            onShareClick = { onShareClick(article) },
            onOpenInBrowserClick = { onOpenInBrowserClick(article) },
        )
        Box(modifier = Modifier.weight(1f)) {
            webContent(article)
        }
    }
}

@Composable
private fun NoSelectedArticle(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.select_article),
            modifier = Modifier.padding(16.dp),
            color = colorResource(R.color.text_read),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ArticleDetailTopBar(
    title: String,
    onShareClick: () -> Unit,
    onOpenInBrowserClick: () -> Unit,
) {
    val contentColor = colorResource(R.color.text_primary)
    TopAppBar(
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        actions = {
            IconButton(onClick = onShareClick) {
                Image(
                    painter = painterResource(R.drawable.ic_share),
                    contentDescription = stringResource(R.string.share),
                )
            }
            IconButton(onClick = onOpenInBrowserClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = stringResource(R.string.open_in_browser),
                    tint = contentColor,
                )
            }
        },
        backgroundColor = colorResource(R.color.background_toolbar),
        contentColor = contentColor,
    )
}

/**
 * WebView that loads [url]. Back goes through the page history first; when there is no history
 * left, back is not handled here and goes to the screen. A new URL gets a new WebView, so the
 * history of the previous article is not kept.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun ArticleWebView(url: String, modifier: Modifier = Modifier) {
    key(url) {
        var webView by remember { mutableStateOf<WebView?>(null) }
        var canGoBack by remember { mutableStateOf(false) }
        BackHandler(enabled = canGoBack) { webView?.goBack() }
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                            canGoBack = view.canGoBack()
                        }
                    }
                    loadUrl(url)
                    webView = this
                }
            },
            modifier = modifier.fillMaxSize(),
            onRelease = { it.destroy() },
        )
    }
}
