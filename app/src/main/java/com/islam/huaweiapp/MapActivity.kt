package com.islam.huaweiapp

import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.huawei.hmf.tasks.Task
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.*
import com.islam.huaweiapp.utils.LocationHelper
import com.islam.huaweiapp.utils.RequestPermission
import com.islam.huaweiapp.viewModel.UpdateTitleViewModel
import kotlinx.android.synthetic.main.activity_map.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback, HuaweiMap.OnMapClickListener {
    private var mLatLng: LatLng? = null
    private var myLatLng: LatLng? = null
    private var hMap: HuaweiMap? = null
    private var mMarker: Marker? = null
    private lateinit var markerOptions: MarkerOptions
    private var mTitle: String? = "Searching...."
    private var myTitle: String? = "Searching...."
    private var lat: Double = 30.1
    private var long: Double = 31.1
    private var updateMyLocation: Boolean = false

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var settingsClient: SettingsClient
    private lateinit var mLocationRequest: LocationRequest
    private var mLocationCallback: LocationCallback? = null

    private lateinit var locationHelper: LocationHelper
    private lateinit var model: UpdateTitleViewModel

    companion object {
        private const val TAG = "MapActivity"
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        RequestPermission.requestLocationPermission(this)

        model = ViewModelProvider(this).get(UpdateTitleViewModel::class.java)
        locationHelper = LocationHelper(this, model)

        model.title.observe(this, Observer {

            mTitle = it

            progressBar.visibility = View.GONE

            if (updateMyLocation) {
                myTitle = mTitle
            }

            setMarkers()

        })

        // get mapView by layout view
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle =
                savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }

        mapView?.apply {
            onCreate(mapViewBundle)
            getMapAsync(this@MapActivity)
        }


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        settingsClient = LocationServices.getSettingsClient(this)
        mLocationRequest = LocationRequest().apply {
            interval = 1000
            needAddress = true
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallBack()

    }

    private fun locationCallBack() {
        if (null == mLocationCallback) {
            mLocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    if (locationResult != null) {
                        val locations: List<Location> =
                            locationResult.locations
                        if (locations.isNotEmpty()) {
                            for (location in locations) {

                                lat = location.latitude
                                long = location.longitude

                                mLatLng = LatLng(lat, long)
                                myLatLng = mLatLng

                                animateCamera()

                                updateMyLocation = true
                                locationHelper.setlocation(lat, long)

                                removeLocationUpdatesWithCallback()
                            }
                        }
                    }
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                    locationAvailability?.let {
                        val flag: Boolean = locationAvailability.isLocationAvailable
                        Log.i(TAG, "onLocationAvailability isLocationAvailable:$flag")
                    }
                }
            }
        }
    }

    private fun animateCamera() {
        val build =
            CameraPosition.Builder().target(mLatLng).zoom(12f).build()
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(build)
        hMap!!.animateCamera(cameraUpdate)
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
        removeLocationUpdatesWithCallback()
        mapView!!.onDestroy()
    }

    override fun onMapReady(map: HuaweiMap) {

        hMap = map

        hMap!!.setOnMapClickListener(this)

        requestLocationUpdatesWithCallback()

    }

    private fun setMarkers() {
        hMap!!.clear()

        hMap!!.addMarker(
            MarkerOptions().position(myLatLng)
                .title(myTitle)
                .infoWindowAnchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_star))
                .clusterable(true)
        )

        if (!updateMyLocation) {

            mMarker = hMap!!.addMarker(
                MarkerOptions().position(mLatLng)
                    .title(mTitle)
                    .infoWindowAnchor(0.5f, 0.5f)
                    .clusterable(true)
            )

            mMarker!!.showInfoWindow()

        } else {

            mLatLng = LatLng(lat + .02, long + .02)

            mMarker = hMap!!.addMarker(MarkerOptions().position(mLatLng))
        }

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

    private fun requestLocationUpdatesWithCallback() {
        try {
            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(mLocationRequest)
            val locationSettingsRequest = builder.build()
            // check devices settings before request location updates.
            //Before requesting location update, invoke checkLocationSettings to check device settings.
            val locationSettingsResponseTask: Task<LocationSettingsResponse> =
                settingsClient.checkLocationSettings(locationSettingsRequest)

            locationSettingsResponseTask.addOnSuccessListener { locationSettingsResponse: LocationSettingsResponse? ->
                Log.i(TAG, "check location settings success  {$locationSettingsResponse}")
                // request location updates
                fusedLocationProviderClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    Looper.getMainLooper()
                )
                    .addOnSuccessListener {
                        Log.i(TAG, "requestLocationUpdatesWithCallback onSuccess")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "requestLocationUpdatesWithCallback onFailure:${e.message}")
                    }
            }
                .addOnFailureListener { e: Exception ->
                    Log.e(TAG, "checkLocationSetting onFailure:${e.message}")
                    when ((e as ApiException).statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(
                                this, 0
                            )
                        } catch (sie: IntentSender.SendIntentException) {
                            Log.e(TAG, "PendingIntent unable to execute request.")
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "requestLocationUpdatesWithCallback exception:${e.message}")
        }
    }

    private fun removeLocationUpdatesWithCallback() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
                .addOnSuccessListener {
                    Log.i(
                        TAG,
                        "removeLocationUpdatesWithCallback onSuccess"
                    )
                }
                .addOnFailureListener { e ->
                    Log.e(
                        TAG,
                        "removeLocationUpdatesWithCallback onFailure:${e.message}"
                    )
                }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "removeLocationUpdatesWithCallback exception:${e.message}"
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: apply LOCATION PERMISSION successful")
                requestLocationUpdatesWithCallback()
            } else {
                Toast.makeText(this, getString(R.string.failed_locate), Toast.LENGTH_LONG).show()
            }
        }
        if (requestCode == 200) {
            if (grantResults.size > 2 && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(
                    TAG,
                    "onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION successful"
                )
                requestLocationUpdatesWithCallback()
            } else {
                Toast.makeText(this, getString(R.string.failed_locate), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onMapClick(latLng: LatLng) {
        progressBar.visibility = View.GONE

        mLatLng = latLng
        mTitle = getString(R.string.search)

        updateMyLocation = false
        locationHelper.setlocation(latLng.latitude, latLng.longitude)

        animateCamera()

        setMarkers()
    }
}