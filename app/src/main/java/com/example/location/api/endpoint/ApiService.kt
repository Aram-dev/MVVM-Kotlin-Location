package com.example.location.api.endpoint

import com.example.location.data.model.LocationModel
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("location")
    fun sendLocation(@Body request: LocationModel): Single<String>
}