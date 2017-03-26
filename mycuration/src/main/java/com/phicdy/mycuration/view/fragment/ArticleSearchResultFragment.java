package com.phicdy.mycuration.view.fragment;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.presenter.ArticleSearchResultPresenter;
import com.phicdy.mycuration.rss.Article;
import com.phicdy.mycuration.view.activity.InternalWebViewActivity;
import com.phicdy.mycuration.util.PreferenceHelper;
import com.phicdy.mycuration.view.ArticleSearchResultView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ArticleSearchResultFragment extends Fragment implements ArticleSearchResultView {

    private ArticleSearchResultPresenter presenter;
    private ListView resultListView;
    private ArrayList<Article> articles;

    private static final String OPEN_URL_ID = "openUrl";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceHelper prefMgr = PreferenceHelper.getInstance(getActivity());
        boolean isNewTop = prefMgr.getSortNewArticleTop();
        boolean isOpenInternal = prefMgr.isOpenInternal();
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(getActivity());
        presenter = new ArticleSearchResultPresenter(isNewTop, isOpenInternal, dbAdapter);
        presenter.setView(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_search_result, container, false);
        resultListView = (ListView) view.findViewById(android.R.id.list);
        resultListView.setEmptyView(view.findViewById(R.id.emptyView));
        setAllListener();
        return view;
    }

    public void handleIntent(@NonNull Intent intent) {
        String query = intent.getStringExtra(SearchManager.QUERY);
        presenter.handleIntent(intent.getAction(), query);

    }
    private void setAllListener() {
        // When an article selected, open this URL in default browser
        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (articles.size() == 0) return;
                String url = articles.get(position).getUrl();
                presenter.onListViewItemClicked(url);
            }
        });

        resultListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent,
                                           View view, int position, long id) {
                if (articles.size() == 0) return true;
                String url = articles.get(position).getUrl();
                presenter.onListViewItemLongClick(url);
                return true;
            }
        });
    }

    @Override
    public void refreshList(@NonNull ArrayList<Article> articles) {
        this.articles = articles;
        ArticlesListAdapter articlesListAdapter = new ArticlesListAdapter(articles);
        resultListView.setAdapter(articlesListAdapter);
    }

    @Override
    public void startInternalWebView(@NonNull String url) {
        Intent intent = new Intent(getActivity(),
                InternalWebViewActivity.class);
        intent.putExtra(OPEN_URL_ID, url);
        startActivity(intent);
    }

    @Override
    public void startExternalWebView(@NonNull String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void startShareUrl(@NonNull String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(intent);
    }

    /**
     *
     * @author phicdy Display articles list
     */
    class ArticlesListAdapter extends ArrayAdapter<Article> {
        ArticlesListAdapter(ArrayList<Article> articles) {
			/*
			 * @param cotext
			 *
			 * @param int : Resource ID
			 *
			 * @param T[] objects : data list
			 */
            super(getActivity(), R.layout.articles_list, articles);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;

            // Use contentView
            View row = convertView;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
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
                        "yyyy/MM/dd HH:mm:ss", Locale.US);
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
