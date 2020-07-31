package com.islam.huaweiapp.utils

import android.content.Context
import android.net.ConnectivityManager

object Utils {

    fun isOnline(context: Context): Boolean {
        var connected = false
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.allNetworkInfo
        for (ni in netInfo) {
            if (ni.typeName
                    .equals("WIFI", ignoreCase = true)
            ) if (ni.isConnected) connected = true
            if (ni.typeName
                    .equals("MOBILE", ignoreCase = true)
            ) if (ni.isConnected) connected = true
        }
        return connected
    }

}