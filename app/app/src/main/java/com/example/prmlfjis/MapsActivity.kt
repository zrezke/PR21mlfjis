package com.example.prmlfjis

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.Observer
import com.example.prmlfjis.databinding.ActivityMapsBinding
import com.example.prmlfjis.network.GeocodeResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import kotlin.math.min

private val GREEN = Color.parseColor("#fffff600")
private val RED = Color.parseColor("#ffff0505")


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolygonClickListener,
    GoogleMap.OnCameraMoveListener, GoogleMap.OnMapClickListener {

    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog

    private lateinit var data : JSONObject
    private lateinit var placeTypeMappings: JSONObject
    private lateinit var placeDangerousness: JSONObject
    private lateinit var regionDangerousness: JSONObject
    private lateinit var cityDangerousness: JSONObject

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val viewModel: MapsViewModel by viewModels()

    private var observingPolygons: MutableList<Pair<String, PolygonOptions>> = mutableListOf()

    private var observingScope: String = "region"

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

        data = JSONObject(assets.open("data.json").bufferedReader().use { it.readText() })
        regionDangerousness = loadJsonData("regions")
        cityDangerousness = loadJsonData("city")
        placeTypeMappings = loadJsonData("place_map")
        placeDangerousness = loadJsonData("places")
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
        val marker = MarkerOptions()
        marker.position(p0)
            .title("Danger estimate")
        Log.d("MARKER", marker.toString())
        viewModel.getReverseGeocoding(p0)
            .observe(this) { placeData ->

                var dangerEstimate = 0f
                var nEstimates = 0f
                placeData.results.forEach { result: GeocodeResult ->
                    result.types!!.forEach { placeType ->
                        dangerEstimate += calculatePlaceTypeDanger(placeType)
                        nEstimates++
                    }
                }
                dangerEstimate /= nEstimates
                dangerEstimate *= 100
//                dangerEstimate += normalize

                marker.snippet("Estimated danger: $dangerEstimate")
                val iWindow: Marker? = mMap.addMarker(marker)
                iWindow?.showInfoWindow()
            }
    }

    private fun calculatePlaceTypeDanger(placeType: String): Float {
        val placeMapping: String = placeTypeMappings.getString(placeType)
        Log.d("MAPPED", "$placeType to $placeMapping")
        val placeDanger: Double = placeDangerousness.getDouble(placeMapping)
        Log.d("Danger of place $placeMapping", placeDanger.toString())
        return placeDanger.toFloat()
    }

    override fun onCameraMove() {
        val zoom = mMap.cameraPosition.zoom
//        Log.d("ZOOM", zoom.toString())
        if (zoom in 8.0..12.0 && observingScope != "city" && observingScope != "places") {
            Log.d("ZOOM-if", "city")
            mMap.clear()
            observingScope = "city"
            viewModel.chooseAreaOutlines("city")
        } else if (zoom < 8.0 && observingScope != "region") {
            Log.d("ZOOM-if", "region")
            mMap.clear()
            observingScope = "region"
            viewModel.chooseAreaOutlines("region")
        } else if (observingScope == "city" && zoom > 12) {
            observingScope = "places"
            mMap.clear()
        } else if (observingScope == "places" && zoom <= 12) {
            observingScope = "city"
            mMap.clear()
            viewModel.chooseAreaOutlines("city")
        }
    }

    private fun addAreaOutlines(outlines: MutableList<Pair<String, PolygonOptions>>) {
        observingPolygons = outlines
        for (outline in outlines) {
            val dataName = outline.first
            val observingData: JSONObject = loadJsonData(observingScope, dataName)
            val dangerScore = makeDangerScore(observingData)
            val greenColor = if (observingScope == "region") GREEN - (0x33 shl 24) else GREEN - (0x66 shl 24)
            val redColor = if (observingScope == "region") RED - (0x33 shl 24) else RED - (0x66 shl 24)
            outline.second.fillColor(
                ColorUtils.blendARGB(
                    greenColor,
                    redColor,
                    min(1f, dangerScore.toFloat() / 70f)
                )
            )
            val polygon = mMap.addPolygon(
                outline.second
                    .clickable(true)
            )
            polygon.isClickable = true
            polygon.tag = outline.first
        }
    }

    private fun dataDialog(region: String) {
        dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(region.uppercase())
        val dataPopupView: View = layoutInflater.inflate(R.layout.data_dialog, null)

        dataPopupView.findViewById<Button>(R.id.buttonClose).setOnClickListener {
            dialog.dismiss()
        }

        dialogBuilder.setView(dataPopupView)
        dialog = dialogBuilder.create()

        try {
            setDialogData(dataPopupView, region)
            dialog.show()
        } catch (e: Exception) {
            Snackbar.make(
                binding.root,
                "No data available for this region",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun loadJsonData(dataScope: String, dataName: String? = null): JSONObject {
        if (dataScope == "region") {
            return data.getJSONObject("regions").apply {
                if (dataName != null) return this.getJSONObject(dataName.lowercase())
            }
        } else if (dataScope == "city") {
            return data.getJSONObject("cities").apply {
                if (dataName != null) return this.getJSONObject(dataName.lowercase())
            }
        } else {
            return data.getJSONObject(dataScope).apply {
                if (dataName != null) return this.getJSONObject(dataName.lowercase())
            }
        }
    }

    private fun setDialogData(dataPopupView: View, dataName: String) {
        Log.d("CLICKED:", dataName.lowercase())
        val observingData: JSONObject = loadJsonData(observingScope, dataName)

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

        val dangerScore = makeDangerScore(observingData)  // normalized with 150 for the value to have more meaning
        dataPopupView.findViewById<ProgressBar>(R.id.progressBar).progress = dangerScore
//        dataPopupView.resources.getString(R.string.danger_score, dangerScore)
//        ABOVE WAY NOT WORKING FOR SOME REASON??
        dataPopupView.findViewById<TextView>(R.id.textDangerScore).text =
            "Danger score: $dangerScore%"
    }

    private fun makeDangerScore(observingData: JSONObject): Int {
        return (observingData.getInt("data_danger_score") * 100) / 130
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
}
