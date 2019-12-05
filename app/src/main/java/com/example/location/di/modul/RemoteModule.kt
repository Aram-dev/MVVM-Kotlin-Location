package com.example.location.di.modul

import com.example.location.NET_BASE_URL
import com.example.location.api.endpoint.ApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
class RemoteModule {

    @Provides
    @Reusable
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides
    @Reusable
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    @Provides
    @Reusable
    fun provideRetrofit(gSon: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(NET_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gSon))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
}