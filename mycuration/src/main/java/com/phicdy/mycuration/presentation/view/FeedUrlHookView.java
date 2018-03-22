package com.phicdy.mycuration.presentation.view;

public interface FeedUrlHookView {
    void showSuccessToast();
    void showInvalidUrlErrorToast();
    void showGenericErrorToast();
    void finishView();
}
