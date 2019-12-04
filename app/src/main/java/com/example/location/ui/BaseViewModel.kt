package com.example.location.ui

import androidx.lifecycle.ViewModel
import com.example.location.di.component.DaggerViewModelInjector
import com.example.location.di.component.ViewModelInjector
import com.example.location.di.modul.RemoteModule

abstract class BaseViewModel: ViewModel(){

    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .networkModule(RemoteModule())
        .build()

    init {
        inject()
    }
    private fun inject() {
        when (this) {
            is MainViewModel -> injector.inject(this)
        }
    }
}