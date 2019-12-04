package com.example.location.di

import com.example.location.App
import com.example.location.di.modul.RemoteModule
import dagger.Component
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [RemoteModule::class])
interface AppComponent : AndroidInjector<App>