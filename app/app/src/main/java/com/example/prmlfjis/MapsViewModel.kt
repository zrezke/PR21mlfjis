package com.example.prmlfjis

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prmlfjis.network.NominatimApi
import kotlinx.coroutines.launch

class MapsViewModel : ViewModel() {

    public fun getAreaOutline(country: String="slovenija", city: String="", query: String="") {
        viewModelScope.launch {
            val result: String = NominatimApi.retrofitService.getAreaOutline(country, city, query)
            print("LMAO: " + result)
        }
    }
}