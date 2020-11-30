package com.phicdy.mycuration.presentation.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.glide.GlideApp
import com.phicdy.mycuration.legacy.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class SelectFilterTargetRssFragment : Fragment(), CoroutineScope {

    private var selectedList = mutableListOf<Feed>()
    private lateinit var recyclerView: RecyclerView

    private val rssRepository: RssRepository by inject()

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_select_filter_target_rss, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            launch {
                val adapter = TargetRssListAdapter(rssRepository.getAllFeedsWithoutNumOfUnreadArticles())
                recyclerView = it.findViewById(R.id.rv_fiter_target) as RecyclerView
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(activity)
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    fun list(): List<Feed>? {
        return selectedList
    }

    fun updateSelected(selectedList: MutableList<Feed>) {
        this.selectedList = selectedList
    }

    private inner class TargetRssListAdapter(private val feeds: List<Feed>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.filter_target_rss_list, parent, false)
            return ViewHolder(itemView)
        }

        override fun getItemCount(): Int {
            return feeds.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val feed = feeds[position]
            if (holder is ViewHolder) {
                holder.tvRssTitle.text = feed.title
                val iconPath = feed.iconPath
                if (iconPath == Feed.DEDAULT_ICON_PATH) {
                    holder.ivIcon.setImageResource(R.drawable.ic_rss)
                } else {
                    GlideApp.with(this@SelectFilterTargetRssFragment)
                            .load(iconPath)
                            .placeholder(R.drawable.ic_rss)
                            .circleCrop()
                            .error(R.drawable.ic_rss)
                            .into(holder.ivIcon)
                }

                var isChecked = false
                if (selectedList.size > 0) {
                    for (selectedFeed in selectedList) {
                        if (feed.id == selectedFeed.id) {
                            isChecked = true
                            break
                        }
                    }
                }
                holder.cbSelect.isChecked = isChecked
                holder.cbSelect.setOnClickListener { v ->
                    val checkBox = v as CheckBox
                    if (checkBox.isChecked) {
                        checkFeed(position)
                    } else {
                        uncheckFeed(position)
                    }
                }

                holder.itemView.setOnClickListener {
                    val newChecked = !holder.cbSelect.isChecked
                    holder.cbSelect.isChecked = newChecked
                    if (newChecked) {
                        checkFeed(position)
                    } else {
                        uncheckFeed(position)
                    }
                }
            }
        }

        private fun uncheckFeed(position: Int) {
            val selected = feeds[position]
            val iterator = selectedList.iterator()
            while (iterator.hasNext()) {
                val feed = iterator.next()
                if (selected.id == feed.id) {
                    iterator.remove()
                    break
                }
            }
        }

        private fun checkFeed(position: Int) {
            val selected = feeds[position]
            val iterator = selectedList.iterator()
            var isExist = false
            while (iterator.hasNext()) {
                val feed = iterator.next()
                if (selected.id == feed.id) {
                    isExist = true
                    break
                }
            }
            if (!isExist) {
                selectedList.add(selected)
            }
        }

        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val cbSelect = itemView.findViewById(R.id.cb_target) as CheckBox
            val ivIcon = itemView.findViewById(R.id.iv_rss_icon) as ImageView
            val tvRssTitle = itemView.findViewById(R.id.tv_rss_title) as TextView
        }
    }
}
