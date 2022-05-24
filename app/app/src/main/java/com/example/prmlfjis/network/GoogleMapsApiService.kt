package com.example.prmlfjis.network

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query


private const val API_KEY = "AIzaSyBk2hVFC4B0xWF3X1i4LCOMsmHdezATCSA"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private const val BASE_URL =
    "https://maps.googleapis.com/maps/api/";

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

data class AddressComponents (
    @Json(name="short_name") val shortName : String?,
    @Json(name="long_name") val longName : String?,
    @Json(name="postcode_localities") val postcodeLocalities : List<String>?,
    val types : List<String>?
)

data class GeocodeResult (
    val types: List<String>?,
    val formatted_address : String?,
    val address_components : List<AddressComponents>?,
    @Json(name="partial_match") val partialMatch : Boolean?,
    @Json(name="place_id") val placeId : String?,
    @Json(name="postcode_localities") val postcodeLocalities : List<String>?,
    // also has: geometry, but we don't need at all, and it would be painful to implement i think
)

data class GoogleMapsGeocodingResults (
    val results : List<GeocodeResult>
)

interface GoogleMapsApiService {

    /**
     * Put in latlng represented as exmaple: 40.714224,-73.961452
     *
     * Get out information about place/places at location...
     * - https://developers.google.com/maps/documentation/geocoding/requests-reverse-geocoding
     */

    @Headers("X-Android-Package: com.example.prmlfjis")
    @GET("geocode/json")
    suspend fun reverseGeocoding(
        @Query(value="latlng", encoded=true) latlng : String,
        @Query(value="key", encoded=true) key : String = API_KEY,
        @Header("X-Android-Cert") sha1 : String
    ) : GoogleMapsGeocodingResults
}


object GoogleMapsApi {
    val retrofitService: GoogleMapsApiService by lazy {
        retrofit.create(GoogleMapsApiService::class.java)
    }
}