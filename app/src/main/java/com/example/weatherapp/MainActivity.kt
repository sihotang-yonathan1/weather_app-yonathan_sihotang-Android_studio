package com.example.weatherapp

import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.cardview.widget.CardView
import coil.load
import coil.size.Dimension
import coil.size.Precision
import coil.size.Scale
import com.example.weatherapp.network.Coordinate
import com.example.weatherapp.network.CurrentWeatherApiResponse
import com.example.weatherapp.network.CurrentWeatherApiService
import com.example.weatherapp.network.GeoCodingApiService
import com.example.weatherapp.network.GeoCodingDirectResponse
import com.google.android.material.textfield.TextInputEditText
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class MainActivity : AppCompatActivity() {
    var locationName: String = "Manado"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val weatherCardView: CardView = findViewById(R.id.weather_card_view)
        val cityNameLabel: TextView = findViewById(R.id.city_name_label)
        val tempNumberLabel: TextView = findViewById(R.id.temp_label)
        val weatherIconContainer: ImageView = findViewById(R.id.weather_icon_container)
        val weatherNameLabelView: TextView = findViewById(R.id.weather_name_label)
        val weatherDescriptionLabel: TextView = findViewById(R.id.weather_description_container)

        var stringBuffer: String = ""
        fun updateState(locationName: String = "Manado"){
            Log.d("updateState", "updateState: $stringBuffer")
            runBlocking {
                val locationDefered: Deferred<List<GeoCodingDirectResponse>> = async {
                    getCItyData(locationName)
                }
                val locationDeferedValue = locationDefered.await()
                if (locationDeferedValue.isNotEmpty()){
                    weatherCardView.visibility = View.VISIBLE
                    weatherIconContainer.visibility = View.VISIBLE

                    Log.d("async-locationDefferedValue", "onCreate: $locationDeferedValue")
                    val currentWeatherDefered: Deferred<CurrentWeatherApiResponse> = async {
                        getCurrentWeatherData(
                            latitude = locationDeferedValue[0].lat,
                            longitude = locationDeferedValue[0].lon
                        )
                    }
                    Log.d("updateState", "updateState: location name: $locationName")
                    val currentWeatherDeferredValue = currentWeatherDefered.await()
                    Log.d("weather icon", "onCreate: ${currentWeatherDeferredValue.weather?.get(0)?.icon}")

                    val weatherIconUrl =
                        "https://openweathermap.org/img/wn/${currentWeatherDeferredValue.weather[0].icon}@2x.png"
                    weatherIconContainer.load(weatherIconUrl) {
                        scale(Scale.FILL)
                        size(Dimension(280), Dimension(500))
                    }

                    runOnUiThread {
                        cityNameLabel.text = locationDeferedValue[0].name
                        tempNumberLabel.text = String.format(
                            "%s \u2103", currentWeatherDeferredValue.main.temp.toString()
                        )

                        Log.d(
                            "weatherNameLabelView",
                            "updateState: ${currentWeatherDeferredValue.weather[0]?.main}"
                        )
                        //weatherNameLabelView.text = currentWeatherDeferredValue.weather[0].main.toString()
                        Log.d(
                            "weatherNameLabelView",
                            "updateState: before ${weatherNameLabelView.text}"
                        )
                        weatherNameLabelView.text =
                            currentWeatherDeferredValue.weather[0]?.main?.toString()
                        weatherDescriptionLabel.text =
                            currentWeatherDeferredValue.weather[0]?.description.toString()
                        Log.d(
                            "weatherNameLabelView",
                            "updateState: after ${weatherNameLabelView.text}"
                        )
                    }
                }
                else {
                    if (locationName != ","){
                        cityNameLabel.text = null
                        weatherNameLabelView.text = "Error, no data found"
                        weatherIconContainer.visibility = View.INVISIBLE
                        weatherDescriptionLabel.text = null
                        tempNumberLabel.text = null
                    }
                }
            }
        }

        val locationSearchView: TextInputEditText = findViewById(R.id.locationSearchView)
        locationSearchView.addTextChangedListener (
            object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    Log.d("locationSearchView", "afterTextChanged: $s")
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // locationName = s.toString()
                    // updateState(locationName)
                    stringBuffer = s.toString()

                }
            }
        )
        locationSearchView.setOnEditorActionListener(
            object: TextView.OnEditorActionListener {
                override fun onEditorAction(
                    v: TextView?,
                    actionId: Int,
                    event: KeyEvent?
                ): Boolean {
                    updateState(stringBuffer)
                    return true
                }
            }
        )
        updateState(locationName = locationName)
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