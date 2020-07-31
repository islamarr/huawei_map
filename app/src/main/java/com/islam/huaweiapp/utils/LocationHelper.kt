package com.islam.huaweiapp.utils

import android.app.Activity
import android.util.Log
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchService
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.Coordinate
import com.huawei.hms.site.api.model.NearbySearchRequest
import com.huawei.hms.site.api.model.NearbySearchResponse
import com.huawei.hms.site.api.model.SearchStatus
import com.islam.huaweiapp.R
import com.islam.huaweiapp.viewModel.UpdateTitleViewModel
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class LocationHelper(activity: Activity, var model: UpdateTitleViewModel) {
    private val TAG = "LocationHelper"
    private var searchService: SearchService? = null

    init {
        try {
            searchService = SearchServiceFactory.create(
                activity,
                URLEncoder.encode(
                    activity.getString(R.string.huawei_api_key),
                    "utf-8"
                )
            )
        } catch (e: UnsupportedEncodingException) {
            Log.e(TAG, "encode apikey error")
        }
    }

    fun setlocation(latitude: Double, longitude: Double) {
        val request = NearbySearchRequest()
        val location = Coordinate(latitude, longitude)
        request.location = location
        val resultListener: SearchResultListener<NearbySearchResponse?> =
            object : SearchResultListener<NearbySearchResponse?> {
                // Return search results upon a successful search.
                override fun onSearchResult(results: NearbySearchResponse?) {
                    if (results == null || results.totalCount <= 0) {
                        return
                    }
                    val sites = results.sites
                    if (sites == null || sites.size == 0) {
                        return
                    }
                    for (site in sites) {

                        val address = site.formatAddress

                        model.updateTitle(address)

                        break
                    }
                }

                // Return the result code and description upon a search exception.
                override fun onSearchError(status: SearchStatus) {
                    Log.i(
                        "TAG",
                        "Error : " + status.errorCode + " " + status.errorMessage
                    )
                }
            }

        // Call the nearby place search API.
        searchService!!.nearbySearch(request, resultListener)
    }


}