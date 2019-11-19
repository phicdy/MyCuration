package com.phicdy.mycuration.rss

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.phicdy.mycuration.glide.GlideApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.security.InvalidParameterException

class RssListAdapter(
        private val coroutineScope: CoroutineScope,
        private val changeRssListModeActionCreator: ChangeRssListModeActionCreator,
        private val rssListStateStore: RSSListStateStore,
        private val mListener: RssListFragment.OnFeedListFragmentListener?
) : ListAdapter<RssListItem, RecyclerView.ViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ALL_RSS -> {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_all_rss, parent, false)
                AllRssViewHolder(view)
            }
            VIEW_TYPE_FAVORITE -> {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_favorite, parent, false)
                FavoriteViewHolder(view)
            }
            VIEW_TYPE_RSS -> {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.feeds_list, parent, false)
                RssViewHolder(view)
            }
            VIEW_TYPE_FOOTER -> {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_item_rss_footer, parent, false)
                RssFooterView(view)
            }
            else -> throw InvalidParameterException("Invalid view type")
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is RssListItem.All -> VIEW_TYPE_ALL_RSS
        is RssListItem.Favroite -> VIEW_TYPE_FAVORITE
        is RssListItem.Content -> VIEW_TYPE_RSS
        is RssListItem.Footer -> VIEW_TYPE_FOOTER
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AllRssViewHolder -> {
                val item = getItem(position) as RssListItem.All
                holder.setUnreadCount(item.unreadCount.toString())
                holder.itemView.setOnClickListener {
                    mListener?.onAllUnreadClicked()
                }
            }
            is FavoriteViewHolder -> {
                holder.itemView.setOnClickListener {
                    mListener?.onFavoriteClicked()
                }
            }
            is RssViewHolder -> {
                when (val item = getItem(position)) {
                    is RssListItem.Content -> {
                        holder.itemView.setOnClickListener {
                            val feedId = item.rssId
                            if (feedId != -1) mListener?.onListClicked(feedId)
                        }
                        holder.itemView.setOnCreateContextMenuListener { menu, _, _ ->
                            menu.add(0, EDIT_FEED_TITLE_MENU_ID, 0, R.string.edit_rss_title).setOnMenuItemClickListener {
                                mListener?.onEditRssClicked(item.rssId, item.rssTitle)
                                true
                            }
                            menu.add(0, DELETE_FEED_MENU_ID, 1, R.string.delete_rss).setOnMenuItemClickListener {
                                mListener?.onDeleteRssClicked(item.rssId, position)
                                true
                            }
                        }
                        if (item.isDefaultIcon) {
                            holder.showDefaultIcon()
                        } else {
                            holder.showIcon(item.rssIconPath)
                        }
                        holder.updateTitle(item.rssTitle)
                        holder.updateUnreadCount(item.unreadCount.toString())
                    }
                    is RssListItem.Footer -> throw IllegalStateException()
                }
            }
            is RssFooterView -> {
                holder.itemView.setOnClickListener {
                    rssListStateStore.state.value?.let {
                        coroutineScope.launch {
                            changeRssListModeActionCreator.run(it)
                        }
                    }
                }
                when (val item = getItem(position)) {
                    is RssListItem.Content -> throw IllegalStateException()
                    is RssListItem.Footer -> {
                        when (item.state) {
                            RssListFooterState.UNREAD_ONLY -> holder.showAllView()
                            RssListFooterState.ALL -> holder.showHideView()
                        }
                    }
                }
            }
        }
    }

    private class AllRssViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        fun setUnreadCount(count: String) {
            itemView.findViewById<TextView>(R.id.allUnreadCount).text = count
        }
    }

    private class FavoriteViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView)

    private inner class RssViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val feedIcon = itemView.findViewById(R.id.feedIcon) as ImageView
        private val feedTitle = itemView.findViewById(R.id.feedTitle) as TextView
        private val feedCount = itemView.findViewById(R.id.feedCount) as TextView

        fun showDefaultIcon() {
            feedIcon.setImageResource(R.drawable.ic_rss)
        }

        fun showIcon(iconPath: String) {
            GlideApp.with(feedIcon.context)
                    .load(iconPath)
                    .placeholder(R.drawable.ic_rss)
                    .circleCrop()
                    .error(R.drawable.ic_rss)
                    .into(feedIcon)
        }

        fun updateTitle(title: String) {
            feedTitle.text = title
        }

        fun updateUnreadCount(count: String) {
            feedCount.text = count
        }
    }

    private inner class RssFooterView(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView), RssItemView.Footer {
        internal val title = itemView.findViewById<TextView>(R.id.tv_rss_footer_title)
        override fun showAllView() {
            title.setText(R.string.show_all_rsses)
        }

        override fun showHideView() {
            title.setText(R.string.hide_rsses)
        }
    }

    companion object {
        private const val DELETE_FEED_MENU_ID = 1000
        private const val EDIT_FEED_TITLE_MENU_ID = 1001

        private const val VIEW_TYPE_ALL_RSS = 0
        private const val VIEW_TYPE_FAVORITE = 1
        private const val VIEW_TYPE_RSS = 2
        private const val VIEW_TYPE_FOOTER = 3
    }
}

private val diffCallback = object : DiffUtil.ItemCallback<RssListItem>() {
    override fun areItemsTheSame(oldItem: RssListItem, newItem: RssListItem): Boolean {
        return when (oldItem) {
            is RssListItem.Content -> {
                when (newItem) {
                    is RssListItem.Content -> oldItem.rssId == newItem.rssId
                    else -> false
                }
            }
            is RssListItem.Footer -> {
                when (newItem) {
                    is RssListItem.Footer -> oldItem.state == newItem.state
                    else -> false
                }
            }
            is RssListItem.All -> {
                when (newItem) {
                    is RssListItem.All -> oldItem.unreadCount == newItem.unreadCount
                    else -> false
                }
            }
            is RssListItem.Favroite -> {
                when (newItem) {
                    is RssListItem.Favroite -> true
                    else -> false
                }
            }
        }
    }

    override fun areContentsTheSame(oldItem: RssListItem, newItem: RssListItem): Boolean {
        return when (oldItem) {
            is RssListItem.Content -> {
                when (newItem) {
                    is RssListItem.Content -> oldItem == newItem
                    else -> false
                }
            }
            is RssListItem.Footer -> {
                when (newItem) {
                    is RssListItem.Footer -> oldItem == newItem
                    else -> false
                }
            }
            is RssListItem.All -> {
                when (newItem) {
                    is RssListItem.All -> true
                    else -> false
                }
            }
            is RssListItem.Favroite -> {
                when (newItem) {
                    is RssListItem.Favroite -> true
                    else -> false
                }
            }
        }
    }
}