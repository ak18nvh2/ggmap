package com.example.ggmaps.models.direction

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class Polyline {
    @SerializedName("points")
    @Expose
    var points: String? = null
}