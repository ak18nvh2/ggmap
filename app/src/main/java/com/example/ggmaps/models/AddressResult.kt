package com.example.ggmaps.models

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class AddressResult {
    @SerializedName("html_attributions")
    @Expose
    var htmlAttributions: List<Any>? = null

    @SerializedName("results")
    @Expose
    var results: List<Result>? = null

    @SerializedName("status")
    @Expose
    var status: String? = null
}