package com.example.location.di.component

import com.example.location.di.modul.RemoteModule
import com.example.location.ui.MainViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(RemoteModule::class)])
interface ViewModelInjector {

    fun inject(mainViewModel: MainViewModel)

    @Component.Builder
    interface Builder {
        fun build(): ViewModelInjector

        fun networkModule(networkModule: RemoteModule): Builder
    }
}