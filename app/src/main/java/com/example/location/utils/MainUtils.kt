package com.example.location.utils

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.example.location.PREF_KEY_SERVICE_NAME
import com.example.location.PREF_KEY_SERVICE_STATE
import com.example.location.data.ServiceState

fun isPermissionsGranted(context: Context) =
    ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

fun setServiceState(context: Context, state: ServiceState) {
    val sharedPrefs = getPreferences(context)
    sharedPrefs.edit().let {
        it.putString(PREF_KEY_SERVICE_STATE, state.name)
        it.apply()
    }
}

fun getServiceState(context: Context): ServiceState {
    val sharedPrefs = getPreferences(context)
    val value = sharedPrefs.getString(PREF_KEY_SERVICE_STATE, ServiceState.STOPPED.name)
    return ServiceState.valueOf(value.toString())
}

private fun getPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREF_KEY_SERVICE_NAME, 0)
}