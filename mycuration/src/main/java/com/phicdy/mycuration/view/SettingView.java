package com.phicdy.mycuration.view;

import android.support.annotation.NonNull;

public interface SettingView {
    void setUpdateInterval(int index, @NonNull String summary);
    void setAutoUpdateInMainUi(boolean isAutoUpdateInMainUi);
    void setArticleSort(boolean isNewArticleTop);
    void setInternalBrowser(boolean isEnabled);
    void setAllReadBehavior(int index, @NonNull String summary);
    void setSwipeDirection(int index, @NonNull String summary);
    void startLicenseActivity();
}
