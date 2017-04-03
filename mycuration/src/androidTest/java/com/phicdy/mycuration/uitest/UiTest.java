package com.phicdy.mycuration.uitest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;

import com.squareup.spoon.Spoon;

import java.io.File;

abstract class UiTest {
    void takeScreenshot(UiDevice device) {
        Context context = InstrumentationRegistry.getTargetContext();
        final File file = new File(context.getExternalCacheDir(), System.currentTimeMillis() + ".png");
        device.takeScreenshot(file);
        Spoon.save(context, file);
    }

    void takeScreenshot(UiDevice device, @NonNull String fileName) {
        if (fileName.equals("")) {
            takeScreenshot(device);
            return;
        }
        Context context = InstrumentationRegistry.getTargetContext();
        final File file = new File(context.getExternalCacheDir(), fileName + ".png");
        device.takeScreenshot(file);
        Spoon.save(context, file);
    }
}
