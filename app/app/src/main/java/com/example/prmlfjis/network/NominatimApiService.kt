package com.example.prmlfjis.network

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private const val BASE_URL =
    "https://nominatim.oskardolenc.eu/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

data class NominatimJsonData(
    val boundingbox: List<String>,
    @Json(name = "class") val _class: String,
    @Json(name = "display_name") val displayName: String,
    val geotext: String,
    val importance: Double,
    val lat: String,
    val lon: String,
    val licence: String,
    @Json(name = "osm_id") val osmId: Long,
    @Json(name = "osm_type") val osmType: String,
    @Json(name = "place_id") val placeId: Long,
    val type: String
)


interface NominatimApiService {
    @Headers("Accept-Encoding: identity")
    @GET("search")
    suspend fun getAreaOutline(
        @Query(value = "country", encoded = true) country: String = "",
        @Query(value = "city", encoded = true) city: String = "",
        @Query(value = "polygon_text", encoded = true) polygon_text: Int = 1,
        @Query(value = "format", encoded = true) format: String = "json",
        @Query(value = "polygon_threshold", encoded = true) polygonThreshold: Float = 0.001f
    ): List<NominatimJsonData>

    @GET("search")
    suspend fun searchQuery(
        @Query(value = "q", encoded = true) query: String = "",
        @Query(value = "polygon_text", encoded = true) polygon_text: Int = 1,
        @Query(value = "format", encoded = true) format: String = "json",
        @Query(value = "polygon_threshold", encoded = true) polygonThreshold: Float = 0.001f
    ): List<NominatimJsonData>
}

object NominatimApi {
    val retrofitService: NominatimApiService by lazy {
        retrofit.create(NominatimApiService::class.java)
    }
}