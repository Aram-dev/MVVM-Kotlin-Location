package com.example.location

import android.Manifest
import android.app.Activity
import android.content.*
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.location.data.ServiceState
import com.example.location.data.enum.Actions
import com.example.location.data.model.LocationModel
import com.example.location.databinding.MainActivityBinding
import com.example.location.ui.MainViewModel
import com.example.location.utils.*
import kotlinx.android.synthetic.main.main_activity.*
import timber.log.Timber
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var mainViewModel: MainViewModel

    private var isBound: Boolean = false
    private lateinit var binding: MainActivityBinding
    private lateinit var receiver: LocationReceiver
    private lateinit var service: LocationService

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as LocationService.LocalBinder
            this@MainActivity.service = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = LocationReceiver {
            mainViewModel.sendLocationData(it)
            updateUI(it)
        }

        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        binding.btnStart.let {
            it.setOnClickListener {
                Timber.tag(LOG_TAG).i("START THE SERVICE")
                mainViewModel.turnOnGps(this)
            }
        }

        binding.btnStop.let {
            it.setOnClickListener {
                Timber.tag(LOG_TAG).i("STOP THE SERVICE")
                actionOnService(Actions.STOP)
            }
        }
        binding.mainViewModel = mainViewModel
    }

    private fun updateUI(location: LocationModel) {
        if (service.isForeground()) {
            service.updateData()
        } else {
            tv_updated.text = getLocationTitle(this)
            tv_latitude.text = getString(R.string.latitude, location.latitude)
            tv_longitude.text = getString(R.string.longitude, location.longitude)
        }
    }

    private fun observeData() {
        mainViewModel.isGpsOnBoolean.observe(this, Observer {
            Timber.tag(LOG_TAG).i("is GPS ON: $it")
            if (it) {
                provideLocation()
            } else {
                mainViewModel.turnOnGps(this)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            receiver,
            IntentFilter(ACTION_BROADCAST)
        )
    }

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, LocationService::class.java), connection,
            Context.BIND_AUTO_CREATE
        )
        observeData()
    }

    override fun onStop() {
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            provideLocation()
        }
    }

    private fun provideLocation() {
        when {
            isPermissionsGranted(this) -> {
                actionOnService(Actions.START)
            }

            else -> ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                LOCATION_REQUEST
            )
        }
    }

    private fun actionOnService(action: Actions) {
        if (getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP) return
        Intent(this, LocationService::class.java).also {
            Timber.tag(LOG_TAG).i("Starting the service")
            it.action = action.name
            startService(it)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST -> {
                provideLocation()
            }
        }
    }
}
