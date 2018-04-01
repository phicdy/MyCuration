package com.phicdy.mycuration.presentation.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.phicdy.mycuration.R;
import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.presentation.presenter.ArticleListPresenter;
import com.phicdy.mycuration.presentation.view.activity.InternalWebViewActivity;
import com.phicdy.mycuration.presentation.view.activity.TopActivity;
import com.phicdy.mycuration.data.rss.Feed;
import com.phicdy.mycuration.domain.rss.UnreadCountManager;
import com.phicdy.mycuration.tracker.GATrackerHelper;
import com.phicdy.mycuration.util.PreferenceHelper;
import com.phicdy.mycuration.presentation.view.ArticleListView;
import com.phicdy.mycuration.presentation.view.ArticleRecyclerView;

import java.security.InvalidParameterException;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE;
import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static android.support.v7.widget.helper.ItemTouchHelper.RIGHT;

public class ArticlesListFragment extends Fragment implements ArticleListView {

    private ArticleListPresenter presenter;

    private ArticleRecyclerView recyclerView;
    private SimpleItemRecyclerViewAdapter articlesListAdapter;

    private FloatingActionButton fab;

    private OnArticlesListFragmentListener listener;
    private TextView emptyView;

    public interface OnArticlesListFragmentListener {
        void finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance();
        UnreadCountManager unreadManager = UnreadCountManager.getInstance();

        // Set feed id and url from main activity
        Intent intent = getActivity().getIntent();
        int feedId = intent.getIntExtra(TopActivity.FEED_ID, Feed.ALL_FEED_ID);
        int curationId = intent.getIntExtra(TopActivity.CURATION_ID,
                ArticleListPresenter.DEFAULT_CURATION_ID);
        intent.putExtra(TopActivity.FEED_ID, feedId);

        // Set swipe direction
        PreferenceHelper prefMgr = PreferenceHelper.INSTANCE;
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
        recyclerView = (ArticleRecyclerView) view.findViewById(R.id.rv_article);
        emptyView = (TextView) view.findViewById(R.id.emptyViewArticle);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        articlesListAdapter = new SimpleItemRecyclerViewAdapter();
        recyclerView.setAdapter(articlesListAdapter);
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
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ACTION_STATE_SWIPE, LEFT | RIGHT);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                presenter.onSwiped(direction, viewHolder.getAdapterPosition());
            }
        });
        helper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(helper);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastItemPosition = manager.findLastVisibleItemPosition();
                presenter.onScrolled(lastItemPosition);
            }
        });

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onFabButtonClicked();
                GATrackerHelper.INSTANCE.sendEvent(getString(R.string.scroll_article_list));
            }
        });
    }

    public void handleAllRead() {
        presenter.handleAllRead();
    }

    @Override
    public void openInternalWebView(@NonNull String url) {
        GATrackerHelper.INSTANCE.sendEvent(getString(R.string.tap_article_internal));
        Intent intent = new Intent(getActivity(), InternalWebViewActivity.class);
        intent.putExtra(InternalWebViewActivity.KEY_OPEN_URL, url);
        startActivity(intent);
    }

    @Override
    public void openExternalWebView(@NonNull String url) {
        GATrackerHelper.INSTANCE.sendEvent(getString(R.string.tap_article_external));
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void notifyListView() {
        articlesListAdapter.notifyDataSetChanged();
    }

    @Override
    public void finish() {
        listener.finish();
    }

    @Override
    public int getFirstVisiblePosition() {
        LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
        return manager.findFirstVisibleItemPosition();
    }

    @Override
    public int getLastVisiblePosition() {
        LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
        return manager.findLastCompletelyVisibleItemPosition();
    }

    @Override
    public void showShareUi(@NonNull String url) {
        if (isAdded()) GATrackerHelper.INSTANCE.sendEvent(getString(R.string.share_article));
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(intent);
    }

    @Override
    public void scrollTo(int position) {
        recyclerView.smoothScrollToPosition(position);
    }

    @Override
    public boolean isBottomVisible() {
        boolean isLastItemVisible = getLastVisiblePosition() == recyclerView.getAdapter().getItemCount()-1;
        int chilidCount = recyclerView.getChildCount();
        if (chilidCount < 1) return false;
        View lastItem = recyclerView.getChildAt(chilidCount-1);
        if (lastItem == null) return false;
        boolean isLastItemBottomVisible = lastItem.getBottom() == recyclerView.getHeight();
        return isLastItemVisible && isLastItemBottomVisible;
    }

    @Override
    public void showEmptyView() {
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int VIEW_TYPE_ARTICLE = 0;
        public static final int VIEW_TYPE_FOOTER = 1;
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder;
            switch (viewType) {
                case VIEW_TYPE_FOOTER:
                    View footer = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.footer_article_list_activity, parent, false);
                    holder = new FooterViewHolder(footer);
                    break;
                case VIEW_TYPE_ARTICLE:
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.articles_list, parent, false);
                    holder = new ArticleViewHolder(view);
                    break;
                default:
                    throw new InvalidParameterException("Invalid view type for article list");
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ArticleViewHolder) {
                ArticleViewHolder articleViewHolder = (ArticleViewHolder) holder;
                articleViewHolder.mView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        presenter.onListItemClicked(holder.getAdapterPosition());
                    }
                });
                articleViewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        presenter.onListItemLongClicked(holder.getAdapterPosition());
                        return true;
                    }
                });
                presenter.onBindViewHolder(articleViewHolder, position);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return presenter.onGetItemViewType(position);
        }

        @Override
        public int getItemCount() {
            return presenter.articleSize();
        }

        class FooterViewHolder extends RecyclerView.ViewHolder {
            FooterViewHolder(View itemView) {
                super(itemView);
            }
        }
        public class ArticleViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final TextView articleTitle;
            final TextView articlePostedTime;
            final TextView articlePoint;
            final TextView articleUrl;
            final TextView feedTitleView;
            final ImageView feedIconView;

            ArticleViewHolder(View view) {
                super(view);
                articleTitle = (TextView)view.findViewById(R.id.articleTitle);
                articlePostedTime = (TextView) view.findViewById(R.id.articlePostedTime);
                articlePoint = (TextView) view.findViewById(R.id.articlePoint);
                articleUrl = (TextView) view.findViewById(R.id.tv_articleUrl);
                feedTitleView = (TextView) view.findViewById(R.id.feedTitle);
                feedIconView = (ImageView) view.findViewById(R.id.iv_feed_icon);
                mView = view;
            }

            public void setArticleTitle(@NonNull String title) {
                articleTitle.setText(title);
            }

            public void setArticleUrl(@NonNull String url) {
                articleUrl.setText(url);
            }

            public void setArticlePostedTime(@NonNull String time) {
                articlePostedTime.setText(time);
            }

            public void setNotGetPoint() {
                articlePoint.setText(getString(R.string.not_get_hatena_point));
            }

            public void setArticlePoint(@NonNull String point) {
                articlePoint.setText(point);
            }

            public void hideRssInfo() {
                feedTitleView.setVisibility(View.GONE);
                feedIconView.setVisibility(View.GONE);
            }

            public void setRssTitle(@NonNull String title) {
                feedTitleView.setText(title);
                feedTitleView.setTextColor(Color.BLACK);
            }

            public void setRssIcon(@NonNull String path) {
                Bitmap bmp = BitmapFactory.decodeFile(path);
                feedIconView.setImageBitmap(bmp);
            }

            public void setDefaultRssIcon() {
                feedIconView.setImageResource(R.drawable.no_icon);
            }

            public void changeColorToRead() {
                articleTitle.setTextColor(Color.GRAY);
                articlePostedTime.setTextColor(Color.GRAY);
                articlePoint.setTextColor(Color.GRAY);
                feedTitleView.setTextColor(Color.GRAY);
            }

            public void changeColorToUnread() {
                articleTitle.setTextColor(Color.BLACK);
                articlePostedTime.setTextColor(Color.BLACK);
                articlePoint.setTextColor(Color.BLACK);
                feedTitleView.setTextColor(Color.BLACK);
            }

            @Override
            public String toString() {
                return super.toString();
            }
        }
    }
}