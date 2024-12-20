package com.example.weather

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.databinding.DialogAddWeatherLocationBinding
import com.example.weather.databinding.DialogDeleteWeatherLocationBinding
import com.example.weather.weather.GeoApiService
import com.example.weather.weather.WeatherCache
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import org.chromium.net.CronetEngine
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private val cronetEngine: CronetEngine by lazy {
        CronetEngine.Builder(this).build()
    }
    val frags: LinkedHashMap<Int, Weather1Fragment> by lazy {
        initializeFragments()
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainActivity.PagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        adapter = PagerAdapter(this)
        binding.pager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = frags.get(position)?.tabText
        }.attach()
        binding.textView.setOnClickListener {
            showDeleteWeatherLocationDialog()
        }
        binding.buttonAdd.setOnClickListener{
            showAddWeatherLocationDialog()
        }
    }

    private fun showDeleteWeatherLocationDialog() {
        val dialogBinding = DialogDeleteWeatherLocationBinding.inflate(layoutInflater)
        val bsd = BottomSheetDialog(this)
        bsd.setContentView(dialogBinding.root)
        dialogBinding.buttonDelete.setOnClickListener {
            val selectedTabPosition = binding.tabLayout.selectedTabPosition
            removeWeatherLocation(frags.get(selectedTabPosition)?.tabText.toString())
            binding.tabLayout.removeTab(binding.tabLayout.getTabAt(selectedTabPosition)!!)
            bsd.dismiss()
        }
        dialogBinding.buttonDeleteCancel.setOnClickListener {
            bsd.dismiss()
        }
        bsd.show()
    }

    private fun removeWeatherLocation(weatherLocationKey: String) {
        val editor = getWeatherLocationFile().edit()
        editor.remove(weatherLocationKey)
        editor.apply()
    }

    private fun showAddWeatherLocationDialog() {
        val dialogBinding = DialogAddWeatherLocationBinding.inflate(layoutInflater)
        val bsd = BottomSheetDialog(this)
        bsd.setContentView(dialogBinding.root)
        dialogBinding.buttonSave.setOnClickListener {
            val coordinateEntry = dialogBinding.editTextCoordinates.text.toString()
            val coords = coordinateEntry.split(",")
            var lat : String? = null
            var lon : String? = null
            if (coords.size == 2) {
                lat = coords[0].trim()
                lon = coords[1].trim()
            } else if (coords.size == 1 && coords[0].toIntOrNull() != null){
                val zipCoords : List<String> = GeoApiService.getCoords(cronetEngine, getCoordsByZipUrl(coords[0].toString().trim()))
                lat = zipCoords[0]
                lon = zipCoords[1]
            }

            if (lat?.toDoubleOrNull() == null || lon?.toDoubleOrNull() == null) {
                // send coordinateEntry in entirety to geo api
                // if response has entry pull out lat and lon from it
                val zipCoords = GeoApiService.getCoords(cronetEngine, getCoordsByQueryUrl(coordinateEntry))
                lat = zipCoords[0]
                lon = zipCoords[1]
            }
            if (lat != null && lon != null) {
                addWeather1Fragment(lat, lon, dialogBinding.editTextTabText.text.toString().trim())
                saveWeatherLocation(lat, lon, dialogBinding.editTextTabText.text.toString().trim())
            }
            bsd.dismiss()
        }
        dialogBinding.buttonCancel.setOnClickListener {
            bsd.dismiss()
        }
        bsd.show()
    }

    private fun getCoordsByZipUrl(zip: String) : String {
        val url = "${getString(R.string.geo_zip_query)}$zip&appid=${getString(R.string.weather_api_key)}"
        return url
    }

    private fun getCoordsByQueryUrl(query: String) : String {
        val url = "${getString(R.string.geo_general_query)}$query&appid=${getString(R.string.weather_api_key)}"
        return url
    }

    inner class PagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun createFragment(position: Int): Fragment {
            return frags.get(position) as Fragment
        }

        override fun getItemCount(): Int = frags.size
    }

    private fun initializeFragments(): LinkedHashMap<Int, Weather1Fragment> {
        val frags: LinkedHashMap<Int, Weather1Fragment> = LinkedHashMap()
        val map = getWeatherLocationMap()
        for (mapEntry in map.entries) {
            val tabText = mapEntry.key.trim()
            val fragmentDefTokens = mapEntry.value.split(",")
            val lat = fragmentDefTokens[0].trim()
            val lon = fragmentDefTokens[1].trim()
            frags.put(
                frags.size, createWeather1Fragment(lat, lon, tabText)
            )
        }
        return frags
    }

    private fun getWeatherLocationMap() : Map<String, String> {
        val weatherLocationFile = getWeatherLocationFile()
        return weatherLocationFile.all as Map<String, String>
    }

    private fun getWeatherLocationFile() : SharedPreferences {
        return FileService.getFile(this, "weather-locations")!!
    }

    private fun saveWeatherLocation(
        lat: String,
        lon: String,
        tabText: String
    ) {
        val editor = getWeatherLocationFile().edit()!!
        editor.putString(tabText, "$lat, $lon, $tabText")
        editor.apply()
    }
    private fun addWeather1Fragment(
        lat: String,
        lon: String,
        tabText: String
    ): Weather1Fragment {
        val fragment = createWeather1Fragment(lat, lon, tabText)
        val position = frags.size
        frags.put(position, fragment)
        adapter.createFragment(position)
        adapter.notifyDataSetChanged()
        return fragment
    }

    private fun createWeather1Fragment(
        lat: String,
        lon: String,
        tabText: String
    ): Weather1Fragment {
        return Weather1Fragment(createWeatherCache(lat, lon), binding.textView, tabText)
    }

    private fun createWeatherCache(lat: String, lon: String): WeatherCache {
        return WeatherCache(
            0,
            cronetEngine,
            getWeatherApiUrl(
                lat,
                lon
            )
        )
    }

    private fun getWeatherApiUrl(lat: String, lon: String): String {
        val aUrl =
            "${getString(R.string.weather_api_url)}?lat=$lat&lon=$lon&appid=${getString(R.string.weather_api_key)}&units=imperial"
        return aUrl
    }
}