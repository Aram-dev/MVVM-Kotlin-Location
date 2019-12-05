package com.example.location.data.model

data class LocationDataModel(
    val txUpdated: String,
    val txLatitude: String,
    val txLongitude: String
) {
    override fun toString(): String {
        return "$txUpdated\n$txLatitude\n$txLongitude"
    }
}