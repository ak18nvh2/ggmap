package com.example.ggmaps.api

import com.example.ggmaps.models.AddressResult
import com.example.ggmaps.models.Direction
import com.example.ggmaps.models.Geocoding
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {
    @GET("directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") key: String,
        @Query("alternatives") alternatives: Boolean = true
    ): Call<Direction>

    @GET("place/textsearch/json")
    fun getAddressResult(
        @Query("query") location: String,
        @Query("key") key: String
    ): Call<AddressResult>

    @GET("geocode/json")
    fun getNearbySearch(
        @Query("latlng") latLng: String,
        @Query("key") key: String
    ): Call<Geocoding>
}