package com.example.ggmaps.models.geocoding

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class Geocoding {
    @SerializedName("results")
    @Expose
    var results: List<Result>? = null

    @SerializedName("status")
    @Expose
    var status: String? = null
}