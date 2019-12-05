package com.example.location

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.location.data.ServiceState
import com.example.location.data.enum.Actions
import com.example.location.data.model.LocationModel
import com.example.location.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


class LocationService : Service() {

    private var isForeground = false
    private var isServiceStarted = false
    private val localBinder = LocalBinder()
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentLocation: LocationModel? = null
    private var locationManager: NotificationManager? = null

    inner class LocalBinder : Binder() {
        fun getService(): LocationService {
            return this@LocationService
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        Timber.tag(LOG_TAG).d("Service onBind")
        stopForeground(true)
        isForeground = false
        return localBinder
    }

    override fun onRebind(intent: Intent?) {
        Timber.tag(LOG_TAG).d("Service onRebind")
        stopForeground(true)
        isForeground = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (isServiceStarted && !isForeground) {
            Timber.tag(LOG_TAG).d("Service unBind")
            startForeground(NOTIFICATION_ID, updateData())
            isForeground = true
        }
        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.tag(LOG_TAG).d("Service onStartCommand, startId: $startId")
        if (intent != null) when (intent.action) {
                Actions.START.name -> startService()
                Actions.STOP.name -> stopService()
                else ->
                    Timber.tag(LOG_TAG).d("No action!!!")
            } else {
            Timber.tag(LOG_TAG).d("Service onStartCommand with a null intent")
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        Timber.tag(LOG_TAG).d("Service onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        isForeground = false
        locationManager = null
        Timber.tag(LOG_TAG).d("Service onDestroy")
    }

    fun isForeground(): Boolean {
        return isForeground
    }

    private fun startService() {
        if (isServiceStarted) return
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)

        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocationService::lock").apply {
                    acquire(10 * 60 * 1000L /*10 minutes*/)
                }
            }

        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
                    Timber.tag(LOG_TAG).d("Start Update")
                    startLocationUpdate()
                }
                delay( 1000)
            }
        }
    }

    private fun startLocationUpdate() {
        LocationUtils().updateByFusedLocation(this@LocationService) {
            Timber.tag(LOG_TAG).d("New Location: $it")
            currentLocation = it
            sendUpdateBroadcast(it)
        }
    }

    private fun sendUpdateBroadcast(location: LocationModel) {
        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_LOCATION, location)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun stopService() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Timber.tag(LOG_TAG).d("Service stopped without starting: ${e.message}")
        }
        isForeground = false
        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
    }

    fun updateData(): Notification {
        val notification = buildNotification(this, currentLocation)
        updateNotification(locationManager, notification)
        return notification
    }
}
