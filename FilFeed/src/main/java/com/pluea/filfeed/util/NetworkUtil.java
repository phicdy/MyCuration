package com.pluea.filfeed.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NetworkUtil {

	public static boolean isWifiConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		return info.isConnected() && info.getTypeName().equals("WIFI");
	}
}
