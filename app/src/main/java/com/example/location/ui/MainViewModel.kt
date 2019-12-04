package com.example.location.ui

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.location.api.repo.Repository
import com.example.location.data.model.LocationModel
import com.example.location.utils.LocationUtils
import io.reactivex.disposables.Disposable
import javax.inject.Inject


class MainViewModel : BaseViewModel() {

    @Inject
    lateinit var repository: Repository

    private var subscription: Disposable? = null

    private var isGpsOn: MutableLiveData<Boolean> = MutableLiveData()

    var isGpsOnBoolean = isGpsOn.apply {
        this.value
    }

    fun sendLocationData(location: LocationModel) {
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
}