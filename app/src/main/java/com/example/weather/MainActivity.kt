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
    }

    private fun showDeleteWeatherLocationDialog() {
        val dialogBinding = DialogDeleteWeatherLocationBinding.inflate(layoutInflater)
        val bsd = BottomSheetDialog(this)
        bsd.setContentView(dialogBinding.root)
        dialogBinding.buttonDelete.setOnClickListener {
            removeWeatherLocation("tampa")
            bsd.dismiss()
            adapter.notifyDataSetChanged()
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
            val coords = dialogBinding.editTextCoordinates.text.toString().split(",")
            val lat = coords[0].trim()
            val lon = coords[1].trim()
            addWeather1Fragment(lat, lon, dialogBinding.editTextTabText.text.toString().trim())
            saveWeatherLocation(lat, lon, dialogBinding.editTextTabText.text.toString().trim())
            bsd.dismiss()
        }
        dialogBinding.buttonCancel.setOnClickListener {
            bsd.dismiss()
        }
        bsd.show()
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
//        frags.put(
//            frags.size, createWeather1Fragment(
//                getString(R.string.waconia_latitude),
//                getString(R.string.waconia_longitude), "Waconia"
//            )
//        )
//        frags.put(
//            frags.size, createWeather1Fragment(
//                getString(R.string.ridges_latitude),
//                getString(R.string.ridges_longitude), "Ridges"
//            )
//        )
//        frags.put(
//            frags.size, createWeather1Fragment(
//                getString(R.string.tampa_fl_latitude),
//                getString(R.string.tampa_fl_longitude), "Tampa"
//            )
//        )
//        frags.put(
//            frags.size, createWeather1Fragment(
//                getString(R.string.hss_latitude),
//                getString(R.string.hss_longitude), "HSS"
//            )
//        )
//        frags.put(
//            frags.size, createWeather1Fragment(
//                getString(R.string.deer_shack_latitude),
//                getString(R.string.deer_shack_longitude), "Deer Shack"
//            )
//        )
//        frags.put(frags.size, createWeather1Fragment(getString(R.string.riviera_fl_latitude),
//            getString(R.string.riviera_fl_longitude), "Jensen Beach"))
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