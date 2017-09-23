package com.phicdy.mycuration.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.phicdy.mycuration.R;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.presenter.ArticleListPresenter;
import com.phicdy.mycuration.rss.Article;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.phicdy.mycuration.tracker.GATrackerHelper;
import com.phicdy.mycuration.util.PreferenceHelper;
import com.phicdy.mycuration.util.TextUtil;
import com.phicdy.mycuration.view.ArticleListView;
import com.phicdy.mycuration.view.activity.InternalWebViewActivity;
import com.phicdy.mycuration.view.activity.TopActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ArticlesListFragment extends Fragment implements ArticleListView {

    private ArticleListPresenter presenter;

    private ListView listView;
    private ArticlesListAdapter articlesListAdapter;
    private static final String OPEN_URL_ID = "openUrl";

    private View footer;
    private FloatingActionButton fab;

    private OnArticlesListFragmentListener listener;
    public interface OnArticlesListFragmentListener {
        boolean onListViewTouchEvent(MotionEvent event);
        void finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(getActivity());
        UnreadCountManager unreadManager = UnreadCountManager.getInstance(getActivity());

        // Set feed id and url from main activity
        Intent intent = getActivity().getIntent();
        int feedId = intent.getIntExtra(TopActivity.FEED_ID, Feed.ALL_FEED_ID);
        int curationId = intent.getIntExtra(TopActivity.CURATION_ID,
                ArticleListPresenter.DEFAULT_CURATION_ID);
        intent.putExtra(TopActivity.FEED_ID, feedId);

        // Set swipe direction
        PreferenceHelper prefMgr = PreferenceHelper.getInstance(getActivity());
        prefMgr.setSearchFeedId(feedId);
        int swipeDirectionOption = prefMgr.getSwipeDirection();
        boolean isOpenInternal = prefMgr.isOpenInternal();
        boolean isAllReadBack = prefMgr.getAllReadBack();
        boolean isNewestArticleTop = prefMgr.getSortNewArticleTop();
        presenter = new ArticleListPresenter(feedId, curationId, dbAdapter, unreadManager,
                isOpenInternal, isAllReadBack, isNewestArticleTop, swipeDirectionOption);
        presenter.setView(this);
        presenter.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnArticlesListFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement Article list listener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_articles_list, container, false);
        listView = (ListView) view.findViewById(R.id.lv_article);
        articlesListAdapter = new ArticlesListAdapter(new ArrayList<Article>());
        listView.setAdapter(articlesListAdapter);
        listView.setEmptyView(view.findViewById(R.id.emptyView));
        footer = inflater.inflate(R.layout.footer_article_list_activity, container, false);
        listView.addFooterView(footer);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        setAllListener();
        presenter.createView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    private void setAllListener() {
        // When an article selected, open this URL in default browser
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Article clickedArticle = articlesListAdapter.getItem(position);
                presenter.onListItemClicked(clickedArticle);
            }
        });

        listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                Article clickedArticle = articlesListAdapter.getItem(position);
                if (clickedArticle != null) {
                    presenter.onListItemLongClicked(clickedArticle);
                }
                return true;
            }
        });

        listView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return listener.onListViewTouchEvent(event);
            }
        });

        listView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return listener.onListViewTouchEvent(event);
            }
        });

        listView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount == visibleItemCount) {
                    // All of the items are visible
                    return;
                }
                if (totalItemCount == firstVisibleItem + visibleItemCount) {
                    // Reach end of the list
                    presenter.loadArticles();
                }
            }
        });

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onFabButtonClicked();
                GATrackerHelper.sendEvent(getString(R.string.scroll_article_list));
            }
        });
    }

    public void onFlying(MotionEvent event1, MotionEvent event2, float velocityX) {
        // Set touched position in articles list from touch event
        int touchedPosition = listView.pointToPosition(
                (int) event1.getX(), (int) event1.getY());
        if (touchedPosition < 0 || touchedPosition > articlesListAdapter.getCount()) return;
        presenter.onFlying(touchedPosition, event1, event2, velocityX);
    }

    public void handleAllRead() {
        presenter.handleAllRead();
    }

    @Override
    public void invalidateView() {
        listView.invalidateViews();
    }

    @Override
    public void showFooter() {
        // It causes the crash of ClassCastException
        // if ListView#addFooterView() is called before ListView#setAdapter()
        if (listView.getAdapter() != null) {
            listView.addFooterView(footer);
        }
    }

    @Override
    public void removeFooter() {
        listView.removeFooterView(footer);
    }

    @Override
    public void addArticle(Article article) {
        articlesListAdapter.add(article);
    }

    @Override
    public void openInternalWebView(@NonNull String url) {
        GATrackerHelper.sendEvent(getString(R.string.tap_article_internal));
        Intent intent = new Intent(getActivity(), InternalWebViewActivity.class);
        intent.putExtra(OPEN_URL_ID, url);
        startActivity(intent);
    }

    @Override
    public void openExternalWebView(@NonNull String url) {
        GATrackerHelper.sendEvent(getString(R.string.tap_article_external));
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void notifyListView() {
        articlesListAdapter.notifyDataSetChanged();
    }

    @Override
    public int size() {
        return articlesListAdapter.getCount();
    }

    @Override
    public void finish() {
        listener.finish();
    }

    @Override
    public Article getItem(int position) {
        return articlesListAdapter.getItem(position);
    }

    @Override
    public int getFirstVisiblePosition() {
        return listView.getFirstVisiblePosition();
    }

    @Override
    public int getLastVisiblePosition() {
        return listView.getLastVisiblePosition();
    }

    @Override
    public void showShareUi(@NonNull String url) {
        if (isAdded()) GATrackerHelper.sendEvent(getString(R.string.share_article));
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(intent);
    }

    @Override
    public void scroll(int positionToScroll, int pixelFromTopAfterScroll) {
        if (positionToScroll < 0 || pixelFromTopAfterScroll < 0) return;
        listView.smoothScrollToPositionFromTop(positionToScroll, pixelFromTopAfterScroll);
    }

    @Override
    public boolean isBottomVisible() {
        boolean isLastItemVisible = listView.getLastVisiblePosition() == listView.getAdapter().getCount()-1;
        int chilidCount = listView.getChildCount();
        if (chilidCount < 1) return false;
        View lastItem = listView.getChildAt(chilidCount-1);
        if (lastItem == null) return false;
        boolean isLastItemBottomVisible = lastItem.getBottom() == listView.getHeight();
        return isLastItemVisible && isLastItemBottomVisible;
    }

    /**
     *
     * @author phicdy Display articles list
     */
    private class ArticlesListAdapter extends ArrayAdapter<Article> {

        private ViewHolder holder;

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
            // Use contentView
            View row = convertView;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.articles_list, parent, false);
                holder = new ViewHolder();
                holder.articleTitle = (TextView)row.findViewById(R.id.articleTitle);
                holder.articlePostedTime = (TextView) row.findViewById(R.id.articlePostedTime);
                holder.articlePoint = (TextView) row.findViewById(R.id.articlePoint);
                holder.articleUrl = (TextView) row.findViewById(R.id.tv_articleUrl);
                holder.feedTitleView = (TextView) row.findViewById(R.id.feedTitle);
                holder.feedIconView = (ImageView) row.findViewById(R.id.iv_feed_icon);
                row.setTag(holder);
            }else {
                holder = (ViewHolder)row.getTag();
            }
            
            Article article = this.getItem(position);
            if (article != null) {
                holder.articleTitle.setText(article.getTitle());
                holder.articleUrl.setText(article.getUrl());

                // Set article posted date
                SimpleDateFormat format = new SimpleDateFormat(
                        "yyyy/MM/dd HH:mm:ss", Locale.US);
                String dateString = format.format(new Date(article
                        .getPostedDate()));
                holder.articlePostedTime.setText(dateString);

                // Set RSS Feed unread article count
                String hatenaPoint = article.getPoint();
                if(hatenaPoint.equals(Article.DEDAULT_HATENA_POINT)) {
                    holder.articlePoint.setText(getString(R.string.not_get_hatena_point));
                }else {
                    holder.articlePoint.setText(hatenaPoint);
                }

                String feedTitle = article.getFeedTitle();
                if(feedTitle == null) {
                    holder.feedTitleView.setVisibility(View.GONE);
                    holder.feedIconView.setVisibility(View.GONE);
                }else {
                    holder.feedTitleView.setText(feedTitle);
                    holder.feedTitleView.setTextColor(Color.BLACK);

                    String iconPath = article.getFeedIconPath();
                    if (!TextUtil.isEmpty(iconPath) && new File(iconPath).exists()) {
                        Bitmap bmp = BitmapFactory.decodeFile(article.getFeedIconPath());
                        holder.feedIconView.setImageBitmap(bmp);
                    }else {
                        holder.feedIconView.setImageResource(R.drawable.no_icon);
                    }
                }
                holder.articleTitle.setTextColor(Color.BLACK);
                holder.articlePostedTime.setTextColor(Color.BLACK);
                holder.articlePoint.setTextColor(Color.BLACK);

                // Change color if already be read
                if (article.getStatus().equals(Article.TOREAD) || article.getStatus().equals(Article.READ)) {
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
            TextView articleUrl;
            TextView feedTitleView;
            ImageView feedIconView;
        }
    }
}