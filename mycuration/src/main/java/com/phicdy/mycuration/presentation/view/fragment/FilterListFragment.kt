package com.phicdy.mycuration.presentation.view.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var filtersRecyclerView: RecyclerView
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbAdapter = DatabaseAdapter.getInstance()
        presenter = FilterListPresenter(this, dbAdapter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_filter_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            filtersRecyclerView = it.findViewById(R.id.rv_filter) as RecyclerView
            emptyView = it.findViewById(R.id.filter_emptyView) as TextView
            if (dbAdapter.numOfFeeds == 0) {
                emptyView.setText(R.string.no_rss_message)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    override fun showFilterList(filters: ArrayList<Filter>) {
        filtersRecyclerView.visibility = View.VISIBLE
        filtersListAdapter = FiltersListAdapter(filters)
        filtersRecyclerView.layoutManager = LinearLayoutManager(activity)
        filtersRecyclerView.adapter = filtersListAdapter
        filtersListAdapter.notifyDataSetChanged()
    }

    override fun hideFilterList() {
        filtersRecyclerView.visibility = View.GONE
    }

    override fun showEmptyView() {
        emptyView.visibility = View.VISIBLE
    }

    override fun hideEmptyView() {
        emptyView.visibility = View.GONE
    }

    override fun remove(position: Int) {
        filtersListAdapter.notifyItemRemoved(position)
    }

    override fun notifyListChanged() {
        filtersListAdapter.notifyDataSetChanged()
    }

    override fun startEditActivity(filterId: Int) {
        val intent = Intent(activity, RegisterFilterActivity::class.java)
        intent.putExtra(KEY_EDIT_FILTER_ID, filterId)
        startActivity(intent)
    }

    private inner class FiltersListAdapter(private val filters: ArrayList<Filter>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.filters_list, parent, false)
            return ViewHolder(itemView)
        }

        override fun getItemCount(): Int {
            return filters.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val filter = filters[position]

            if (holder is ViewHolder) {
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
                    holder.filterKeyword.text = getString(R.string.filter_keyword, keyword)
                }

                val url = filter.url
                if (url == "") {
                    holder.filterUrl.visibility = View.GONE
                } else {
                    holder.filterUrl.text = getString(R.string.url, url)
                }

                holder.filterEnabled.setOnCheckedChangeListener { _, isChecked ->
                    val clickedFilter = filters[position]
                    presenter.onFilterCheckClicked(clickedFilter, isChecked)
                }
                holder.filterEnabled.isChecked = filter.isEnabled

                holder.itemView.setOnCreateContextMenuListener { menu, _, _ ->
                    menu.add(0, EDIT_FILTER_MENU_ID, 0, R.string.edit_filter).setOnMenuItemClickListener {
                        presenter.onEditMenuClicked(filters[position])
                        true
                    }
                    menu.add(0, DELETE_FILTER_MENU_ID, 1, R.string.delete_filter).setOnMenuItemClickListener {
                        presenter.onDeleteMenuClicked(position, filters[position], filters.size)
                        filters.removeAt(position)
                        true
                    }
                }
            }
        }

        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal val filterTitle = itemView.findViewById(R.id.filterTitle) as TextView
            internal val feedTitle = itemView.findViewById(R.id.filterTargetFeed) as TextView
            internal val filterKeyword = itemView.findViewById(R.id.filterKeyword) as TextView
            internal val filterUrl = itemView.findViewById(R.id.filterUrl) as TextView
            internal val filterEnabled = itemView.findViewById(R.id.sw_filter_enable) as Switch
        }
    }
}
