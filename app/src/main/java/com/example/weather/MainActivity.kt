package com.example.weather

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.weather.WeatherCache
import com.google.android.material.tabs.TabLayoutMediator
import org.chromium.net.CronetEngine


class MainActivity : AppCompatActivity() {
    private val cronetEngine: CronetEngine by lazy {
        CronetEngine.Builder(this).build()
    }
    private lateinit var binding: ActivityMainBinding
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
        val adapter = PagerAdapter(this)
        binding.pager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = adapter.getTabText(position)
        }.attach()
    }

    inner class PagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        val frags: LinkedHashMap<Int, Weather1Fragment> = LinkedHashMap()
        override fun createFragment(position: Int): Fragment {
            return getFrag(position) as Fragment
        }

        override fun getItemCount(): Int = 6

        fun getTabText(position: Int): String? {
            return getFrag(position)?.tabText
        }

        private fun getFrag(position: Int): Weather1Fragment? {
            if (frags.isEmpty()) {
                initializeFragments()
            }
            return frags.get(position)
        }

        private fun initializeFragments() {
            createWeather1Fragment(getString(R.string.waconia_latitude),
                getString(R.string.waconia_longitude), "Waconia")
            createWeather1Fragment(getString(R.string.ridges_latitude),
                getString(R.string.ridges_longitude), "Ridges")
            createWeather1Fragment(getString(R.string.tampa_fl_latitude),
                getString(R.string.tampa_fl_longitude), "Tampa")
            createWeather1Fragment(getString(R.string.hss_latitude),
                getString(R.string.hss_longitude), "HSS")
            createWeather1Fragment(getString(R.string.deer_shack_latitude),
                getString(R.string.deer_shack_longitude), "Deer Shack")
            createWeather1Fragment(getString(R.string.riviera_fl_latitude),
                getString(R.string.riviera_fl_longitude), "Jensen Beach")
        }

        private fun createWeather1Fragment(
            lat: String,
            lon: String,
            tabText: String
        ) {
            frags.put(frags.size, Weather1Fragment(createWeatherCache(lat, lon), binding.textView, tabText))
        }
    }
    private fun createWeatherCache(lat: String, lon: String) : WeatherCache {
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