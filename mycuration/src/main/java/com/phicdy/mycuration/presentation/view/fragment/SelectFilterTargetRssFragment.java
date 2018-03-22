package com.phicdy.mycuration.presentation.view.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.data.rss.Feed;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class SelectFilterTargetRssFragment extends ListFragment {

    private ArrayList<Feed> selectedList = new ArrayList<>();

    public SelectFilterTargetRssFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_filter_target_rss, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Context context = getActivity();
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance();
        TargetRssListAdapter adapter = new TargetRssListAdapter(dbAdapter.getAllFeedsWithoutNumOfUnreadArticles(), context);
        getListView().setAdapter(adapter);
    }

    public ArrayList<Feed> list() {
        return selectedList;
    }

    public void updateSelected(ArrayList<Feed> selectedList) {
        this.selectedList = selectedList;
    }

    private class TargetRssListAdapter extends ArrayAdapter<Feed> {
        TargetRssListAdapter(ArrayList<Feed> feeds, Context context) {
            super(context, R.layout.filter_target_rss_list, feeds);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;

            // Use contentView and setup ViewHolder
            View row = convertView;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.filter_target_rss_list, parent, false);
                holder = new ViewHolder();
                holder.cbSelect = (CheckBox) row.findViewById(R.id.cb_target);
                holder.ivIcon = (ImageView) row.findViewById(R.id.iv_rss_icon);
                holder.tvRssTitle = (TextView) row.findViewById(R.id.tv_rss_title);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            Feed feed = this.getItem(position);
            if (feed == null) return row;
            holder.tvRssTitle.setText(feed.getTitle());
            String iconPath = feed.getIconPath();
            if(iconPath == null || iconPath.equals(Feed.DEDAULT_ICON_PATH)) {
                holder.ivIcon.setImageResource(R.drawable.no_icon);
            }else {
                File file = new File(iconPath);
                if (file.exists()) {
                    Bitmap bmp = BitmapFactory.decodeFile(file.getPath());
                    holder.ivIcon.setImageBitmap(bmp);
                }
            }

            boolean isChecked = false;
            if (selectedList != null && selectedList.size() > 0) {
                for (Feed selectedFeed : selectedList) {
                    if (feed.getId() == selectedFeed.getId()) {
                        isChecked = true;
                        break;
                    }
                }
            }
            holder.cbSelect.setChecked(isChecked);
            holder.cbSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox)v;
                    if (checkBox.isChecked()) {
                        checkFeed(position);
                    }else {
                        uncheckFeed(position);
                    }
                }
            });

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean newChecked = !holder.cbSelect.isChecked();
                    holder.cbSelect.setChecked(newChecked);
                    if (newChecked) {
                        checkFeed(position);
                    }else {
                        uncheckFeed(position);
                    }
                }
            });
            return row;
        }

        private void uncheckFeed(int position) {
            Feed selected = getItem(position);
            if (selected == null) return;
            Iterator<Feed> iterator = selectedList.iterator();
            while (iterator.hasNext()) {
                Feed feed = iterator.next();
                if (selected.getId() == feed.getId()) {
                    iterator.remove();
                    break;
                }
            }
        }

        private void checkFeed(int position) {
            Feed selected = getItem(position);
            if (selected == null) return;
            Iterator<Feed> iterator = selectedList.iterator();
            boolean isExist = false;
            while (iterator.hasNext()) {
                Feed feed = iterator.next();
                if (selected.getId() == feed.getId()) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                selectedList.add(selected);
            }
        }

        private class ViewHolder {
            private CheckBox cbSelect;
            private ImageView ivIcon;
            private TextView tvRssTitle;
        }
    }
}
