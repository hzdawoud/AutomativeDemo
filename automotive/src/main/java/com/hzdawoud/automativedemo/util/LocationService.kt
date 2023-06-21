package com.hzdawoud.automativedemo.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast

class LocationService(private val context: Context) {
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    private var lastLocation: Location? = null
    private val distanceThreshold = 2.0

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(callback: (Location) -> Unit) {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (isLocationSignificantlyChanged(location)) {
                    callback(location)
                    onLocationChangedHandler(location)
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String) {
            }

            override fun onProviderDisabled(provider: String) {
            }
        }

        locationManager?.requestLocationUpdates(
            LocationManager.PASSIVE_PROVIDER,
            0,
            0f,
            locationListener as LocationListener
        )
    }

    private fun isLocationSignificantlyChanged(location: Location): Boolean {
        lastLocation?.let {
            val distance = location.distanceTo(it)
            return distance > distanceThreshold
        } ?: kotlin.run {
            lastLocation = location
            return true
        }
    }

    private fun onLocationChangedHandler(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude

        Toast.makeText(context, "Latitude: $latitude, Longitude: $longitude", Toast.LENGTH_SHORT)
            .show()
        println("Hzm: Latitude: $latitude, Longitude: $longitude")

        lastLocation = location
    }

    fun stopLocationUpdates() {
        locationListener?.let { locationManager?.removeUpdates(it) }
    }
}





