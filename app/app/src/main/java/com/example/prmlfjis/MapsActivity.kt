package com.example.prmlfjis

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.example.prmlfjis.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.material.snackbar.Snackbar


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolygonClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val viewModel: MapsViewModel by viewModels()

    private var observingPolygons: MutableList<Pair<String, PolygonOptions>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.viewModel = viewModel
        val areaOutlineObserver =
            Observer<MutableList<Pair<String, PolygonOptions>>> { outlines -> addAreaOutlines(outlines) }
        viewModel.areaOutlines.observe(this, areaOutlineObserver)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // we can probably use Polygons to mark slovenian territory by municipality.
        // when a user clicks on a polygon statistics about crime are shown
        // when user zooms in more they can select a place, and an estimation of
        // how dangerous the place is is shown?? perhaps

        // Get back the mutable Polygon
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(46.1911808, 15.2093154), 7.0f))
        mMap.setOnPolygonClickListener(this)
    }

    private fun addAreaOutlines(outlines: MutableList<Pair<String, PolygonOptions>>) {
        observingPolygons = outlines
        for (outline in outlines) {
            val polygon = mMap.addPolygon(
                outline.second
                    .clickable(true)
            )
            polygon.isClickable = true
            polygon.tag = outline.first
        }
    }

    override fun onPolygonClick(polygon: Polygon) {
        Toast.makeText(this, "Regija: ${polygon.tag?.toString()}", Toast.LENGTH_SHORT).show()
    }
}