package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import coil.load
import coil.size.Precision
import coil.size.Scale
import com.example.weatherapp.network.Coordinate
import com.example.weatherapp.network.CurrentWeatherApiResponse
import com.example.weatherapp.network.CurrentWeatherApiService
import com.example.weatherapp.network.GeoCodingApiService
import com.example.weatherapp.network.GeoCodingDirectResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class MainActivity : AppCompatActivity() {
    private var coordinate = Coordinate(1.4900578, 124.8408708) // Manado coordinate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: send suspended return value back to main function
        runBlocking {
            val locationDefered: Deferred<List<GeoCodingDirectResponse>> = async {
                getCItyData("Manado")
            }
            val locationDeferedValue = locationDefered.await()[0]
            Log.d("async-locationDefferedValue", "onCreate: $locationDeferedValue")
            val currentWeatherDefered: Deferred<CurrentWeatherApiResponse> = async {
                getCurrentWeatherData(
                    latitude = locationDeferedValue.lat,
                    longitude = locationDeferedValue.lon
                )
            }

            val currentWeatherDeferredValue = currentWeatherDefered.await()

            val cityNameLabel: TextView = findViewById(R.id.city_name_label)
            val tempNumberLabel: TextView = findViewById(R.id.temp_label)
            val weatherIconContainer: ImageView = findViewById(R.id.weather_icon_container)

            Log.d("weather icon", "onCreate: ${currentWeatherDeferredValue.weather[0].icon}")
            val weatherIconUrl = "https://openweathermap.org/img/wn/${currentWeatherDeferredValue.weather[0].icon}@2x.png"
            weatherIconContainer.load(weatherIconUrl){
                scale(Scale.FILL)
                size(300, 500)
                precision(Precision.EXACT)
            }

            cityNameLabel.text = locationDeferedValue.name
            tempNumberLabel.text = currentWeatherDeferredValue.main.temp.toString()
        }
    }
}

suspend fun getCItyData(locationName: String = "Manado", limit: Int = 1): List<GeoCodingDirectResponse>{
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val retrofit_obj = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/geo/1.0/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service = retrofit_obj.create(GeoCodingApiService::class.java)
    return service.getLocationInfo(
        locationName = locationName,
        limit = limit
    )
}

suspend fun getCurrentWeatherData(latitude: Double, longitude: Double) : CurrentWeatherApiResponse {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val retrofit_obj = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service = retrofit_obj.create(CurrentWeatherApiService::class.java)
    return service.getCurrentWeather(
        latitude = latitude,
        longitude = longitude
    )
}