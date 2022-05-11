package com.example.prmlfjis.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL =
    "http://oskardolenc.eu:591/"
private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface NominatimApiService {
    @GET("search")
    suspend fun getAreaOutline(@Query(value="country", encoded=true) country : String ="Slovenija",
                        @Query(value="city", encoded = true) city: String = "",
                        @Query(value="query", encoded = true) query: String = "",
                        @Query(value="polygon_text", encoded=true) polygon_text: Int = 1,
                        @Query(value="format", encoded=true) format : String = "json"): String
}

object NominatimApi {
    val retrofitService : NominatimApiService by lazy {
        retrofit.create(NominatimApiService::class.java)
    }
}