package com.example.location.api.repo

import com.example.location.LOG_TAG
import com.example.location.api.endpoint.ApiService
import com.example.location.data.model.LocationModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class Repository @Inject constructor(
    private val apiService: ApiService
) {
    fun sendLocation(location: LocationModel): Disposable {
        return apiService.sendLocation(location)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                Timber.tag(LOG_TAG).i("OnSubscribe: Disposable- ${it.isDisposed}")
            }
            .doOnTerminate {
                Timber.tag(LOG_TAG).i("OnTerminate: Location- $location")
            }
            .doOnSuccess {
                Timber.tag(LOG_TAG).i("OnSuccess: $it")
            }
            .doOnError {
                Timber.tag(LOG_TAG).i("OnError: ${it.message}")
            }
            .subscribe({}, {})
    }
}