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
import org.chromium.net.CronetEngine


class MainActivity : AppCompatActivity() {
    private val cronetEngine: CronetEngine by lazy {
        CronetEngine.Builder(this).build()
    }
    private val ridgesWeatherCache: WeatherCache by lazy {
        WeatherCache(0, cronetEngine, getWeatherApiUrl(getString(R.string.ridges_latitude), getString(R.string.ridges_longitude)))
    }
    private val waconiaWeatherCache: WeatherCache by lazy {
        WeatherCache(0, cronetEngine, getWeatherApiUrl(getString(R.string.waconia_latitude), getString(R.string.waconia_longitude)))
    }
    private val rivieraWeatherCache: WeatherCache by lazy {
        WeatherCache(0, cronetEngine, getWeatherApiUrl(getString(R.string.riviera_fl_latitude), getString(R.string.riviera_fl_longitude)))
    }
    private val tampaWeatherCache: WeatherCache by lazy {
        WeatherCache(0, cronetEngine, getWeatherApiUrl(getString(R.string.tampa_fl_latitude), getString(R.string.tampa_fl_longitude)))
    }
    private val hssWeatherCache: WeatherCache by lazy {
        WeatherCache(0, cronetEngine, getWeatherApiUrl(getString(R.string.hss_latitude), getString(R.string.hss_longitude)))
    }
    private val deerShackWeatherCache: WeatherCache by lazy {
        WeatherCache(0, cronetEngine, getWeatherApiUrl(getString(R.string.deer_shack_latitude), getString(R.string.deer_shack_longitude)))
    }
    private lateinit var binding: ActivityMainBinding
    fun getWeatherApiUrl(lat: String, lon: String): String {
        val aUrl =
            "${getString(R.string.weather_api_url)}?lat=$lat&lon=$lon&appid=${getString(R.string.weather_api_key)}&units=imperial"
        return aUrl
    }

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
        binding.pager.adapter = PagerAdapter(this)
    }

    inner class PagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> Weather1Fragment(waconiaWeatherCache, binding.textView)
                1 -> Weather1Fragment(ridgesWeatherCache, binding.textView)
                2 -> Weather1Fragment(tampaWeatherCache, binding.textView)
                3 -> Weather1Fragment(hssWeatherCache, binding.textView)
                4 -> Weather1Fragment(deerShackWeatherCache, binding.textView)
                else -> Weather1Fragment(rivieraWeatherCache, binding.textView)
            }
        }

        override fun getItemCount(): Int = 6
    }
}