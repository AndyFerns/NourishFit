package com.example.nourishfit.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.util.GeoPoint

class Converters {
    private val gson = Gson()

    // Converts a list of GeoPoints into a JSON string for the database
    @TypeConverter
    fun fromGeoPointList(route: List<GeoPoint>): String {
        return gson.toJson(route)
    }

    // Converts a JSON string from the database back into a list of GeoPoints
    @TypeConverter
    fun toGeoPointList(routeString: String): List<GeoPoint> {
        val listType = object : TypeToken<List<GeoPoint>>() {}.type
        return gson.fromJson(routeString, listType)
    }
}
