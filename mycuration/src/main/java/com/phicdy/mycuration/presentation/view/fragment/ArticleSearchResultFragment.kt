package com.phicdy.mycuration.presentation.view.fragment

import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView

import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.presentation.presenter.ArticleSearchResultPresenter
import com.phicdy.mycuration.data.rss.Article
import com.phicdy.mycuration.presentation.view.activity.InternalWebViewActivity
import com.phicdy.mycuration.util.PreferenceHelper
import com.phicdy.mycuration.presentation.view.ArticleSearchResultView

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class ArticleSearchResultFragment : Fragment(), ArticleSearchResultView {

    companion object {
        private const val OPEN_URL_ID = "openUrl"
    }

    private lateinit var presenter: ArticleSearchResultPresenter
    private lateinit var resultListView: ListView
    private lateinit var articles: ArrayList<Article>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefMgr = PreferenceHelper
        val isNewTop = prefMgr.sortNewArticleTop
        val isOpenInternal = prefMgr.isOpenInternal
        val dbAdapter = DatabaseAdapter.getInstance()
        presenter = ArticleSearchResultPresenter(isNewTop, isOpenInternal, dbAdapter)
        presenter.setView(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_article_search_result, container, false)
        resultListView = view.findViewById(android.R.id.list) as ListView
        resultListView.emptyView = view.findViewById(R.id.emptyView)
        setAllListener()
        return view
    }

    fun handleIntent(intent: Intent) {
        val query = intent.getStringExtra(SearchManager.QUERY)
        presenter.handleIntent(intent.action, query)

    }

    private fun setAllListener() {
        // When an article selected, open this URL in default browser
        resultListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (articles.size == 0) return@OnItemClickListener
            val url = articles[position].url
            presenter.onListViewItemClicked(url)
        }

        resultListView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            if (articles.size == 0) return@OnItemLongClickListener true
            val url = articles[position].url
            presenter.onListViewItemLongClick(url)
            true
        }
    }

    override fun refreshList(articles: ArrayList<Article>) {
        this.articles = articles
        val articlesListAdapter = ArticlesListAdapter(articles)
        resultListView.adapter = articlesListAdapter
    }

    override fun startInternalWebView(url: String) {
        val intent = Intent(activity,
                InternalWebViewActivity::class.java)
        intent.putExtra(OPEN_URL_ID, url)
        startActivity(intent)
    }

    override fun startExternalWebView(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    override fun startShareUrl(url: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, url)
        startActivity(intent)
    }

    /**
     *
     * @author phicdy Display articles list
     */
    internal inner class ArticlesListAdapter(articles: ArrayList<Article>)/*
			 * @param cotext
			 *
			 * @param int : Resource ID
			 *
			 * @param T[] objects : data list
			 */ : ArrayAdapter<Article>(activity, R.layout.articles_list, articles) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val holder: ViewHolder

            // Use contentView
            lateinit var row: View
            if (convertView == null) {
                val inflater = activity.layoutInflater
                row = inflater.inflate(R.layout.articles_list, parent, false)
                holder = ViewHolder()
                holder.articleTitle = row
                        .findViewById(R.id.articleTitle) as TextView
                holder.articlePostedTime = row
                        .findViewById(R.id.articlePostedTime) as TextView
                holder.articlePoint = row
                        .findViewById(R.id.articlePoint) as TextView
                holder.feedTitleView = row
                        .findViewById(R.id.feedTitle) as TextView
                row.tag = holder
            } else {
                row = convertView
                holder = row.tag as ViewHolder
            }

            val article = this.getItem(position)

            if (article != null) {
                // set RSS Feed title
                holder.articleTitle.text = article.title

                // set RSS posted date
                val format = SimpleDateFormat(
                        "yyyy/MM/dd HH:mm:ss", Locale.US)
                val dateString = format.format(Date(article
                        .postedDate))
                holder.articlePostedTime.text = dateString

                // set RSS Feed unread article count
                val hatenaPoint = article.point
                if (hatenaPoint == Article.DEDAULT_HATENA_POINT) {
                    holder.articlePoint.text = getString(R.string.not_get_hatena_point)
                } else {
                    holder.articlePoint.text = hatenaPoint
                }

                val feedTitle = article.feedTitle
                if (feedTitle == null) {
                    holder.feedTitleView.visibility = View.GONE
                } else {
                    holder.feedTitleView.text = feedTitle
                }

                holder.articleTitle.setTextColor(Color.BLACK)
                holder.articlePostedTime.setTextColor(Color.BLACK)
                holder.articlePoint.setTextColor(Color.BLACK)
                holder.feedTitleView.setTextColor(Color.BLACK)

                // If readStaus exists,change status
                // if(readStatus.containsKey(String.valueOf(position)) &&
                // readStatus.getInt(String.valueOf(position)) ==
                // article.getId()) {
                if (article.status == Article.TOREAD || article.status == Article.READ) {
                    holder.articleTitle.setTextColor(Color.GRAY)
                    holder.articlePostedTime.setTextColor(Color.GRAY)
                    holder.articlePoint.setTextColor(Color.GRAY)
                    holder.feedTitleView.setTextColor(Color.GRAY)
                }
            }

            return row
        }

        private inner class ViewHolder {
            internal lateinit var articleTitle: TextView
            internal lateinit var articlePostedTime: TextView
            internal lateinit var articlePoint: TextView
            internal lateinit var feedTitleView: TextView
        }
    }

}
