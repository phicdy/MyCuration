package com.pluea.filfeed.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

import com.pluea.filfeed.R;
import com.pluea.filfeed.db.DatabaseAdapter;
import com.pluea.filfeed.rss.Article;
import com.pluea.filfeed.rss.Feed;
import com.pluea.filfeed.util.PreferenceManager;

public class ArticleSearchResultActivity extends ListActivity {

	private ArrayList<Article> articles;
	private DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(this);
	private PreferenceManager prefMgr;
	private Intent intent;
	private ListView resultListView;
	private ArticlesListAdapter articlesListAdapter;

	public static final String OPEN_URL_ID = "openUrl";
	private static final String LOG_TAG = "RSSReader.ArticlesList";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_search_result);

		// Set feed id and url from main activity
		intent = getIntent();

		prefMgr = PreferenceManager.getInstance(getApplicationContext());

		// title
		setTitle(getString(R.string.search_result));

		getActionBar().setDisplayHomeAsUpEnabled(true);

		resultListView = getListView();
		resultListView.setEmptyView(findViewById(R.id.emptyView));
		setAllListener();

		handleIntent(getIntent());
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
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		
		return super.onMenuItemSelected(featureId, item);
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
	 * @author kyamaguchi Display articles list
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

			// Use contentView
			View row = convertView;
			if (convertView == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.articles_list, parent, false);
			}

			Article article = this.getItem(position);

			if (article != null) {
				// set RSS Feed title
				TextView articleTitle = (TextView) row
						.findViewById(R.id.articleTitle);
				articleTitle.setText(article.getTitle());

				// set RSS posted date
				TextView articlePostedTime = (TextView) row
						.findViewById(R.id.articlePostedTime);
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy/MM/dd HH:mm:ss");
				String dateString = format.format(new Date(article
						.getPostedDate()));
				articlePostedTime.setText(dateString);

				// set RSS Feed unread article count
				TextView articlePoint = (TextView) row
						.findViewById(R.id.articlePoint);
				String hatenaPoint = article.getPoint();
				if (hatenaPoint.equals(Feed.DEDAULT_HATENA_POINT)) {
					articlePoint
							.setText(getString(R.string.not_get_hatena_point));
				} else {
					articlePoint.setText(hatenaPoint);
				}

				TextView feedTitleView = (TextView) row
						.findViewById(R.id.feedTitle);
				String feedTitle = article.getFeedTitle();
				if (feedTitle == null) {
					feedTitleView.setVisibility(View.GONE);
				} else {
					feedTitleView.setText(feedTitle);
				}

				articleTitle.setTextColor(Color.BLACK);
				articlePostedTime.setTextColor(Color.BLACK);
				articlePoint.setTextColor(Color.BLACK);
				feedTitleView.setTextColor(Color.BLACK);

				// If readStaus exists,change status
				// if(readStatus.containsKey(String.valueOf(position)) &&
				// readStatus.getInt(String.valueOf(position)) ==
				// article.getId()) {
				if (article.getStatus().equals(Article.TOREAD)
						|| article.getStatus().equals(Article.READ)) {
					articleTitle.setTextColor(Color.GRAY);
					articlePostedTime.setTextColor(Color.GRAY);
					articlePoint.setTextColor(Color.GRAY);
					feedTitleView.setTextColor(Color.GRAY);
				}
			}

			return (row);
		}

	}
}
