package com.phicdy.mycuration.curatedarticlelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.phicdy.mycuration.advertisement.AdProvider
import com.phicdy.mycuration.advertisement.AdViewHolder
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.feature_curated_article_list.R
import com.phicdy.mycuration.glide.GlideApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.security.InvalidParameterException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CuratedArticleListAdapter(
        private val coroutineScope: CoroutineScope,
        private val listener: Listener,
        private val adProvider: AdProvider
) : ListAdapter<CuratedArticleItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_FOOTER -> {
                val footer = LayoutInflater.from(parent.context)
                        .inflate(R.layout.footer_curated_article_list, parent, false)
                FooterViewHolder(footer)
            }
            VIEW_TYPE_ARTICLE -> {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_curated_articles_list, parent, false)
                ArticleViewHolder(view)
            }
            VIEW_TYPE_AD -> {
                adProvider.newViewHolderInstance(parent)
            }
            else -> throw InvalidParameterException("Invalid view type for article list")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ArticleViewHolder -> {
                holder.mView.setOnClickListener {
                    coroutineScope.launch {
                        listener.onItemClicked(holder.bindingAdapterPosition, currentList)
                    }
                }
                holder.mView.setOnLongClickListener {
                    listener.onItemLongClicked(holder.bindingAdapterPosition, currentList)
                    true
                }

                val content = getItem(position) as? CuratedArticleItem.Content
                        ?: throw IllegalStateException()
                val article = content.value

                holder.articleTitle.text = article.title
                holder.articleUrl.text = article.url

                val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
                val dateString = format.format(Date(article.postedDate))
                holder.articlePostedTime.text = dateString

                // Set RSS Feed unread article count
                holder.articlePoint.text = if (article.point == Article.DEDAULT_HATENA_POINT) {
                    holder.itemView.context.getString(R.string.not_get_hatena_point)
                } else {
                    article.point
                }

                if (article.feedTitle.isEmpty()) {
                    holder.feedTitleView.visibility = View.GONE
                    holder.feedIconView.visibility = View.GONE
                } else {
                    holder.feedTitleView.text = article.feedTitle

                    val iconPath = article.feedIconPath
                    if (iconPath.isNotBlank() && iconPath != Feed.DEDAULT_ICON_PATH) {
                        GlideApp.with(holder.feedIconView)
                                .load(article.feedIconPath)
                                .placeholder(R.drawable.ic_rss)
                                .circleCrop()
                                .error(R.drawable.ic_rss)
                                .into(holder.feedIconView)
                    } else {
                        holder.feedIconView.setImageResource(R.drawable.ic_rss)
                    }
                }

                // Change color if already be read
                val color = if (article.status == Article.READ) {
                    ContextCompat.getColor(holder.itemView.context, R.color.text_read)
                } else {
                    ContextCompat.getColor(holder.itemView.context, R.color.text_primary)
                }
                holder.articleTitle.setTextColor(color)
                holder.articlePostedTime.setTextColor(color)
                holder.articlePoint.setTextColor(color)
                holder.feedTitleView.setTextColor(color)
            }

            is AdViewHolder -> holder.bind()
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is CuratedArticleItem.Content -> VIEW_TYPE_ARTICLE
        is CuratedArticleItem.Advertisement -> VIEW_TYPE_AD
    }

    private class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private class ArticleViewHolder(
            val mView: View
    ) : RecyclerView.ViewHolder(mView) {
        val articleTitle: TextView = mView.findViewById(R.id.articleTitle) as TextView
        val articlePostedTime: TextView = mView.findViewById(R.id.articlePostedTime) as TextView
        val articlePoint: TextView = mView.findViewById(R.id.articlePoint) as TextView
        val articleUrl: TextView = mView.findViewById(R.id.tv_articleUrl) as TextView
        val feedTitleView: TextView = mView.findViewById(R.id.feedTitle) as TextView
        val feedIconView: ImageView = mView.findViewById(R.id.iv_feed_icon) as ImageView
    }

    interface Listener {
        fun onItemClicked(position: Int, articles: List<CuratedArticleItem>)
        fun onItemLongClicked(position: Int, articles: List<CuratedArticleItem>)
    }

    companion object {
        private const val VIEW_TYPE_ARTICLE = 0
        private const val VIEW_TYPE_FOOTER = 1
        private const val VIEW_TYPE_AD = 2
    }
}


private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CuratedArticleItem>() {
    override fun areItemsTheSame(oldItem: CuratedArticleItem, newItem: CuratedArticleItem): Boolean {
        return when (newItem) {
            is CuratedArticleItem.Content -> {
                when (oldItem) {
                    is CuratedArticleItem.Content -> oldItem.value.id == newItem.value.id
                    is CuratedArticleItem.Advertisement -> false
                }
            }
            is CuratedArticleItem.Advertisement -> {
                when (oldItem) {
                    is CuratedArticleItem.Content -> false
                    is CuratedArticleItem.Advertisement -> newItem == oldItem
                }
            }
        }
    }

    override fun areContentsTheSame(oldItem: CuratedArticleItem, newItem: CuratedArticleItem): Boolean {
        return when (newItem) {
            is CuratedArticleItem.Content -> {
                when (oldItem) {
                    is CuratedArticleItem.Content -> oldItem.value == newItem.value
                    is CuratedArticleItem.Advertisement -> false
                }
            }
            is CuratedArticleItem.Advertisement -> {
                when (oldItem) {
                    is CuratedArticleItem.Content -> false
                    is CuratedArticleItem.Advertisement -> true
                }
            }
        }
    }

}

sealed class CuratedArticleItem {
    data class Content(val value: Article) : CuratedArticleItem()
    object Advertisement : CuratedArticleItem()
}
