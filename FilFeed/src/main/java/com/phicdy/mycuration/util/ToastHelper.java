package com.phicdy.mycuration.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by phicdy on 2015/08/25.
 */
public class ToastHelper {

    public static void showToast(Context context, String text, int length) {
        if (context == null || text == null || text.equals("") ||
                (length != Toast.LENGTH_SHORT && length != Toast.LENGTH_LONG)) {
            return;
        }
        Toast.makeText(context, text, length).show();
    }
}
