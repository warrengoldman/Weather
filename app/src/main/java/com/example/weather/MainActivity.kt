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
                if (zipCoords.size > 1) {
                    lat = zipCoords[0]
                    lon = zipCoords[1]
                }
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
        var processedQuery = query
        val queryTokens = query.split(",")
        if (queryTokens.size > 1) {
            var longState = getStateFromAbbr(queryTokens[1].trim())
            if (longState == null) {
                longState = queryTokens[1]
            }
            var rem = getRemainderAfterElementTwo(queryTokens)
            var cntry = ""
            if (queryTokens.size < 3) {
                cntry = ", US"
            }
            processedQuery = "${queryTokens[0]}, $longState$cntry$rem"
        }
        val url = "${getString(R.string.geo_general_query)}$processedQuery&appid=${getString(R.string.weather_api_key)}"
        return url
    }
    private val stateMap = mutableMapOf<String, String>(
        "AL" to "Alabama",
        "AK" to "Alaska",
        "AS" to "American Samoa",
        "AZ" to "Arizona",
        "AR" to "Arkansas",
        "CA" to "California",
        "CO" to "Colorado",
        "CT" to "Connecticut",
        "DE" to "Delaware",
        "DC" to "District of Columbia",
        "FL" to "Florida",
        "GA" to "Georgia",
        "GU" to "Guam",
        "HI" to "Hawaii",
        "ID" to "Idaho",
        "IL" to "Illinois",
        "IN" to "Indiana",
        "IA" to "Iowa",
        "KS" to "Kansas",
        "KY" to "Kentucky",
        "LA" to "Louisiana",
        "ME" to "Maine",
        "MH" to "Marshall Islands",
        "MD" to "Maryland",
        "MA" to "Massachusetts",
        "MI" to "Michigan",
        "FM" to "Micronesia",
        "MN" to "Minnesota",
        "MS" to "Mississippi",
        "MO" to "Missouri",
        "MT" to "Montana",
        "NE" to "Nebraska",
        "NV" to "Nevada",
        "NH" to "New Hampshire",
        "NJ" to "New Jersey",
        "NM" to "New Mexico",
        "NY" to "New York",
        "NC" to "North Carolina",
        "ND" to "North Dakota",
        "MP" to "Northern Marianas",
        "OH" to "Ohio",
        "OK" to "Oklahoma",
        "OR" to "Oregon",
        "PW" to "Palau",
        "PA" to "Pennsylvania",
        "PR" to "Puerto Rico",
        "RI" to "Rhode Island",
        "SC" to "South Carolina",
        "SD" to "South Dakota",
        "TN" to "Tennessee",
        "TX" to "Texas",
        "UT" to "Utah",
        "VT" to "Vermont",
        "VI" to "Virgin Islands",
        "VA" to "Virginia",
        "WA" to "Washington",
        "WV" to "West Virginia",
        "WI" to "Wisconsin",
        "WY" to "Wyoming"
    )

    private fun getStateFromAbbr(state: String): String? {
        val stateUpper = state.uppercase()
        val longState = stateMap.get(stateUpper)
        return longState
    }

    private fun getRemainderAfterElementTwo(strs: List<String>) : String {
        var result : String = ""
        var cnt = 0;
        for (str in strs) {
            if (cnt > 1) {
                result += ", $str"
            }
            cnt++
        }
        return result
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