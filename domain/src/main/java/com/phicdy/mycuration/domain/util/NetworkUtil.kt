package com.phicdy.mycuration.domain.util

import android.content.Context
import android.net.ConnectivityManager


object NetworkUtil {

    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        return info != null && info.isConnected && info.typeName == "WIFI"
    }
}
