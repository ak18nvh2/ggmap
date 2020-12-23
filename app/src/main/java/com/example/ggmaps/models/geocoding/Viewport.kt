package com.example.ggmaps.models.geocoding

import com.example.ggmaps.models.direction.Northeast
import com.example.ggmaps.models.direction.Southwest
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class Viewport {
    @SerializedName("northeast")
    @Expose
    var northeast: Northeast? = null

    @SerializedName("southwest")
    @Expose
    var southwest: Southwest? = null
}