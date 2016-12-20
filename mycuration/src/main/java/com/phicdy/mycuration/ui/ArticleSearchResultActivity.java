package com.phicdy.mycuration.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Article;
import com.phicdy.mycuration.tracker.GATrackerHelper;
import com.phicdy.mycuration.util.PreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ArticleSearchResultActivity extends ActionBarActivity {

	private ArrayList<Article> articles;
	private DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(this);
	private PreferenceHelper prefMgr;
	private Intent intent;
	private ListView resultListView;
	private ArticlesListAdapter articlesListAdapter;

	public static final String OPEN_URL_ID = "openUrl";
	private static final String LOG_TAG = "FilFeed.SearchResult";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_search_result);

		// Set feed id and url from main activity
		intent = getIntent();

		prefMgr = PreferenceHelper.getInstance(getApplicationContext());

		// title
		setTitle(getString(R.string.search_result));

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		resultListView = (ListView)findViewById(android.R.id.list);
		resultListView.setEmptyView(findViewById(R.id.emptyView));
		setAllListener();

		handleIntent(getIntent());
	}

	@Override
	protected void onResume() {
		super.onResume();
		GATrackerHelper.sendScreen(getString(R.string.search_result));
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			boolean isNewestArticleTop = prefMgr.getSortNewArticleTop();

			String query = intent.getStringExtra(SearchManager.QUERY);
			articles = dbAdapter.searchArticles(query, isNewestArticleTop);
			articlesListAdapter = new ArticlesListAdapter(articles);
			resultListView.setAdapter(articlesListAdapter);
		}
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		
		return true;
	}

	private void setAllListener() {
		// When an article selected, open this URL in default browser
		resultListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						if (prefMgr.isOpenInternal()) {
							intent = new Intent(getApplicationContext(),
									InternalWebViewActivity.class);
							intent.putExtra(OPEN_URL_ID,
									articles.get(position).getUrl());
						} else {
							Uri uri = Uri.parse(articles.get(position)
									.getUrl());
							intent = new Intent(Intent.ACTION_VIEW, uri);
						}
						startActivity(intent);
					}

				});

		resultListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						Log.d(LOG_TAG, "onLongClick");
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType("text/plain");
						intent.putExtra(Intent.EXTRA_TEXT,
								articles.get(position).getUrl());
						startActivity(intent);
						return true;
					}

				});
	}

	// If Back button pushed
	@Override
	protected void onDestroy() {
		// onSaveInstanceState(readStatus);
		super.onDestroy();
	}

	/**
	 * 
	 * @author phicdy Display articles list
	 */
	class ArticlesListAdapter extends ArrayAdapter<Article> {
		public ArticlesListAdapter(ArrayList<Article> articles) {
			/*
			 * @param cotext
			 * 
			 * @param int : Resource ID
			 * 
			 * @param T[] objects : data list
			 */
			super(ArticleSearchResultActivity.this, R.layout.articles_list,
					articles);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			
			// Use contentView
			View row = convertView;
			if (convertView == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.articles_list, parent, false);
				holder = new ViewHolder();
				holder.articleTitle = (TextView) row
						.findViewById(R.id.articleTitle);
				holder.articlePostedTime = (TextView) row
						.findViewById(R.id.articlePostedTime);
				holder.articlePoint = (TextView) row
						.findViewById(R.id.articlePoint);
				holder.feedTitleView = (TextView) row
						.findViewById(R.id.feedTitle);
				row.setTag(holder);
			}else {
				holder = (ViewHolder)row.getTag();
			}

			Article article = this.getItem(position);

			if (article != null) {
				// set RSS Feed title
				holder.articleTitle.setText(article.getTitle());

				// set RSS posted date
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy/MM/dd HH:mm:ss");
				String dateString = format.format(new Date(article
						.getPostedDate()));
				holder.articlePostedTime.setText(dateString);

				// set RSS Feed unread article count
				String hatenaPoint = article.getPoint();
				if (hatenaPoint.equals(Article.DEDAULT_HATENA_POINT)) {
					holder.articlePoint
							.setText(getString(R.string.not_get_hatena_point));
				} else {
					holder.articlePoint.setText(hatenaPoint);
				}

				String feedTitle = article.getFeedTitle();
				if (feedTitle == null) {
					holder.feedTitleView.setVisibility(View.GONE);
				} else {
					holder.feedTitleView.setText(feedTitle);
				}

				holder.articleTitle.setTextColor(Color.BLACK);
				holder.articlePostedTime.setTextColor(Color.BLACK);
				holder.articlePoint.setTextColor(Color.BLACK);
				holder.feedTitleView.setTextColor(Color.BLACK);

				// If readStaus exists,change status
				// if(readStatus.containsKey(String.valueOf(position)) &&
				// readStatus.getInt(String.valueOf(position)) ==
				// article.getId()) {
				if (article.getStatus().equals(Article.TOREAD)
						|| article.getStatus().equals(Article.READ)) {
					holder.articleTitle.setTextColor(Color.GRAY);
					holder.articlePostedTime.setTextColor(Color.GRAY);
					holder.articlePoint.setTextColor(Color.GRAY);
					holder.feedTitleView.setTextColor(Color.GRAY);
				}
			}

			return row;
		}

		private class ViewHolder {
			TextView articleTitle;
			TextView articlePostedTime;
			TextView articlePoint;
			TextView feedTitleView;
		}
	}
}
