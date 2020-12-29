package com.example.ggmaps.models

import com.example.ggmaps.models.Northeast
import com.example.ggmaps.models.Southwest
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