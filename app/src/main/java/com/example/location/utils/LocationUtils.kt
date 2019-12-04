package com.example.location.utils

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.os.Looper
import android.widget.Toast
import com.example.location.GPS_REQUEST
import com.example.location.GPS_UPDATE_TIME
import com.example.location.LOG_TAG
import com.example.location.SMALLEST_DISTANCE
import com.example.location.data.model.LocationModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import timber.log.Timber

class LocationUtils {

    private val locationSettingsRequest: LocationSettingsRequest
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var callback: LocationCallback

    init {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(LocationRequest.create().apply {
                interval = 2000
                fastestInterval = 1000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            })
        locationSettingsRequest = builder.build()
        builder.setAlwaysShow(true)
    }

    fun turnGPSOn(context: Context, gpsStatus: (Boolean) -> Unit) {
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            gpsStatus(true)
        } else {
            settingsClient
                .checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(context as Activity) {
                    gpsStatus(true)
                }
                .addOnFailureListener(context) { e ->
                    when ((e as ApiException).statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                            try {
                                val rae = e as ResolvableApiException
                                rae.startResolutionForResult(context, GPS_REQUEST)
                            } catch (sie: IntentSender.SendIntentException) {
                                Timber.tag(LOG_TAG).i("PendingIntent unable to execute request.")
                            }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            val errorMessage =
                                "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings."
                            Timber.tag(LOG_TAG).e(errorMessage)
                        }
                    }
                }
        }
    }

    fun updateByFusedLocation(
        appContext: Context,
        callbacks: (LocationModel) -> Unit
    ) {
        locationClient = LocationServices.getFusedLocationProviderClient(appContext)
        callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Timber.tag(LOG_TAG).d("LocationResult: $locationResult.")
                    if (location != null) {
                        callbacks(
                            LocationModel(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    }
                }
            }
        }

        locationClient.requestLocationUpdates(
            getLocationRequest(),
            callback,
            Looper.getMainLooper()
        )
    }

    private fun getLocationRequest(): LocationRequest {
        return LocationRequest().apply {
            interval = GPS_UPDATE_TIME
            smallestDisplacement = SMALLEST_DISTANCE
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
}