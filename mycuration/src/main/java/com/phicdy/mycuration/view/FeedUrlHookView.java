package com.phicdy.mycuration.view;

public interface FeedUrlHookView {
    void showSuccessToast();
    void showInvalidUrlErrorToast();
    void showGenericErrorToast();
    void finishView();
}
