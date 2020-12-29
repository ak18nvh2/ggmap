package com.example.ggmaps.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ggmaps.MapsFactory
import com.example.ggmaps.R
import com.example.ggmaps.models.AddressResult
import com.example.ggmaps.models.Route
import com.example.ggmaps.viewmodels.MapViewModel
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
    private lateinit var mMapViewModel: MapViewModel
    lateinit var mRouteMarkerList: ArrayList<Marker>
    private lateinit var mRoutePolyline: Polyline
    private lateinit var mLocalMarkerOptions: MarkerOptions
    private var mCurrentAddress = ""
    private var mCountClickToMap = 0
    private var mIdPolyCurrent = ""
    private lateinit var mArrayListPolyLine: ArrayList<Polyline>
    private lateinit var mArrayListRoute: ArrayList<Route>
    private var mSearchState = 0

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        init()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun init() {

        mArrayListRoute = ArrayList()
        mMapViewModel = MapViewModel()
        mRouteMarkerList = ArrayList()
        mArrayListPolyLine = ArrayList()
        mMapViewModel = MapViewModel()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        supportActionBar?.hide()
        btn_getCurrentLocation.setOnClickListener(this)
        btn_searchWay.setOnClickListener(this)
        btn_deleteAllMarker.setOnClickListener(this)
        btn_search.setOnClickListener(this)
        btn_searchAddress.setOnClickListener(this)
        btn_swapMarker.setOnClickListener(this)
        cl_searchWay.visibility = View.GONE
        tv_routeInformation.visibility = View.GONE
        tv_summary.visibility = View.GONE
        getCurrentLocation()
        registerLiveDataListener()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener { p0 ->
            getAddressByGeocode(p0)
            clearSearchState()
        }
        mMap.setOnMapClickListener {
            mCountClickToMap++
            if (mCountClickToMap % 2 == 1) {
                cl_searchWay.visibility = View.GONE
                cl_btnClearMarker.visibility = View.GONE
                cl_btnGetCurrentLocation.visibility = View.GONE
                cl_btnSearchWay.visibility = View.GONE
                tv_routeInformation.visibility = View.GONE
                tv_summary.visibility = View.GONE
                edt_inputAddress.visibility = View.GONE
                btn_searchAddress.visibility = View.GONE
            } else {
                cl_btnClearMarker.visibility = View.VISIBLE
                cl_btnGetCurrentLocation.visibility = View.VISIBLE
                cl_btnSearchWay.visibility = View.VISIBLE
                if (mSearchState == 1) {
                    tv_routeInformation.visibility = View.VISIBLE
                    tv_summary.visibility = View.VISIBLE
                    cl_searchWay.visibility = View.VISIBLE

                    edt_inputAddress.visibility = View.GONE
                    btn_searchAddress.visibility = View.GONE
                } else {
                    tv_routeInformation.visibility = View.GONE
                    tv_summary.visibility = View.GONE
                    cl_searchWay.visibility = View.GONE

                    edt_inputAddress.visibility = View.VISIBLE
                    btn_searchAddress.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
    private fun clearSearchState() {
        if (mSearchState == 1) {
            mSearchState = 0
            MapsFactory.removeMaker(mMap, mRouteMarkerList)
            tv_routeInformation.visibility = View.GONE
            tv_summary.visibility = View.GONE
            cl_searchWay.visibility = View.GONE
        }
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_getCurrentLocation -> {
                getCurrentLocation()
                clearSearchState()

                btn_searchAddress.visibility = View.VISIBLE
                edt_inputAddress.visibility = View.VISIBLE
            }
            R.id.btn_searchWay -> {
                cl_searchWay.visibility = View.VISIBLE
                btn_searchAddress.visibility = View.GONE
                edt_inputAddress.visibility = View.GONE
                edt_startPoint.setText(mCurrentAddress)
            }
            R.id.btn_deleteAllMarker -> {
                tv_routeInformation.visibility = View.GONE
                tv_summary.visibility = View.GONE
                cl_searchWay.visibility = View.GONE
                MapsFactory.removeMaker(mMap, mRouteMarkerList)
                mSearchState = 0
                btn_searchAddress.visibility = View.VISIBLE
                edt_inputAddress.visibility = View.VISIBLE
            }
            R.id.btn_search -> {
                closeKeyboard()
                mMapViewModel.getDirection(
                    edt_startPoint.text.toString(),
                    edt_destinationPoint.text.toString()
                )
                mSearchState = 1
            }
            R.id.btn_swapMarker -> {
                closeKeyboard()
                val tempString = edt_startPoint.text.toString()
                edt_startPoint.text = edt_destinationPoint.text
                edt_destinationPoint.setText(tempString)
                mMapViewModel.getDirection(
                    edt_startPoint.text.toString(),
                    edt_destinationPoint.text.toString()
                )
            }
            R.id.btn_searchAddress -> {
                closeKeyboard()
                clearSearchState()
                mMapViewModel.getAddress(edt_inputAddress.text.toString())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun registerLiveDataListener() {
        mMapViewModel.geocodingLiveData.observe(this, {
            MapsFactory.removeMaker(mMap, mRouteMarkerList)
            if (it != null) {
                val latLng = LatLng(
                    it.results?.get(0)?.geometry?.location?.lat!!,
                    it.results?.get(0)?.geometry?.location?.lng!!
                )
                mCurrentAddress = it.results?.get(0)?.formattedAddress!!
                mLocalMarkerOptions =
                    MarkerOptions().position(latLng)
                        .title(latLng.toString())
                        .snippet(it.results?.get(0)?.formattedAddress)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_3))
                val localMarker = mMap.addMarker(mLocalMarkerOptions)
                mRouteMarkerList.add(localMarker)
                mMap.animateCamera(MapsFactory.autoZoomLevel(mRouteMarkerList))
            }
        })
        mMapViewModel.arrayDirection.observe(this, {
            MapsFactory.removeMaker(mMap, mRouteMarkerList)
            mArrayListPolyLine.clear()
            mArrayListRoute.clear()
            if (it != null) {
                if (it.status.equals("OK")) {
                    mArrayListRoute = it.routes as ArrayList<Route>
                    drawAllRoute(mArrayListRoute, -1)
                } else {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                }
            }
        })
        mMapViewModel.addressResultLiveData.observe(this, {
            if (it != null) {
                if (it.status.equals("OK")) {
                    drawOnePoint(it, mMap)
                } else {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun drawAllRoute(routes: ArrayList<Route>, id: Int) {
        mMap.clear()
        if (id == -1) {
            var distanceMin: Int = routes[0].legs?.get(0)?.distance?.value!!.toInt()
            var distanceIndexMin = 0
            routes.forEachIndexed { index, route ->
                val distanceTemp = route.legs?.get(0)?.distance?.value!!.toInt()
                if (distanceTemp < distanceMin) {
                    distanceMin = distanceTemp
                    distanceIndexMin = index
                }
            }
            val routeTemp = routes[distanceIndexMin]
            routes[distanceIndexMin] = routes[routes.size - 1]
            routes[routes.size - 1] = routeTemp
        } else {
            val routeTemp = routes[id]
            routes[id] = routes[routes.size - 1]
            routes[routes.size - 1] = routeTemp
        }

        routes.forEachIndexed { index, _ ->
            if (index == routes.size - 1) {
                drawOneRoute(routes, routes[index], this, mMap, R.color.green)
            } else {
                drawOneRoute(routes, routes[index], this, mMap, R.color.gray)
            }
        }

        tv_routeInformation.visibility = View.VISIBLE
        tv_summary.visibility = View.VISIBLE
        tv_routeInformation.text =
            routes[routes.size - 1].legs?.get(0)?.distance?.text + " ( " + routes[routes.size - 1].legs?.get(
                0
            )?.duration?.text + " )"
        tv_summary.text = routes[routes.size - 1].summary
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun drawOneRoute(
        routes: ArrayList<Route>,
        route: Route,
        mContext: Context,
        mMap: GoogleMap,
        color: Int
    ) {
        val startLatLng = LatLng(
            route.legs?.get(0)?.startLocation?.lat!!,
            route.legs?.get(0)?.startLocation?.lng!!
        )
        val startMarkerOptions: MarkerOptions =
            MarkerOptions().position(startLatLng)
                .title(route.legs?.get(0)?.startAddress)
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

        val polylineOptions = MapsFactory.drawRoute(mContext, color)
        val pointsList = PolyUtil.decode(route.overviewPolyline?.points)
        for (point in pointsList) {
            polylineOptions.add(point)
        }
        mRoutePolyline = mMap.addPolyline(polylineOptions)
        mArrayListPolyLine.add(mRoutePolyline)
        if (color == R.color.blue) {
            mIdPolyCurrent = mRoutePolyline.id
        }
        mMap.setOnPolylineClickListener {
            var idTemp = -1
            mArrayListPolyLine.forEachIndexed { index, polyline ->
                if (polyline.id == it.id) {
                    idTemp = index
                }
            }
            mArrayListPolyLine.clear()
            drawAllRoute(routes, idTemp)
        }

        mRoutePolyline.isClickable = true
        mMap.animateCamera(MapsFactory.autoZoomLevel(mRouteMarkerList))

    }

    private fun drawOnePoint(addressResult: AddressResult, mMap: GoogleMap) {
        val latLng = LatLng(
            addressResult.results?.get(0)?.geometry?.location?.lat!!,
            addressResult.results?.get(0)?.geometry?.location?.lng!!
        )
        mLocalMarkerOptions =
            MarkerOptions().position(latLng)
                .title(latLng.toString())
                .snippet(addressResult.results?.get(0)?.formattedAddress)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_3))
        val localMarker = mMap.addMarker(mLocalMarkerOptions)
        mRouteMarkerList.add(localMarker)
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
        val latLng: String = p0.latitude.toString() + "," + p0.longitude.toString()
        mMapViewModel.getGeocode(latLng)
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