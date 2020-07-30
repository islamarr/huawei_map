package com.islam.huaweiapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.*
import kotlinx.android.synthetic.main.activity_map.*


class Map : AppCompatActivity(), OnMapReadyCallback {
    private var mLatLng: LatLng? = null
    private var hMap: HuaweiMap? = null
    private var mMarker: Marker? = null
    var mTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        if (!hasPermissions(
                this,
                *RUNTIME_PERMISSIONS
            )
        ) {
            ActivityCompat.requestPermissions(
                this,
                RUNTIME_PERMISSIONS,
                REQUEST_CODE
            )
        }

        // get mapView by layout view
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle =
                savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }

        mapView?.apply {
            onCreate(mapViewBundle)
            getMapAsync(this@Map)
        }

        val lat = 30.0
        val ln = 31.1
        mTitle = "Address"
        mLatLng = LatLng(lat, ln)
    }

    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onMapReady(map: HuaweiMap) {

        hMap = map
        hMap!!.isMyLocationEnabled = true
        hMap?.uiSettings?.isMyLocationButtonEnabled = true

        // Move camera by CameraPosition param ,latlag and zoom params can set here
        val build = CameraPosition.Builder().target(mLatLng).zoom(18f).build()
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(build)
        hMap!!.animateCamera(cameraUpdate)

        addMareker()

        hMap!!.setOnMapClickListener { latLng ->

            // Clear all Markers
            hMap?.clear()

            mLatLng = latLng
            mTitle = "NewAddress"
            addMareker()

            mMarker!!.showInfoWindow()


        }

    }

    private fun addMareker() {
        mMarker = hMap!!.addMarker(
            MarkerOptions().position(mLatLng)
                .title(mTitle)
                .infoWindowAnchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_star))
                .clusterable(true)
        )
    }

    override fun onPause() {
        mapView!!.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    companion object {
        private const val TAG = "Map"
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        private const val REQUEST_CODE = 100
        private val RUNTIME_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET
        )

        private fun hasPermissions(
            context: Context,
            vararg permissions: String
        ): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            permission
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return false
                    }
                }
            }
            return true
        }
    }
}