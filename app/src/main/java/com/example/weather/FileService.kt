package com.example.weather

import android.content.Context
import android.content.SharedPreferences

object FileService {
    private var map = mutableMapOf<String, SharedPreferences>()
    fun getFile(context: Context, filename: String): SharedPreferences? {
        if (map[filename] == null) {
            map[filename] = context.getSharedPreferences(filename, Context.MODE_PRIVATE)
        }
        return map[filename]
    }
}