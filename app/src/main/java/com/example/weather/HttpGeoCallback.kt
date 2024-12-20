package com.example.weather

import android.util.Log
import com.example.weather.weather.GeoInstance
import kotlinx.serialization.json.Json
import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

private const val TAG = "MyUrlRequestCallback"

class HttpGeoCallback : UrlRequest.Callback() {
    var geoInstances: Array<GeoInstance>? = null
    var isCompleted: Boolean = false
    var receivedData = ByteBuffer.allocateDirect(307200)

    override fun onRedirectReceived(
        request: UrlRequest?,
        info: UrlResponseInfo?,
        newLocationUrl: String?
    ) {
        request?.followRedirect()
    }

    override fun onResponseStarted(
        request: UrlRequest?,
        info: UrlResponseInfo?
    ) {
        request?.read(ByteBuffer.allocateDirect(102400))
    }

    override fun onReadCompleted(
        request: UrlRequest?,
        info: UrlResponseInfo?,
        byteBuffer: ByteBuffer?
    ) {
        byteBuffer!!.flip() // Prepare the received buffer for reading

        // Ensure sufficient capacity in receivedData
        if (receivedData.remaining() < byteBuffer.remaining()) {
            val newBuffer = ByteBuffer.allocate(receivedData.capacity() + byteBuffer.remaining())
            receivedData.flip()
            newBuffer.put(receivedData)
            receivedData = newBuffer
        }

        receivedData.put(byteBuffer) // Append received data
        byteBuffer.clear() // Prepare the received buffer for the next read

        request!!.read(byteBuffer)
    }

    override fun onSucceeded(
        request: UrlRequest?,
        info: UrlResponseInfo?
    ) {
        val str1 = String(receivedData.array(), StandardCharsets.UTF_8).replace("\u0000", "")
        try {
            if (str1.indexOf("[") == 0) {
                val json = Json {
                    ignoreUnknownKeys = true
                }
                geoInstances = json.decodeFromString<Array<GeoInstance>>(str1)
            } else {
                val json = Json {
                    ignoreUnknownKeys = true
                }
                val geoInstance = json.decodeFromString<GeoInstance>(str1)
                geoInstances = arrayOf(geoInstance)
            }
            Log.i(TAG, "onSucceeded method called.")
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
        isCompleted = true
    }

    override fun onFailed(
        request: UrlRequest?,
        info: UrlResponseInfo?,
        error: CronetException?
    ) {
        geoInstances = null
        Log.e(TAG, "onFailed method called, error: $error, urlResponseInfo: $info")
        isCompleted = true
    }
}