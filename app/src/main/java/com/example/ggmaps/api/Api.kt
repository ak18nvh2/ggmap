package com.example.ggmaps.api

import com.example.ggmaps.models.geocoding.Geocoding
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {
//    @GET("directions/json")
//    fun getDirections(
//        @Query("origin") origin: String,
//        @Query("destination") destination: String,
//        @Query("key") key: String,
//        @Query("alternatives") alternatives: Boolean = true
//    ): Call<Direction>
//
//    @GET("place/textsearch/json")
//    fun getAddressbySearch(
//        @Query("query") location: String,
//        @Query("key") key: String
//    ): Call<AddressbySearch>

    @GET("geocode/json")
    fun getNearbySearch(
        @Query("latlng") latLng: String,
        @Query("key") key: String
    ): Call<Geocoding>
}