package com.example.weatherapp.network

import android.util.Log
import com.squareup.moshi.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query

private const val CURRENT_WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
private const val CITY_SEARCH_BASE_URL = "https://api.openweathermap.org/geo/1.0/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(CITY_SEARCH_BASE_URL)
    .build()

data class GeoCodingDirectResponse (
    val name: String,
    @Json(name = "local_names") val localNames: Map<String, String>,
    val lat: Double,
    val lon: Double,
    val country: String
)

interface GeoCodingApiService {
    @GET("direct")
    suspend fun getLocationInfo(
        @Query("q") locationName: String = "Manado",
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String = "d0803559f03dafe4ee9b2515f4dc8c21"
    ): List<GeoCodingDirectResponse>
}

data class MainWeatherInfo (
    val temp: Double,
    @Json(name = "feels_like") val feelsLike: Double,
    @Json(name = "temp_min") val tempMin: Double? = null,
    @Json(name = "temp_max") val tempMax: Double? = null,
    val pressure: Int,
    val humidity: Int,
    @Json(name = "sea_level") val seaLevel: Int? = null,
    @Json(name = "grnd_level")val groundLevel: Int? = null,
)

data class WindInfo (
    val speed: Double,
    val deg: Int,
    val gust: Double? = null
)

data class SystemInternalInfo (
    @Json(name = "type") val systemType: Int? = null,
    val id: Int? = null,
    val country: String,
    val sunrise: Int? = null,
    val sunset: Int? = null
)

data class Coordinate (
    val lat: Double, // latitude
    val lon: Double // longitude
)

data class CurrentWeatherApiResponse(
    val coord: Coordinate,
    val weather: List<WeatherObject>,
    val base: String,
    val main: MainWeatherInfo,
    val visibility: Int,
    val wind: WindInfo,
    val rain: Map<String, Double>? = null,
    val clouds: Map<String, Double>? = null,
    val dt: Int,
    @Json(name = "sys") val sys: SystemInternalInfo? = null,
    val timezone: Int,
    val id: Int,
    val name: String,
    val cod: Int
)
interface  CurrentWeatherApiService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = "d0803559f03dafe4ee9b2515f4dc8c21",
        @Query("lang") language: String = "id"
    ): CurrentWeatherApiResponse
}

data class WeatherObject (
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)



fun getCItyData(){
    val retrofit_obj = Retrofit.Builder()
        .baseUrl(CITY_SEARCH_BASE_URL)
        .build()
}