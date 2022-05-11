package com.example.prmlfjis

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prmlfjis.network.NominatimApi
import com.example.prmlfjis.network.NominatimJsonData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import kotlinx.coroutines.launch

class MapsViewModel : ViewModel() {

    private val _status = MutableLiveData<String>()
    private val regije: List<String> = listOf(
        "osrednjeslovenska",
        "Obalno - kraška",
        "Primorsko - notranjska",
        "jugovzhodna Slovenija",
        "posavska",
        "zasavska",
        "savinjska",
        "podravska",
        "koroška",
        "pomurska",
        "goriška",
        "gorenjska"
    )

    val status: LiveData<String> = _status

    private val _areaOutlines = MutableLiveData<MutableList<PolygonOptions>>()
    val areaOutlines : LiveData<MutableList<PolygonOptions>> = _areaOutlines

    init {
        makeAreaOutlines()
    }

    private fun getAreaOutline(country: String="Slovenija", city: String="") {
        viewModelScope.launch {
            try {
                val result: List<NominatimJsonData> = NominatimApi.retrofitService.getAreaOutline(country, city)
                _status.value = result[0].geotext
                Log.i("GEOTEXT: ", _status.value!!)
            } catch (e: Exception) {
                Log.i("Nemska sestka ERROR:", "${e.message}")
                _status.value = "Nemska sestka ERROR: ${e.message}"
            }
        }
    }

    private fun makeAreaOutlines() {
        val result = mutableListOf<PolygonOptions>()
        viewModelScope.launch {
            for (regija in regije) {
                try {
                    Log.i("REGIJA: ", regija)
                    val polygonText = NominatimApi.retrofitService.searchQuery(query="${regija} slovenija")[0].geotext
                    Log.i("POLY  for ${regija}: ", polygonText)
                    result.add(parsePolygonText(polygonText))
                }
                catch (e: Exception) {
                    Log.i("FAIL REGIJA: ", regija)
                    Log.i("API Request error: ", e.stackTraceToString())
                }

            }
            _areaOutlines.value = result
        }
    }


    private fun parsePolygonText(polygonText: String) : PolygonOptions {
        val polygonOptions = PolygonOptions()
            .fillColor(Color.RED)
            .strokeColor(Color.BLACK)
        val latLngStrList = polygonText.replace("POLYGON((", "").replace("))", "").split(",")
        for (latLngStr in latLngStrList) {
            val latLng: MutableList<String> = latLngStr.split(" ") as MutableList<String>
            if (")" in latLng[1]) {
                latLng[1] = latLng[1].replace(")", "")
                polygonOptions.add(LatLng(latLng[1].toDouble(), latLng[0].toDouble()))
                break
            }
            polygonOptions.add(LatLng(latLng[1].toDouble(), latLng[0].toDouble()))
        }
        return polygonOptions
    }
}