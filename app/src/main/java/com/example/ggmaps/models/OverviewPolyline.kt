package com.example.ggmaps.models

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class OverviewPolyline {
    @SerializedName("points")
    @Expose
    var points: String? = null
}