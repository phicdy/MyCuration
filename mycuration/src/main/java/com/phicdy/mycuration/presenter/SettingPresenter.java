package com.phicdy.mycuration.presenter;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.alarm.AlarmManagerTaskManager;
import com.phicdy.mycuration.util.PreferenceHelper;
import com.phicdy.mycuration.view.SettingView;

public class SettingPresenter implements Presenter {
    private final String[] updateIntervalHourItems;
    private final String[] updateIntervalStringItems;
    private final String[] allReadBehaviorItems;
    private final String[] allReadBehaviorStringItems;
    private final String[] swipeDirectionItems;
    private final String[] swipeDirectionStringItems;
    private SettingView view;
    private final PreferenceHelper helper;

    public SettingPresenter(@NonNull PreferenceHelper helper,
                            @NonNull String[] updateIntervalHourItems,
                            @NonNull String[] updateIntervalStringItems,
                            @NonNull String[] allReadBehaviorItems,
                            @NonNull String[] allReadBehaviorStringItems,
                            @NonNull String[] swipeDirectionItems,
                            @NonNull String[] swipeDirectionStringItems) {
        this.helper = helper;
        this.updateIntervalHourItems = updateIntervalHourItems;
        this.updateIntervalStringItems = updateIntervalStringItems;
        this.allReadBehaviorItems = allReadBehaviorItems;
        this.allReadBehaviorStringItems = allReadBehaviorStringItems;
        this.swipeDirectionItems = swipeDirectionItems;
        this.swipeDirectionStringItems = swipeDirectionStringItems;
    }

    public void setView(SettingView view) {
        this.view = view;
    }

    @Override
    public void create() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    public void activityCreate() {
        // Calc interval hour from saved interval second
        int autoUpdateIntervalSecond = helper.getAutoUpdateIntervalSecond();
        int autoUpdateIntervalHour = autoUpdateIntervalSecond / (60 * 60);
        // Set index of saved interval
        for (int i = 0; i < updateIntervalHourItems.length; i++) {
            if (Integer.valueOf(updateIntervalHourItems[i]) == autoUpdateIntervalHour) {
                view.setUpdateInterval(i, updateIntervalStringItems[i]);
                break;
            }
        }

        // Set default value of article sort option
        view.setArticleSort(helper.getSortNewArticleTop());

        // Set default value of internal browser option
        view.setInternalBrowser(helper.isOpenInternal());

        // Set index of behavior of all read
        for (int i = 0; i < allReadBehaviorItems.length; i++) {
            boolean allBehaviorItemBool = (Integer.valueOf(allReadBehaviorItems[i]) == 1);
            boolean savedValue = helper.getAllReadBack();
            if (allBehaviorItemBool == savedValue) {
                view.setAllReadBehavior(i, allReadBehaviorStringItems[i]);
                break;
            }
        }

        // Set index of swipe direction
        for (int i = 0; i < swipeDirectionItems.length; i++) {
            if (Integer.valueOf(swipeDirectionItems[i]) == helper.getSwipeDirection()) {
                view.setSwipeDirection(i, swipeDirectionStringItems[i]);
                break;
            }
        }
    }

    public void updateUpdateInterval(int intervalHour,
                                     @NonNull AlarmManagerTaskManager manager) {
        // Save new interval second
        int intervalSecond = intervalHour * 60 * 60;
        helper.setAutoUpdateIntervalSecond(intervalSecond);

        // Refresh summary
        for (int i = 0; i < updateIntervalHourItems.length; i++) {
            if (Integer.valueOf(updateIntervalHourItems[i]) == intervalHour) {
                view.setUpdateInterval(i, updateIntervalHourItems[i]);
                break;
            }
        }
        // Set new alarm
        manager.setNewAlarm(intervalSecond);
    }

    public void updateAllReadBehavior(boolean isAllReadBack) {
        // Save new behavior of all read
        helper.setAllReadBack(isAllReadBack);

        // Refresh summary
        for (int i = 0; i < allReadBehaviorItems.length; i++) {
            boolean allBehaviorItemBool = (Integer.valueOf(allReadBehaviorItems[i]) == 1);
            if (allBehaviorItemBool == isAllReadBack) {
                view.setAllReadBehavior(i, allReadBehaviorStringItems[i]);
                break;
            }
        }

    }

    public void updateSwipeDirection(int swipeDirection) {
        // Save new swipe direction
        helper.setSwipeDirection(swipeDirection);

        // Refresh summary
        for (int i = 0; i < swipeDirectionItems.length; i++) {
            if (Integer.valueOf(swipeDirectionItems[i]) == swipeDirection) {
                view.setSwipeDirection(i, swipeDirectionStringItems[i]);
                break;
            }
        }
    }

    public void updateArticleSort(boolean isNewArticleTop) {
        helper.setSortNewArticleTop(isNewArticleTop);
    }

    public void updateInternalBrowser(boolean isInternal) {
        helper.setOpenInternal(isInternal);
    }

    public void onLicenseClicked() {
        view.startLicenseActivity();
    }
}
