package com.phicdy.mycuration.presentation.view.fragment

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.rss.Feed
import java.io.File

class SelectFilterTargetRssFragment : ListFragment() {

    private var selectedList: ArrayList<Feed> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_select_filter_target_rss, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = activity
        val dbAdapter = DatabaseAdapter.getInstance()
        val adapter = TargetRssListAdapter(dbAdapter.allFeedsWithoutNumOfUnreadArticles, context)
        listView.adapter = adapter
    }

    fun list(): ArrayList<Feed>? {
        return selectedList
    }

    fun updateSelected(selectedList: ArrayList<Feed>) {
        this.selectedList = selectedList
    }

    private inner class TargetRssListAdapter internal constructor(feeds: ArrayList<Feed>, context: Context) : ArrayAdapter<Feed>(context, R.layout.filter_target_rss_list, feeds) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            lateinit var holder: ViewHolder

            // Use contentView and setup ViewHolder
            lateinit var row: View
            if (convertView == null) {
                val inflater = activity.layoutInflater
                row = inflater.inflate(R.layout.filter_target_rss_list, parent, false)
                holder = ViewHolder()
                holder.cbSelect = row.findViewById(R.id.cb_target) as CheckBox
                holder.ivIcon = row.findViewById(R.id.iv_rss_icon) as ImageView
                holder.tvRssTitle = row.findViewById(R.id.tv_rss_title) as TextView
                row.tag = holder
            } else {
                row = convertView
                holder = row.tag as ViewHolder
            }

            val feed = this.getItem(position) ?: return row
            holder.tvRssTitle.text = feed.title
            val iconPath = feed.iconPath
            if (iconPath == null || iconPath == Feed.DEDAULT_ICON_PATH) {
                holder.ivIcon.setImageResource(R.drawable.no_icon)
            } else {
                val file = File(iconPath)
                if (file.exists()) {
                    val bmp = BitmapFactory.decodeFile(file.path)
                    holder.ivIcon.setImageBitmap(bmp)
                }
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

            row.setOnClickListener {
                val newChecked = !holder.cbSelect.isChecked
                holder.cbSelect.isChecked = newChecked
                if (newChecked) {
                    checkFeed(position)
                } else {
                    uncheckFeed(position)
                }
            }
            return row
        }

        private fun uncheckFeed(position: Int) {
            val selected = getItem(position) ?: return
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
            val selected = getItem(position) ?: return
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

        private inner class ViewHolder {
            internal lateinit var cbSelect: CheckBox
            internal lateinit var ivIcon: ImageView
            internal lateinit var tvRssTitle: TextView
        }
    }
}
