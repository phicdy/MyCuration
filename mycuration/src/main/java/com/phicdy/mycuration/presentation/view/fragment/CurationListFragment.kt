package com.phicdy.mycuration.presentation.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.rss.Curation
import com.phicdy.mycuration.presentation.presenter.CurationListPresenter
import com.phicdy.mycuration.presentation.view.CurationItem
import com.phicdy.mycuration.presentation.view.CurationListView
import com.phicdy.mycuration.presentation.view.activity.AddCurationActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.ext.android.bindScope
import org.koin.android.scope.ext.android.getOrCreateScope
import org.koin.core.parameter.parametersOf
import java.util.ArrayList
import kotlin.coroutines.CoroutineContext


class CurationListFragment : Fragment(), CurationListView, CoroutineScope {

    companion object {
        private const val EDIT_CURATION_MENU_ID = 1
        private const val DELETE_CURATION_MENU_ID = 2
    }

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val presenter: CurationListPresenter by inject { parametersOf(this) }
    private lateinit var curationListAdapter: CurationListAdapter
    private var mListener: OnCurationListFragmentListener? = null
    private lateinit var curationListView: ListView
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindScope(getOrCreateScope("curation_list"))
        job = Job()
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.clear()
        menu.add(0, EDIT_CURATION_MENU_ID, 0, R.string.edit_curation)
        menu.add(0, DELETE_CURATION_MENU_ID, 0, R.string.delete_curation)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        if (item == null) return super.onContextItemSelected(item)
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo

        return when (item.itemId) {
            EDIT_CURATION_MENU_ID -> {
                val editCuration = curationListAdapter.getItem(info.position)
                if (editCuration != null) {
                    presenter.onCurationEditClicked(editCuration.id)
                }
                true
            }
            DELETE_CURATION_MENU_ID -> {
                curationListAdapter.getItem(info.position)?.let {
                    presenter.onCurationDeleteClicked(it)
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun setAllListener() {
        curationListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val curation = curationListAdapter.getItem(position)
            if (mListener != null && curation != null) {
                mListener!!.onCurationListClicked(curation.id)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_curation_list, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            mListener = context as OnCurationListFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement OnCurationListFragmentListener")
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        curationListView = activity?.findViewById(R.id.lv_curation) as ListView
        emptyView = activity?.findViewById(R.id.emptyView_curation) as TextView
        setAllListener()
        presenter.activityCreated()
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    override fun startEditCurationActivity(editCurationId: Int) {
        val intent = Intent()
        intent.setClass(activity, AddCurationActivity::class.java)
        intent.putExtra(AddCurationFragment.EDIT_CURATION_ID, editCurationId)
        startActivity(intent)
    }

    override fun setNoRssTextToEmptyView() {
        emptyView.setText(R.string.no_rss_message)
    }

    override fun setEmptyViewToList() {
        curationListView.emptyView = emptyView
    }

    override fun registerContextMenu() {
        registerForContextMenu(curationListView)
    }

    override fun initListBy(curations: ArrayList<Curation>) {
        activity?.let {
            curationListAdapter = CurationListAdapter(curations, it)
            curationListView.adapter = curationListAdapter
            curationListAdapter.notifyDataSetChanged()
        }
    }

    override fun delete(curation: Curation) {
        curationListAdapter.remove(curation)
        curationListAdapter.notifyDataSetChanged()
    }

    override fun size(): Int {
        return curationListAdapter.count
    }

    interface OnCurationListFragmentListener {
        fun onCurationListClicked(curationId: Int)
    }

    inner class CurationListAdapter(curations: ArrayList<Curation>, context: Context) : ArrayAdapter<Curation>(context, R.layout.curation_list, curations) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            activity?.let {
                // Use contentView and setup ViewHolder
                val holder: ViewHolder
                lateinit var row: View
                if (convertView == null) {
                    val inflater = it.layoutInflater
                    row = inflater.inflate(R.layout.curation_list, parent, false)
                    holder = ViewHolder()
                    holder.curationName = row.findViewById(R.id.tv_curation_title) as TextView
                    holder.curationCount = row.findViewById(R.id.tv_curation_count) as TextView
                    row.tag = holder
                } else {
                    row = convertView
                    holder = row.tag as ViewHolder
                }

                val curation = this.getItem(position)
                launch {
                    presenter.getView(curation, holder)
                }
                return row
            }
            return convertView!!
        }

        inner class ViewHolder: CurationItem {
            lateinit var curationName: TextView
            lateinit var curationCount: TextView

            override fun setName(name: String) {
                curationName.text = name
            }

            override fun setCount(count: String) {
                curationCount.text = count
            }
        }
    }
}
