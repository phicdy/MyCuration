package com.phicdy.mycuration.articlelist

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
import com.phicdy.mycuration.articlelist.action.UpdateFavoriteStatusActionCreator
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.FavoritableArticle
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.glide.GlideApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.security.InvalidParameterException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ArticleListAdapter(
        private val coroutineScope: CoroutineScope,
        private val listener: Listener,
        private val adProvider: AdProvider,
        private val updateFavoriteStatusActionCreator: UpdateFavoriteStatusActionCreator
) : ListAdapter<ArticleItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_FOOTER -> {
                val footer = LayoutInflater.from(parent.context)
                        .inflate(R.layout.footer_article_list_activity, parent, false)
                FooterViewHolder(footer)
            }
            VIEW_TYPE_ARTICLE -> {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.articles_list, parent, false)
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
                        listener.onItemClicked(holder.getAdapterPosition(), currentList)
                    }
                }
                holder.mView.setOnLongClickListener {
                    listener.onItemLongClicked(holder.getAdapterPosition(), currentList)
                    true
                }

                val content = getItem(position) as? ArticleItem.Content
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
                val color = if (article.status == Article.TOREAD || article.status == Article.READ) {
                    ContextCompat.getColor(holder.itemView.context, R.color.text_read)
                } else {
                    ContextCompat.getColor(holder.itemView.context, R.color.text_primary)
                }
                holder.articleTitle.setTextColor(color)
                holder.articlePostedTime.setTextColor(color)
                holder.articlePoint.setTextColor(color)
                holder.feedTitleView.setTextColor(color)

                holder.favoriteOnIcon.visibility = if (article.isFavorite) View.VISIBLE else View.GONE
                holder.favoriteOnIcon.setOnClickListener {
                    holder.favoriteOffIcon.visibility = View.VISIBLE
                    holder.favoriteOnIcon.visibility = View.GONE
                    coroutineScope.launch {
                        updateFavoriteStatusActionCreator.run(position, currentList)
                    }
                }

                holder.favoriteOffIcon.visibility = if (article.isFavorite) View.GONE else View.VISIBLE
                holder.favoriteOffIcon.setOnClickListener {
                    holder.favoriteOffIcon.visibility = View.GONE
                    holder.favoriteOnIcon.visibility = View.VISIBLE
                    coroutineScope.launch {
                        updateFavoriteStatusActionCreator.run(position, currentList)
                    }
                }
            }

            is AdViewHolder -> holder.bind()
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is ArticleItem.Content -> VIEW_TYPE_ARTICLE
        is ArticleItem.Advertisement -> VIEW_TYPE_AD
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
        val favoriteOnIcon: ImageView = mView.findViewById(R.id.favoriteOn)
        val favoriteOffIcon: ImageView = mView.findViewById(R.id.favoriteOff)
    }

    interface Listener {
        fun onItemClicked(position: Int, articles: List<ArticleItem>)
        fun onItemLongClicked(position: Int, articles: List<ArticleItem>)
    }

    companion object {
        private const val VIEW_TYPE_ARTICLE = 0
        private const val VIEW_TYPE_FOOTER = 1
        private const val VIEW_TYPE_AD = 2
    }
}


private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ArticleItem>() {
    override fun areItemsTheSame(oldItem: ArticleItem, newItem: ArticleItem): Boolean {
        return when (newItem) {
            is ArticleItem.Content -> {
                when (oldItem) {
                    is ArticleItem.Content -> oldItem.value.id == newItem.value.id
                    is ArticleItem.Advertisement -> false
                }
            }
            is ArticleItem.Advertisement -> {
                when (oldItem) {
                    is ArticleItem.Content -> false
                    is ArticleItem.Advertisement -> newItem == oldItem
                }
            }
        }
    }

    override fun areContentsTheSame(oldItem: ArticleItem, newItem: ArticleItem): Boolean {
        return when (newItem) {
            is ArticleItem.Content -> {
                when (oldItem) {
                    is ArticleItem.Content -> oldItem.value == newItem.value
                    is ArticleItem.Advertisement -> false
                }
            }
            is ArticleItem.Advertisement -> {
                when (oldItem) {
                    is ArticleItem.Content -> false
                    is ArticleItem.Advertisement -> true
                }
            }
        }
    }

}

sealed class ArticleItem {
    data class Content(val value: FavoritableArticle) : ArticleItem()
    object Advertisement : ArticleItem()
}
