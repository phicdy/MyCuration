package com.phicdy.mycuration.util

import android.content.Context
import android.widget.Toast

object ToastHelper {

    fun showToast(context: Context?, text: String?, length: Int) {
        if (context == null || text == null || text == "" ||
                length != Toast.LENGTH_SHORT && length != Toast.LENGTH_LONG) {
            return
        }
        Toast.makeText(context, text, length).show()
    }
}
