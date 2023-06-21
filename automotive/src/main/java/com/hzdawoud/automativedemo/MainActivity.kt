package com.hzdawoud.automativedemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var locationService: LocationService

    private val permissionService = ActivityPermissionService(this) { isGranted ->
        Log.d(TAG, "location permissions isGranted: $isGranted")
        onMapReady {
            locationService = LocationService(this)
            observeAndUpdate()
        }
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
    private lateinit var previousLocation: TextView
    private lateinit var currentLocation: TextView
    private lateinit var navigationLocationProvider: NavigationLocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.map_view)
        previousLocation = findViewById(R.id.previousLocation)
        currentLocation = findViewById(R.id.currentLocation)
        checkPermissions()

        println("Hzm Availability ${isGooglePlayServicesAvailable(this)}")
    }

    private fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }

    private fun onMapReady(onCompleteCallback: () -> Unit) {
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
                onCompleteCallback.invoke()
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

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun observeAndUpdate() {
        val df: DateFormat = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss", Locale.getDefault())

        if (this::locationService.isInitialized) {
            locationService.requestLocationUpdates() {
                if (this::navigationLocationProvider.isInitialized) {
                    Log.d("location", "${it.second.longitude} ${it.second.latitude}")
                    navigationLocationProvider.changePosition(
                        it.second
                    )
                    it.first?.let { pLocation ->
                        previousLocation.text =
                            "Previous Location: Lat:${pLocation.latitude}, Lng:${pLocation.longitude}  ${
                                df.format(
                                    Calendar.getInstance().time
                                )
                            }"
                    }
                    it.second.let { cLocation ->
                        currentLocation.text =
                            "Current Location: Lat:${cLocation.latitude}, Lng:${cLocation.longitude}  ${
                                df.format(
                                    Calendar.getInstance().time
                                )
                            }"
                    }
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