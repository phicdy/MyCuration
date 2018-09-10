package com.phicdy.mycuration.presentation.view.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView

import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.filter.Filter
import com.phicdy.mycuration.presentation.presenter.FilterListPresenter
import com.phicdy.mycuration.presentation.view.FilterListView
import com.phicdy.mycuration.presentation.view.activity.RegisterFilterActivity

import java.util.ArrayList

class FilterListFragment : Fragment(), FilterListView {

    companion object {
        private const val EDIT_FILTER_MENU_ID = 2000
        private const val DELETE_FILTER_MENU_ID = 2001
        const val KEY_EDIT_FILTER_ID = "editFilterId"
    }

    private lateinit var presenter: FilterListPresenter
    private lateinit var dbAdapter: DatabaseAdapter
    private lateinit var filtersListAdapter: FiltersListAdapter
    private lateinit var filtersListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbAdapter = DatabaseAdapter.getInstance()
        presenter = FilterListPresenter(dbAdapter)
        presenter.setView(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_filter_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            filtersListView = it.findViewById(R.id.lv_filter) as ListView
            val emptyView = it.findViewById(R.id.filter_emptyView) as TextView
            if (dbAdapter.numOfFeeds == 0) {
                emptyView.setText(R.string.no_rss_message)
            }
            filtersListView.emptyView = emptyView
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(0, EDIT_FILTER_MENU_ID, 0, R.string.edit_filter)
        menu.add(0, DELETE_FILTER_MENU_ID, 1, R.string.delete_filter)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        if (item == null) return super.onContextItemSelected(item)
        val info = item.menuInfo as AdapterContextMenuInfo
        if (info.position > filtersListAdapter.count - 1) return false
        val selectedFilter = filtersListAdapter.getItem(info.position) ?: return false
        return when (item.itemId) {
            DELETE_FILTER_MENU_ID -> {
                //Delte selected filter from DB and ListView
                presenter.onDeleteMenuClicked(info.position, selectedFilter)
                true
            }
            EDIT_FILTER_MENU_ID -> {
                presenter.onEditMenuClicked(selectedFilter)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    override fun initList(filters: ArrayList<Filter>) {
        //Set ListView
        filtersListAdapter = FiltersListAdapter(filters)
        filtersListView.adapter = filtersListAdapter
        registerForContextMenu(filtersListView)
    }

    override fun remove(position: Int) {
        filtersListAdapter.remove(filtersListAdapter.getItem(position))
    }

    override fun notifyListChanged() {
        filtersListAdapter.notifyDataSetChanged()
    }

    override fun startEditActivity(filterId: Int) {
        val intent = Intent(activity, RegisterFilterActivity::class.java)
        intent.putExtra(KEY_EDIT_FILTER_ID, filterId)
        startActivity(intent)
    }

    /**
     *
     * @author phicdy
     * Display filters list
     */
    private inner class FiltersListAdapter internal constructor(filters: ArrayList<Filter>)/*
			 * @param cotext
			 * @param int : Resource ID
			 * @param T[] objects : data list
			 */ : ArrayAdapter<Filter>(activity, R.layout.filters_list, filters) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            activity?.let {
                lateinit var holder: ViewHolder

                //Use contentView
                lateinit var row: View
                if (convertView == null) {
                    val inflater = it.layoutInflater
                    row = inflater.inflate(R.layout.filters_list, parent, false)
                    holder = ViewHolder()
                    holder.filterTitle = row.findViewById(R.id.filterTitle) as TextView
                    holder.feedTitle = row.findViewById(R.id.filterTargetFeed) as TextView
                    holder.filterKeyword = row.findViewById(R.id.filterKeyword) as TextView
                    holder.filterUrl = row.findViewById(R.id.filterUrl) as TextView
                    holder.filterEnabled = row.findViewById(R.id.sw_filter_enable) as Switch
                    row.tag = holder
                } else {
                    row = convertView
                    holder = row.tag as ViewHolder
                }

                val filter = this.getItem(position)

                if (filter != null) {
                    //set filter title
                    holder.filterTitle.text = filter.title

                    if (filter.feeds.size <= 1) {
                        holder.feedTitle.text = filter.feedTitle
                    } else {
                        holder.feedTitle.text = getString(R.string.multiple_target_rss)
                    }

                    val keyword = filter.keyword
                    if (keyword == "") {
                        holder.filterKeyword.visibility = View.GONE
                    } else {
                        holder.filterKeyword.text = getString(R.string.keyword) + ": " + keyword
                    }

                    val url = filter.url
                    if (url == "") {
                        holder.filterUrl.visibility = View.GONE
                    } else {
                        holder.filterUrl.text = getString(R.string.url, url)
                    }

                    holder.filterEnabled.setOnCheckedChangeListener { _, isChecked ->
                        val clickedFilter = getItem(position)
                        if (clickedFilter != null) {
                            presenter.onFilterCheckClicked(clickedFilter, isChecked)
                        }
                    }
                    holder.filterEnabled.isChecked = filter.isEnabled
                }
                return row
            }
            return convertView!!
        }

        private inner class ViewHolder {
            internal lateinit var filterTitle: TextView
            internal lateinit var feedTitle: TextView
            internal lateinit var filterKeyword: TextView
            internal lateinit var filterUrl: TextView
            internal lateinit var filterEnabled: Switch
        }
    }
}
