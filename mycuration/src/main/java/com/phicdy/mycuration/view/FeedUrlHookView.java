package com.phicdy.mycuration.view;

public interface FeedUrlHookView {
    void registerFinishAddReceiver();
    void unregisterFinishAddReceiver();
    void showSuccessToast();
    void showInvalidUrlErrorToast();
    void showGenericErrorToast();
    void finishView();
}
