package com.phicdy.mycuration.rss

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.phicdy.mycuration.glide.GlideApp
import com.phicdy.mycuration.rss.databinding.ItemAllRssBinding
import com.phicdy.mycuration.rss.databinding.ItemFavoriteBinding
import com.phicdy.mycuration.rss.databinding.ItemRssBinding
import com.phicdy.mycuration.rss.databinding.ItemRssFooterBinding
import java.security.InvalidParameterException

class RssListAdapter(
    private val mListener: OnFeedListFragmentListener?
) : ListAdapter<RssListItem, RecyclerView.ViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ALL_RSS -> {
                AllRssViewHolder(ItemAllRssBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            VIEW_TYPE_FAVORITE -> {
                FavoriteViewHolder(ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            VIEW_TYPE_RSS -> {
                RssViewHolder(ItemRssBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            VIEW_TYPE_FOOTER -> {
                RssFooterView(ItemRssFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
                    mListener?.onFooterClicked()
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
            private val binding: ItemAllRssBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun setUnreadCount(count: String) {
            binding.allUnreadCount.text = count
        }
    }

    private class FavoriteViewHolder(
            binding: ItemFavoriteBinding
    ) : RecyclerView.ViewHolder(binding.root)

    private inner class RssViewHolder(
            binding: ItemRssBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val feedIcon = binding.feedIcon
        private val feedTitle = binding.feedTitle
        private val feedCount = binding.feedCount

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
            binding: ItemRssFooterBinding
    ) : RecyclerView.ViewHolder(binding.root), RssItemView.Footer {
        private val title = binding.tvRssFooterTitle
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