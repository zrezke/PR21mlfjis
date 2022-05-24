package com.example.prmlfjis

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.lifecycle.Observer

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.prmlfjis.databinding.ActivityMapsBinding
import com.example.prmlfjis.network.GoogleMapsGeocodingResults
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import org.json.JSONObject


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolygonClickListener,
    GoogleMap.OnCameraMoveListener, GoogleMap.OnMapClickListener {

    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val viewModel: MapsViewModel by viewModels()

    private var observingPolygons: MutableList<Pair<String, PolygonOptions>> = mutableListOf()

    private var observingScope: String = ""

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
            Observer<MutableList<Pair<String, PolygonOptions>>> { outlines ->
                addAreaOutlines(
                    outlines
                )
            }
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
        mMap.setOnMapClickListener(this)
        mMap.setOnCameraMoveListener(this)
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
        if (mMap.cameraPosition.zoom <= 12) {
            polygon.tag?.toString()?.let { dataDialog(it) }
        }
    }

    override fun onMapClick(p0: LatLng) {
        if (mMap.cameraPosition.zoom <= 12) {
            return
        }
        mMap.clear()
        val marker : MarkerOptions = MarkerOptions()
        marker.position(p0)
        mMap.addMarker(marker);
        Log.d("MARKER", marker.toString())
        viewModel.getReverseGeocoding(p0).observe(this, Observer<GoogleMapsGeocodingResults>{ placeData ->
            Log.d("Place DATA", placeData.results.toString())
        })
    }


    fun dataDialog(region: String) {
        dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(region.uppercase())
        val dataPopupView: View = layoutInflater.inflate(R.layout.data_dialog, null)

        dataPopupView.findViewById<Button>(R.id.buttonClose).setOnClickListener {
            dialog.dismiss()
        }

        dialogBuilder.setView(dataPopupView)
        dialog = dialogBuilder.create()

        try {
            setDialolgData(dataPopupView, region)
            dialog.show()
        } catch (e: Exception) {
            Snackbar.make(
                binding.root,
                "No data available for this region",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun setDialolgData(dataPopupView: View, dataName: String) {
        val data = assets.open("data.json").bufferedReader().use { it.readText() }
        Log.d("CLICKED:", dataName.lowercase())
        var observingData : JSONObject = JSONObject(data)
        if (observingScope == "region") {
            observingData = JSONObject(data).getJSONObject("regions").getJSONObject(dataName.lowercase())
        } else if (observingScope == "city") {
            observingData = JSONObject(data).getJSONObject("cities").getJSONObject(dataName.lowercase())
        }

        // set data
        val tableLayout = dataPopupView.findViewById<TableLayout>(R.id.dataCardsTableLayout)
        tableLayout.removeAllViews()
        val dataNames = observingData.getJSONArray("data_names")
        val dataValues = observingData.getJSONArray("data_values")

        var row = createNewDialogDataRow()
        for (i in 0 until dataNames.length()) {
            if (row.childCount >= 2) {
                tableLayout.addView(row)
                row = createNewDialogDataRow()
            }
            // create data_card.xml
            val dataCardView = layoutInflater.inflate(R.layout.data_card, null)
            dataCardView.findViewById<TextView>(R.id.dataCardName).text = dataNames.getString(i)
            dataCardView.findViewById<TextView>(R.id.dataCardValue).text = dataValues.getString(i)
            row.addView(dataCardView)
        }
        tableLayout.addView(row)

        val dangerScore = (observingData.getInt("data_danger_score") * 100) / 150  // normalized with 150 for the value to have more meaning
        dataPopupView.findViewById<ProgressBar>(R.id.progressBar).progress = dangerScore
//        dataPopupView.resources.getString(R.string.danger_score, dangerScore)
//        ABOVE WAY NOT WORKING FOR SOME REASON??
        dataPopupView.findViewById<TextView>(R.id.textDangerScore).text =
            "Danger score: $dangerScore%"
    }

    private fun createNewDialogDataRow(): TableRow {
        val row = TableRow(this)
        row.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        row.gravity = Gravity.CENTER
        return row
    }

    override fun onCameraMove() {
        val zoom = mMap.cameraPosition.zoom
//        Log.d("ZOOM", zoom.toString())
        if (zoom in 8.0..12.0 && observingScope != "city" && observingScope != "places") {
            Log.d("ZOOM-if", "city")
            mMap.clear()
            viewModel.chooseAreaOutlines("city")
            observingScope = "city"
        } else if (zoom < 8.0 && observingScope != "region") {
            Log.d("ZOOM-if", "region")
            mMap.clear()
            viewModel.chooseAreaOutlines("region")
            observingScope = "region"
        } else if (observingScope == "city" && zoom > 12) {
            observingScope = "places"
            mMap.clear()
        }
    }
}
