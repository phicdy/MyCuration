package com.phicdy.mycuration.presentation.view;

import android.support.annotation.NonNull;

public interface FeedUrlHookView {
    void showSuccessToast();
    void showInvalidUrlErrorToast();
    void showGenericErrorToast();
    void finishView();
    void trackFailedUrl(@NonNull String url);
}
