package com.example.location.ui

import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.example.location.LOG_TAG
import com.example.location.api.repo.Repository
import com.example.location.data.model.LocationDataModel
import com.example.location.data.model.LocationModel
import com.example.location.utils.LocationUtils
import com.example.location.utils.getLatitudeText
import com.example.location.utils.getLocationTitle
import com.example.location.utils.getLongitudeText
import io.reactivex.disposables.Disposable
import timber.log.Timber
import javax.inject.Inject


class MainViewModel : BaseViewModel() {

    @Inject
    lateinit var repository: Repository

    private var subscription: Disposable? = null

    var dataModel: ObservableField<LocationDataModel> = ObservableField()

    private var isGpsOn: MutableLiveData<Boolean> = MutableLiveData()

    var isGpsOnBoolean = isGpsOn.apply {
        this.value
    }

    fun sendLocationData(context: Context, location: LocationModel) {
        updateData(context, location)
        subscription = repository.sendLocation(location)
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

    fun turnOnGps(context: Context) {
        LocationUtils().turnGPSOn(context) {
            isGpsOn.value = it
        }
    }

    private fun updateData(context: Context, location: LocationModel) {
        dataModel.set(location.mapToData(context))
    }

    private fun LocationModel.mapToData(context: Context): LocationDataModel? {
        return LocationDataModel(
            txUpdated = getLocationTitle(context),
            txLatitude = getLatitudeText(context, this),
            txLongitude = getLongitudeText(context, this)
        ).apply {
            Timber.tag(LOG_TAG).i( this.toString() )
        }
    }
}
