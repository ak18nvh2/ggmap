package com.example.ggmaps.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ggmaps.MapsFactory
import com.example.ggmaps.R
import com.example.ggmaps.models.direction.Route
import com.example.ggmaps.viewmodels.GeocodingViewModel
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    private lateinit var mMap: GoogleMap
    private val mPERMISSION_REQUEST_CODE = 1
    private lateinit var mGeocodingViewModel: GeocodingViewModel
    lateinit var mRouteMarkerList: ArrayList<Marker>
    private lateinit var mRoutePolyline: Polyline
    private lateinit var mLocalMarkerOptions: MarkerOptions
    private var mCurrentAddress = ""
    private var mCountClickToMap = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        init()
    }

    private fun init() {
        mGeocodingViewModel = GeocodingViewModel()
        mRouteMarkerList = ArrayList()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        supportActionBar?.hide()
        btn_getCurrentLocation.setOnClickListener(this)
        btn_searchWay.setOnClickListener(this)
        btn_deleteAllMarker.setOnClickListener(this)
        btn_search.setOnClickListener(this)
        btn_swapMarker.setOnClickListener(this)
        cl_searchWay.visibility = View.GONE
        tv_routeInformation.visibility = View.GONE
        getCurrentLocation()


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener { p0 -> getAddressByGeocode(p0) }
        mMap.setOnMapClickListener {
            mCountClickToMap++
            if (mCountClickToMap % 2 == 1) {
                cl_searchWay.visibility = View.GONE
                cl_btnClearMarker.visibility = View.GONE
                cl_btnGetCurrentLocation.visibility = View.GONE
                cl_btnSearchWay.visibility = View.GONE
                tv_routeInformation.visibility = View.GONE

            } else {
                cl_searchWay.visibility = View.VISIBLE
                cl_btnClearMarker.visibility = View.VISIBLE
                cl_btnGetCurrentLocation.visibility = View.VISIBLE
                cl_btnSearchWay.visibility = View.VISIBLE
                tv_routeInformation.visibility = View.VISIBLE
            }
        }
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view!=null) {
            val imm =  getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken,0)

        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_getCurrentLocation -> {
                getCurrentLocation()
            }
            R.id.btn_searchWay -> {
                cl_searchWay.visibility = View.VISIBLE
                edt_startPoint.setText("Vị trí của bạn")
            }
            R.id.btn_deleteAllMarker -> {
                tv_routeInformation.visibility = View.GONE
                MapsFactory.removeMaker(mMap, mRouteMarkerList)
            }
            R.id.btn_search -> {
                closeKeyboard()
                searchWay(edt_startPoint.text.toString(), edt_destinationPoint.text.toString())

            }
            R.id.btn_swapMarker -> {
                val tempString = edt_startPoint.text.toString()
                edt_startPoint.text = edt_destinationPoint.text
                edt_destinationPoint.setText(tempString)
                searchWay(edt_startPoint.text.toString(), edt_destinationPoint.text.toString())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun searchWay(start: String, des: String) {
        if (start == "Vị trí của bạn") {
            if (des == "Vị trí của bạn") {
                getDirection(mCurrentAddress, mCurrentAddress)
            } else {
                getDirection(mCurrentAddress, des)
            }
        } else {
            if (des == "Vị trí của bạn") {
                getDirection(start, mCurrentAddress)
            } else {
                getDirection(start, des)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getDirection(startPoint: String, destinationPoint: String) {
        mGeocodingViewModel.arrayDirection.observe(this, {
            MapsFactory.removeMaker(mMap, mRouteMarkerList)
            if (it != null) {
                if (it.status.equals("OK")) {
                    var distanceMin: Int =
                        it.routes?.get(0)?.legs?.get(0)?.distance?.value!!.toInt()
                    var distanceIndexMin = 0
                    it.routes?.forEachIndexed { index, route ->
                        setMarkersAndRoute(route, this, mMap,R.color.gray)
                        val distanceTemp = route.legs?.get(0)?.distance?.value!!.toInt()
                        if (distanceTemp < distanceMin) {
                            distanceMin = distanceTemp
                            distanceIndexMin = index
                        }
                    }
                    drawRoute(it.routes?.get(distanceIndexMin)!!)
                } else {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                }
            }
        })
        mGeocodingViewModel.getDirection(startPoint, destinationPoint)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun drawRoute(route: Route) {
        setMarkersAndRoute(route, this, mMap,R.color.blue)
        tv_routeInformation.visibility = View.VISIBLE
        tv_routeInformation.text = route.legs?.get(0)?.distance?.text + " ( " + route.legs?.get(0)?.duration?.text + " )"
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setMarkersAndRoute(route: Route, mContext: Context, mMap: GoogleMap, color: Int) {
        val startLatLng = LatLng(
            route.legs?.get(0)?.startLocation?.lat!!,
            route.legs?.get(0)?.startLocation?.lng!!
        )
        val startMarkerOptions: MarkerOptions =
            MarkerOptions().position(startLatLng).title(route.legs?.get(0)?.startAddress)
                .icon(
                    BitmapDescriptorFactory.fromBitmap(MapsFactory.drawMarker(mContext, "S"))
                )
        val endLatLng = LatLng(
            route.legs?.get(0)?.endLocation?.lat!!,
            route.legs?.get(0)?.endLocation?.lng!!
        )
        val endMarkerOptions: MarkerOptions =
            MarkerOptions().position(endLatLng).title(route.legs?.get(0)?.endAddress)
                .icon(BitmapDescriptorFactory.fromBitmap(MapsFactory.drawMarker(mContext, "D")))
        val startMarker = mMap.addMarker(startMarkerOptions)
        val endMarker = mMap.addMarker(endMarkerOptions)
        mRouteMarkerList.add(startMarker)
        mRouteMarkerList.add(endMarker)

        val polylineOptions = MapsFactory.drawRoute(mContext,color)
        val pointsList = PolyUtil.decode(route.overviewPolyline?.points)
        for (point in pointsList) {
            polylineOptions.add(point)
        }
        mMap.setOnPolylineClickListener {
            Toast.makeText(this, "hien ne", Toast.LENGTH_SHORT).show()
        }
        mRoutePolyline = mMap.addPolyline(polylineOptions)
        mRoutePolyline.isClickable = true
        mMap.animateCamera(MapsFactory.autoZoomLevel(mRouteMarkerList))
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val arrPermission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrPermission, mPERMISSION_REQUEST_CODE)
        } else {
            val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            mFusedLocationClient.lastLocation
                .addOnSuccessListener(this,
                    OnSuccessListener { location ->
                        if (location == null) {
                            return@OnSuccessListener
                        }
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        getAddressByGeocode(currentLocation)
                    })
        }

    }

    private fun getAddressByGeocode(p0: LatLng) {
        mGeocodingViewModel.geocodingLiveData.observe(this, {
            MapsFactory.removeMaker(mMap, mRouteMarkerList)
            if (it != null) {
                mCurrentAddress = it.results?.get(0)?.formattedAddress!!
                mLocalMarkerOptions =
                    MarkerOptions().position(p0)
                        .title(p0.toString())
                        .snippet(it.results?.get(0)?.formattedAddress)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_3))
                val localMarker = mMap.addMarker(mLocalMarkerOptions)
                mRouteMarkerList.add(localMarker)
                mMap.animateCamera(MapsFactory.autoZoomLevel(mRouteMarkerList))
            }
        })
        val latLng: String = p0.latitude.toString() + "," + p0.longitude.toString()
        mGeocodingViewModel.getGeocode(latLng)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == mPERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    getCurrentLocation()
                }
            } else {
                Toast.makeText(this, "no permission", Toast.LENGTH_SHORT).show()
            }
        }
    }
}