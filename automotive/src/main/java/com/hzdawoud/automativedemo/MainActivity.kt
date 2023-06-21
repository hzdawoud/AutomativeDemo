package com.hzdawoud.automativedemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.Manifest
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.hzdawoud.automativedemo.util.ActivityPermissionService
import com.hzdawoud.automativedemo.util.LocationService
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider


class MainActivity : AppCompatActivity() {

    private lateinit var locationService: LocationService

    private val permissionService = ActivityPermissionService(this) { isGranted ->
        Log.d(TAG, "location permissions isGranted: $isGranted")
        onMapReady()
        locationService = LocationService(this)
        observeAndUpdate()
    }

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private lateinit var mapView: MapView
    private lateinit var navigationLocationProvider: NavigationLocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.map_view)
        checkPermissions()

        println("Hzm Availability ${isGooglePlayServicesAvailable(this)}")
    }

    private fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }

    private fun onMapReady() {
        mapView.getMapboxMap().apply {
            setCamera(
                CameraOptions.Builder()
                    .zoom(14.0)
                    .build()
            )
            loadStyleUri(
                Style.MAPBOX_STREETS
            ) {
                initLocationComponent()
                setupGesturesListener()
            }
        }
    }

    private fun checkPermissions() {
        permissionService.checkForMultiplePermissions(
            locationPermissionsSet,
            "location permission is required to keep track your moves as a part of app functionality"
        )
    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        navigationLocationProvider = NavigationLocationProvider()
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.setLocationProvider(navigationLocationProvider)
        locationComponentPlugin.updateSettings {
            this.enabled = true
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
        locationComponentPlugin.addOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener
        )
    }

    override fun onStop() {
        super.onStop()
        if (this::locationService.isInitialized) {
            locationService.stopLocationUpdates()
        }
    }

    override fun onResume() {
        super.onResume()
        observeAndUpdate()
    }

    private fun observeAndUpdate() {
        if (this::locationService.isInitialized) {
            locationService.requestLocationUpdates() {
                if (this::navigationLocationProvider.isInitialized) {
                    Log.d("location", "${it.longitude} ${it.latitude}")
                    navigationLocationProvider.changePosition(
                        it
                    )
                }
            }
        }
    }

    private fun onCameraTrackingDismissed() {
        Toast.makeText(this, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    companion object {
        val TAG: String = MainActivity::class.java.simpleName

        private var locationPermissionsSet: Array<String> =
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
    }
}