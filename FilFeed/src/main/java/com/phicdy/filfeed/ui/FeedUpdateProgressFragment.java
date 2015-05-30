package com.phicdy.filfeed.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.devspark.progressfragment.ProgressFragment;

import com.phicdy.filfeed.R;

public class FeedUpdateProgressFragment extends ProgressFragment {

    private View mContentView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FeedUpdateProgressFragment.
     */
    public static FeedUpdateProgressFragment newInstance() {
        FeedUpdateProgressFragment fragment = new FeedUpdateProgressFragment();
        return fragment;
    }

    public FeedUpdateProgressFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContentView = inflater.inflate(R.layout.fragment_feed_update_progress, null);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Setup content view
        setContentView(mContentView);
        // Setup text for empty content
//        setEmptyText(R.string.empty);
        setContentShown(false);
    }

}
