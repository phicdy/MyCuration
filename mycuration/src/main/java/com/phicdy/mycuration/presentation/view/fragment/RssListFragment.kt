package com.phicdy.mycuration.presentation.view.fragment

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.*
import android.widget.*
import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.domain.rss.UnreadCountManager
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.presentation.presenter.RssListPresenter
import com.phicdy.mycuration.presentation.view.RssListView
import com.phicdy.mycuration.util.PreferenceHelper
import java.io.File

class RssListFragment : Fragment(), RssListView {

    private lateinit var presenter: RssListPresenter
    private lateinit var tvAllUnreadArticleCount: TextView
    private lateinit var allUnread: LinearLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var feedsListView: ListView
    private lateinit var emptyView: TextView

    private lateinit var rssFeedListAdapter: RssFeedListAdapter
    private var mListener: OnFeedListFragmentListener? = null

    private lateinit var dbAdapter: DatabaseAdapter
    private var receiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbAdapter = DatabaseAdapter.getInstance()
        val networkTaskManager = NetworkTaskManager
        retainInstance = true
        val helper = PreferenceHelper
        presenter = RssListPresenter(helper, dbAdapter, networkTaskManager, UnreadCountManager)
        presenter.setView(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(FEEDS_KEY, presenter.feeds)
        outState.putParcelableArrayList(ALL_FEEDS_KEY, presenter.allFeeds)
    }

    override fun onResume() {
        super.onResume()
        setBroadCastReceiver()
        presenter.resume()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(0, DELETE_FEED_MENU_ID, 0, R.string.delete_rss)
        menu.add(0, EDIT_FEED_TITLE_MENU_ID, 1, R.string.edit_rss_title)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        if (item == null) return super.onContextItemSelected(item)
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo

        return when (item.itemId) {
            DELETE_FEED_MENU_ID -> {
                presenter.onDeleteFeedMenuClicked(info.position)
                true
            }
            EDIT_FEED_TITLE_MENU_ID -> {
                presenter.onEditFeedMenuClicked(info.position)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun showEditTitleDialog(position: Int, feedTitle: String) {
        val addView = View.inflate(activity, R.layout.edit_feed_title, null)
        val editTitleView = addView.findViewById(R.id.editFeedTitle) as EditText
        editTitleView.setText(feedTitle)

        AlertDialog.Builder(activity)
                .setTitle(R.string.edit_rss_title)
                .setView(addView)
                .setPositiveButton(R.string.save
                ) { _, _ ->
                    val newTitle = editTitleView.text.toString()
                    presenter.onEditFeedOkButtonClicked(newTitle, position)
                }.setNegativeButton(R.string.cancel, null).show()
    }

    override fun setRefreshing(doScroll: Boolean) {
        swipeRefreshLayout.isRefreshing = doScroll
    }

    override fun init(feeds: ArrayList<Feed>) {
        if (feeds.size == 0) emptyView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        activity?.let {
            rssFeedListAdapter = RssFeedListAdapter(feeds, it)
            feedsListView.adapter = rssFeedListAdapter
            rssFeedListAdapter.notifyDataSetChanged()
        }
    }

    override fun setTotalUnreadCount(count: Int) {
        tvAllUnreadArticleCount.text = count.toString()
    }

    override fun onRefreshCompleted() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun showEditFeedTitleEmptyErrorToast() {
        Toast.makeText(activity, getString(R.string.empty_title), Toast.LENGTH_SHORT).show()
    }

    override fun showEditFeedFailToast() {
        Toast.makeText(activity, getString(R.string.edit_rss_title_error), Toast.LENGTH_SHORT).show()
    }

    override fun showEditFeedSuccessToast() {
        Toast.makeText(activity, getString(R.string.edit_rss_title_success), Toast.LENGTH_SHORT).show()
    }

    override fun showDeleteSuccessToast() {
        Toast.makeText(activity, getString(R.string.finish_delete_rss_success), Toast.LENGTH_SHORT).show()
    }

    override fun showDeleteFailToast() {
        Toast.makeText(activity, getString(R.string.finish_delete_rss_fail), Toast.LENGTH_SHORT).show()
    }

    override fun showAddFeedSuccessToast() {
        Toast.makeText(activity, R.string.add_rss_success, Toast.LENGTH_SHORT).show()
    }

    override fun showGenericAddFeedErrorToast() {
        Toast.makeText(activity, R.string.add_rss_error_generic, Toast.LENGTH_SHORT).show()
    }

    override fun showInvalidUrlAddFeedErrorToast() {
        Toast.makeText(activity, R.string.add_rss_error_invalid_url, Toast.LENGTH_SHORT).show()
    }

    override fun notifyDataSetChanged() {
        rssFeedListAdapter.notifyDataSetChanged()
    }

    override fun showAllUnreadView() {
        allUnread.visibility = View.VISIBLE
    }

    override fun hideAllUnreadView() {
        allUnread.visibility = View.GONE
    }

    override fun showDeleteFeedAlertDialog(position: Int) {
        AlertDialog.Builder(activity)
                .setTitle(R.string.delete_rss_alert)
                .setPositiveButton(R.string.delete
                ) { _, _ -> presenter.onDeleteOkButtonClicked(position) }.setNegativeButton(R.string.cancel, null).show()
    }

    override fun onPause() {
        super.onPause()
        presenter.pause()
        if (receiver != null) {
            activity?.unregisterReceiver(receiver)
            receiver = null
        }
    }

    private fun setAllListener() {
        // When an feed selected, display unread articles in the feed
        feedsListView.onItemClickListener = AdapterView.OnItemClickListener {
            _, _, position, _ -> presenter.onFeedListClicked(position, mListener)
        }
        swipeRefreshLayout.setOnRefreshListener { presenter.onRefresh() }
        allUnread.setOnClickListener {
            if (mListener != null) {
                mListener!!.onAllUnreadClicked()
            }
        }
    }

    private fun setBroadCastReceiver() {
        // receive num of unread articles from Update Task
        receiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                // Set num of unread articles and update UI
                val action = intent.action
                if (action == NetworkTaskManager.FINISH_UPDATE_ACTION) {
                    Log.d(LOG_TAG, "onReceive")
                    presenter.onFinishUpdate()
                }
            }
        }

        val filter = IntentFilter()
        filter.addAction(NetworkTaskManager.FINISH_UPDATE_ACTION)
        activity?.registerReceiver(receiver, filter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rss_list, container, false)
        feedsListView = view.findViewById(R.id.feedList) as ListView
        emptyView = view.findViewById(R.id.emptyView) as TextView
        feedsListView.emptyView = emptyView
        swipeRefreshLayout = view.findViewById(R.id.srl_container) as SwipeRefreshLayout
        tvAllUnreadArticleCount = view.findViewById(R.id.allUnreadCount) as TextView
        allUnread = view.findViewById(R.id.ll_all_unread) as LinearLayout
        registerForContextMenu(feedsListView)
        setAllListener()
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            mListener = context as OnFeedListFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement OnFragmentInteractionListener")
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.activityCreated()
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnFeedListFragmentListener {
        fun onListClicked(feedId: Int)
        fun onAllUnreadClicked()
    }

    /**
     *
     * @author phicdy Display RSS Feeds List
     */
    private inner class RssFeedListAdapter internal constructor(feeds: ArrayList<Feed>, context: Context) : ArrayAdapter<Feed>(context, R.layout.feeds_list, feeds) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            activity?.let {
                val holder: ViewHolder

                // Use contentView and setup ViewHolder
                lateinit var row: View
                if (convertView == null) {
                    val inflater = it.layoutInflater
                    row = inflater.inflate(R.layout.feeds_list, parent, false)
                    holder = ViewHolder()
                    holder.feedIcon = row.findViewById(R.id.feedIcon) as ImageView
                    holder.feedTitle = row.findViewById(R.id.feedTitle) as TextView
                    holder.feedCount = row.findViewById(R.id.feedCount) as TextView
                    row.tag = holder
                } else {
                    row = convertView
                    holder = row.tag as ViewHolder
                }

                val feed = this.getItem(position)
                var iconPath: String? = null
                if (feed != null) {
                    iconPath = feed.iconPath
                }
                holder.feedIcon.visibility = View.VISIBLE
                holder.feedCount.visibility = View.VISIBLE
                if (presenter.isAllRssShowView(position + 1)) {
                    holder.feedIcon.visibility = View.INVISIBLE
                    holder.feedCount.visibility = View.GONE
                    holder.feedTitle.setText(R.string.show_all_rsses)
                } else if (presenter.isHideReadRssView(position + 1)) {
                    holder.feedIcon.visibility = View.INVISIBLE
                    holder.feedCount.visibility = View.GONE
                    holder.feedTitle.setText(R.string.hide_rsses)
                } else if (iconPath == null || iconPath == Feed.DEDAULT_ICON_PATH) {
                    holder.feedIcon.setImageResource(R.drawable.no_icon)
                    if (feed != null) {
                        holder.feedTitle.text = feed.title
                    }
                } else {
                    val file = File(iconPath)
                    if (file.exists()) {
                        val bmp = BitmapFactory.decodeFile(file.path)
                        holder.feedIcon.setImageBitmap(bmp)
                    } else {
                        dbAdapter.saveIconPath(feed.siteUrl, Feed.DEDAULT_ICON_PATH)
                    }
                    holder.feedTitle.text = feed.title
                }

                // set RSS Feed unread article count
                if (feed != null) {
                    holder.feedCount.text = UnreadCountManager.getUnreadCount(feed.id).toString()
                }

                return row
            }
            return convertView!!
        }

        private inner class ViewHolder {
            internal lateinit var feedIcon: ImageView
            internal lateinit var feedTitle: TextView
            internal lateinit var feedCount: TextView
        }
    }

    companion object {

        private const val DELETE_FEED_MENU_ID = 1000
        private const val EDIT_FEED_TITLE_MENU_ID = 1001
        private const val FEEDS_KEY = "feedsKey"
        private const val ALL_FEEDS_KEY = "allFeedsKey"
        private const val LOG_TAG = "FilFeed.FeedList"
    }

}
