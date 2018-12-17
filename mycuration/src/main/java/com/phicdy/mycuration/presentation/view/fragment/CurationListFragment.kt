package com.phicdy.mycuration.presentation.view.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.rss.Curation
import com.phicdy.mycuration.presentation.presenter.CurationListPresenter
import com.phicdy.mycuration.presentation.view.CurationItem
import com.phicdy.mycuration.presentation.view.CurationListView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private lateinit var curationRecyclerView: RecyclerView
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
        curationRecyclerView = activity?.findViewById(R.id.rv_curation) as RecyclerView
        emptyView = activity?.findViewById(R.id.emptyView_curation) as TextView
        launch {
            presenter.activityCreated()
        }
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
        mListener?.startEditCurationActivity(editCurationId)
    }

    override fun setNoRssTextToEmptyView() {
        emptyView.setText(R.string.no_rss_message)
    }

    override fun registerContextMenu() {
        registerForContextMenu(curationRecyclerView)
    }

    override fun initListBy(curations: ArrayList<Curation>) {
        curationListAdapter = CurationListAdapter(curations)
        curationRecyclerView.adapter = curationListAdapter
        curationRecyclerView.layoutManager = LinearLayoutManager(activity)
        curationListAdapter.notifyDataSetChanged()
    }

    override fun showRecyclerView() {
        curationRecyclerView.visibility = View.VISIBLE
    }

    override fun hideRecyclerView() {
        curationRecyclerView.visibility = View.GONE
    }

    override fun showEmptyView() {
        emptyView.visibility = View.VISIBLE
    }

    override fun hideEmptyView() {
        emptyView.visibility = View.GONE
    }

    interface OnCurationListFragmentListener {
        fun onCurationListClicked(curationId: Int)
        fun startEditCurationActivity(editCurationId: Int)
    }

    inner class CurationListAdapter(private val curations: ArrayList<Curation>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.curation_list, parent, false)
            return ViewHolder(itemView)
        }

        override fun getItemCount(): Int {
            return curations.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder.itemView.setOnClickListener {
                val curation = curations[position]
                mListener?.onCurationListClicked(curation.id)
            }

            holder.itemView.setOnCreateContextMenuListener { menu, _, _ ->
                menu.add(0, EDIT_CURATION_MENU_ID, 0, R.string.edit_curation).setOnMenuItemClickListener {
                    val editCuration = curations[position]
                    presenter.onCurationEditClicked(editCuration.id)
                    true
                }
                menu.add(0, DELETE_CURATION_MENU_ID, 0, R.string.delete_curation).setOnMenuItemClickListener {
                    launch {
                        presenter.onCurationDeleteClicked(curations[position], curations.size)
                        curations.removeAt(position)
                        notifyItemRemoved(position)
                        delay(500) // Wait for delete animation
                        notifyDataSetChanged()
                    }
                    true
                }
            }
            val curation = curations[position]
            launch {
                presenter.getView(curation, holder as CurationItem)
            }

        }

        inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), CurationItem {
            val curationName = itemView.findViewById(R.id.tv_curation_title) as TextView
            val curationCount = itemView.findViewById(R.id.tv_curation_count) as TextView

            override fun setName(name: String) {
                curationName.text = name
            }

            override fun setCount(count: String) {
                curationCount.text = count
            }
        }
    }
}
