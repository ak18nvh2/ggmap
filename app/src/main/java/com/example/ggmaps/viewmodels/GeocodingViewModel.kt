package com.example.ggmaps.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ggmaps.GoogleAPIKey
import com.example.ggmaps.api.RetrofitClient
import com.example.ggmaps.models.direction.Direction
import com.example.ggmaps.models.geocoding.Geocoding
import retrofit2.Call
import retrofit2.Response

class GeocodingViewModel : ViewModel() {

    var geocodingLiveData: MutableLiveData<Geocoding> = MutableLiveData()
    var arrayDirection: MutableLiveData<Direction> = MutableLiveData()
    fun getGeocode(latlng: String) {
        val call = RetrofitClient.instance.getNearbySearch(
            latlng,
            GoogleAPIKey.GOOGLE_API_KEY
        )
        call.enqueue(object : retrofit2.Callback<Geocoding> {
            override fun onResponse(call: Call<Geocoding>, response: Response<Geocoding>) {
                if (response.isSuccessful) {
                    Log.d("MainActivity", "google loaded from API")
                    geocodingLiveData.postValue(response.body())
                } else {
                    Log.d("MainActivity", response.message() + response.code())
                    geocodingLiveData.postValue(null)
                }
            }

            override fun onFailure(call: Call<Geocoding>, t: Throwable) {
                Log.d("MainActivity", "error loading from API")
                geocodingLiveData.postValue(null)
            }

        })
    }

    fun getDirection(start: String, destination: String) {
        val call =
            RetrofitClient.instance.getDirections(start, destination, GoogleAPIKey.GOOGLE_API_KEY)
        call.enqueue(object : retrofit2.Callback<Direction> {
            override fun onFailure(call: Call<Direction>, t: Throwable) {
                Log.d("MainActivity", "error loading from API")
                arrayDirection.postValue(null)
            }

            override fun onResponse(call: Call<Direction>, response: Response<Direction>) {
                if (response.isSuccessful) {
                    Log.d("MainActivity", "google loaded from API")
                    arrayDirection.postValue(response.body())
                } else {
                    Log.d("MainActivity", response.message() + response.code())
                    arrayDirection.postValue(null)
                }
            }
        }
        )
    }
}