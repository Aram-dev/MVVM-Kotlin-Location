package com.example.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.location.data.model.LocationModel
import timber.log.Timber

class LocationReceiver(
    val function: (LocationModel) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        val location: LocationModel =
            intent.getSerializableExtra(EXTRA_LOCATION) as LocationModel
        Timber.tag(LOG_TAG).i("Receive New Location: $location")
        function(location)
    }
}