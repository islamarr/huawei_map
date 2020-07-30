package com.islam.huaweiapp.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.checkSelfPermission

class RequestPermission {
    companion object {

        var TAG = "RequestPermission"
        fun requestLocationPermission(context: Context?) {
            // check location permisiion
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                Log.i(TAG, "sdk < 28 Q")
                if (context?.let {
                        checkSelfPermission(
                            it,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    } != PackageManager.PERMISSION_GRANTED
                    && context?.let {
                        checkSelfPermission(
                            it,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    } != PackageManager.PERMISSION_GRANTED
                ) {
                    val strings = arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    ActivityCompat.requestPermissions(context as Activity, strings, 100)
                }
            } else {
                if (context?.let {
                        checkSelfPermission(
                            it,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    } != PackageManager.PERMISSION_GRANTED && context?.let {
                        checkSelfPermission(
                            it,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    } != PackageManager.PERMISSION_GRANTED && context?.let {
                        checkSelfPermission(
                            it,
                            "android.permission.ACCESS_BACKGROUND_LOCATION"
                        )
                    } != PackageManager.PERMISSION_GRANTED
                ) {
                    val strings = arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        "android.permission.ACCESS_BACKGROUND_LOCATION"
                    )
                    ActivityCompat.requestPermissions(context as Activity, strings, 200)
                }
            }
        }
    }
}