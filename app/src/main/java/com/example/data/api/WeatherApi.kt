package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "current") val current: CurrentWeather? = null
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    @Json(name = "temperature_2m") val temperature: Double = 0.0,
    @Json(name = "relative_humidity_2m") val humidity: Int = 0,
    @Json(name = "weather_code") val weatherCode: Int = 0,
    @Json(name = "wind_speed_10m") val windSpeed: Double = 0.0
)

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") lat: Double = 23.8103, // Default Dhaka / user location
        @Query("longitude") lon: Double = 90.4125,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m"
    ): WeatherResponse
}
