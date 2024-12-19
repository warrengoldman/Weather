package com.example.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.weather.databinding.FragmentWeather1Binding
import com.example.weather.weather.WeatherCache
import kotlin.concurrent.thread

class Weather1Fragment(val weatherCache: WeatherCache, val textView: TextView) : Fragment() {
    private lateinit var binding: FragmentWeather1Binding
    private val weather1Adapter = Weather1Adapter()
    private var weatherLocationText: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeather1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        textView.text = getWeatherLocationText()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = weather1Adapter
        fetchAllTasks()
    }

    fun fetchAllTasks() {
        thread {
            val weatherEntries = weatherCache.getWeatherEntries()
            requireActivity().runOnUiThread {
                weather1Adapter.setEntries(weatherEntries!!)
                var cityName = ""
                if (weatherEntries.isNotEmpty()) {
                    cityName = weatherEntries[0].city
                }
                textView.text = getWeatherLocationText()
            }
        }
    }
    fun getWeatherLocationText() : String {
        if (weatherLocationText == null) {
            var cityName = ""
            val weatherEntries = weatherCache.getWeatherEntries()
            if (weatherEntries != null && weatherEntries.isNotEmpty()) {
                    cityName = weatherEntries[0].city
            }
            weatherLocationText = getString(R.string.main_page_intro_msg, cityName)
        }
        return weatherLocationText!!
    }
}