package com.example.ggmaps

import android.content.Context
import android.graphics.*
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PolylineOptions

object MapsFactory {
    fun autoZoomLevel(markerList: ArrayList<Marker>, zoomLevel: Float): CameraUpdate {
        return if (markerList.size == 1) {
            val latitude = markerList[0].position.latitude
            val longitude = markerList[0].position.longitude
            val latLng = LatLng(latitude, longitude)
            return if (zoomLevel != 16f) {
                CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)
            } else {
                CameraUpdateFactory.newLatLngZoom(latLng, 16f)
            }

        } else {
            val builder = LatLngBounds.Builder()
            for (marker in markerList) {
                builder.include(marker.position)
            }
            val bounds = builder.build()
            val padding = 200
            CameraUpdateFactory.newLatLngBounds(bounds, padding)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun drawMarker(context: Context, text: String): Bitmap {
        val drawable = context.resources.getDrawable(R.drawable.ic_marker_2, context.theme)
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        val paint = Paint()
        paint.textSize = context.resources.displayMetrics.density * 16
        paint.style = Paint.Style.FILL
        val textCanvas = Canvas(bitmap)
        textCanvas.drawText(
            text,
            ((bitmap.width * 7) / 20).toFloat(),
            (bitmap.height / 2).toFloat(),
            paint
        )
        return bitmap
    }

    fun drawRoute(context: Context, color: Int): PolylineOptions {
        val polylineOptions = PolylineOptions()
        polylineOptions.width(DisplayUtility.px2dip(context, 40.toFloat()).toFloat())
        polylineOptions.geodesic(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            polylineOptions.color(context.resources.getColor(color, context.theme))
        } else {
            polylineOptions.color(context.resources.getColor(color))
        }
        return polylineOptions
    }

    fun removeMaker(mGoogleMap: GoogleMap,markerList: ArrayList<Marker>) {
        markerList.clear()
        mGoogleMap.clear()
    }
}