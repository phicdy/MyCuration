package com.phicdy.mycuration.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo


object NetworkUtil {

    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        return info != null && info.isConnected && info.typeName == "WIFI"
    }
}
