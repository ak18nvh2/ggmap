package com.example.ggmaps.models

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class Step {
    @SerializedName("distance")
    @Expose
    var distance: Distance? = null

    @SerializedName("duration")
    @Expose
    var duration: Duration? = null

    @SerializedName("end_location")
    @Expose
    var endLocation: EndLocation? = null

    @SerializedName("html_instructions")
    @Expose
    var htmlInstructions: String? = null

    @SerializedName("polyline")
    @Expose
    var polyline: Polyline? = null

    @SerializedName("start_location")
    @Expose
    var startLocation: StartLocation? = null

    @SerializedName("travel_mode")
    @Expose
    var travelMode: String? = null

    @SerializedName("maneuver")
    @Expose
    var maneuver: String? = null
}