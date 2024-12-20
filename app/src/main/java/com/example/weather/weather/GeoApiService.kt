package com.example.weather.weather

import com.example.weather.HttpGeoCallback
import org.chromium.net.CronetEngine
import org.chromium.net.UrlRequest
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.concurrent.thread

object GeoApiService {
    fun getCoords(cronetEngine: CronetEngine, url: String): List<String> {
        val callback = HttpGeoCallback()
        thread {
            val executor: Executor = Executors.newSingleThreadExecutor()
            val requestBuilder = cronetEngine.newUrlRequestBuilder(
                url,
                callback,
                executor
            )
            val request: UrlRequest = requestBuilder.build()
            request.start()
        }
        while (!callback.isCompleted) {
            Thread.sleep(1000)
        }
        val coords = convertToCoords(callback.geoInstances)
        return coords;
    }

    private fun convertToCoords(geoInstances: Array<GeoInstance>?): List<String> {
        val coords = mutableListOf<String>()
        if (geoInstances != null && geoInstances.size > 0) {
            coords.add(geoInstances[0].lat.toString())
            coords.add(geoInstances[0].lon.toString())
        }
        return coords
    }
}