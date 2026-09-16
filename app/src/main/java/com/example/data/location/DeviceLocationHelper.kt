package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.prayer.CityData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume
import kotlin.math.*

data class DeviceLocationResult(
    val cityName: String,
    val countryName: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneOffset: Double,
    val timeZoneId: String
)

object DeviceLocationHelper {

    private const val TAG = "DeviceLocationHelper"

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    fun isLocationServiceEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    suspend fun getDeviceLocation(context: Context): Result<DeviceLocationResult> = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            return@withContext Result.failure(SecurityException("صلاحية الموقع غير ممنوحة للتطبيق"))
        }

        var location: Location? = null

        // Attempt 1: FusedLocationProviderClient (Google Play Services)
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            location = withTimeoutOrNull(5000L) {
                suspendCancellableCoroutine { cont ->
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                        .addOnSuccessListener { loc ->
                            if (cont.isActive) cont.resume(loc)
                        }
                        .addOnFailureListener {
                            if (cont.isActive) cont.resume(null)
                        }
                        .addOnCanceledListener {
                            if (cont.isActive) cont.resume(null)
                        }
                }
            }

            if (location == null) {
                location = suspendCancellableCoroutine { cont ->
                    fusedClient.lastLocation
                        .addOnSuccessListener { loc ->
                            if (cont.isActive) cont.resume(loc)
                        }
                        .addOnFailureListener {
                            if (cont.isActive) cont.resume(null)
                        }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Fused location provider failed, trying LocationManager: ${e.message}")
        }

        // Attempt 2: Standard Android LocationManager fallback
        if (location == null) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                if (lm != null) {
                    val gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val netLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    val passiveLoc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

                    location = when {
                        gpsLoc != null && netLoc != null -> if (gpsLoc.time > netLoc.time) gpsLoc else netLoc
                        gpsLoc != null -> gpsLoc
                        netLoc != null -> netLoc
                        else -> passiveLoc
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "LocationManager fallback failed: ${e.message}")
            }
        }

        if (location == null) {
            return@withContext Result.failure(Exception("تعذر الحصول على إحداثيات الموقع الحالية. يرجى التأكد من تشغيل GPS"))
        }

        val lat = location.latitude
        val lng = location.longitude

        // Detect TimeZone
        val defaultTz = TimeZone.getDefault()
        val tzOffsetMillis = defaultTz.getOffset(System.currentTimeMillis())
        val tzOffsetHours = round((tzOffsetMillis.toDouble() / 3600000.0) * 100) / 100.0
        val tzId = defaultTz.id

        // Reverse Geocode
        var detectedCity = ""
        var detectedCountry = ""

        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale("ar"))
                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(lat, lng, 1) { list ->
                            cont.resume(list)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lng, 1)
                }

                val address = addresses?.firstOrNull()
                if (address != null) {
                    detectedCity = address.locality
                        ?: address.subAdminArea
                        ?: address.adminArea
                        ?: address.featureName
                        ?: ""
                    detectedCountry = address.countryName ?: ""
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Geocoder error: ${e.message}")
        }

        // If Geocoder did not return a city name, find nearest known city or format coordinates
        if (detectedCity.isBlank()) {
            val nearest = findNearestCity(lat, lng)
            if (nearest != null) {
                detectedCity = "${nearest.nameAr} (موقع تقريبي)"
                if (detectedCountry.isBlank()) detectedCountry = nearest.countryAr
            } else {
                detectedCity = "موقعي الحالي (${String.format(Locale.US, "%.2f, %.2f", lat, lng)})"
                if (detectedCountry.isBlank()) detectedCountry = "تحديد آلي (GPS)"
            }
        }

        Result.success(
            DeviceLocationResult(
                cityName = detectedCity,
                countryName = detectedCountry.ifBlank { "تحديد آلي (GPS)" },
                latitude = lat,
                longitude = lng,
                timeZoneOffset = tzOffsetHours,
                timeZoneId = tzId
            )
        )
    }

    private fun findNearestCity(lat: Double, lng: Double): CityDataCity? {
        var minDistance = Double.MAX_VALUE
        var nearest: CityDataCity? = null

        for (city in CityData.CITIES) {
            val d = distanceKm(lat, lng, city.lat, city.lng)
            if (d < minDistance) {
                minDistance = d
                nearest = CityDataCity(city.nameAr, city.countryAr, city.lat, city.lng)
            }
        }
        return if (minDistance <= 150.0) nearest else null
    }

    private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private data class CityDataCity(
        val nameAr: String,
        val countryAr: String,
        val lat: Double,
        val lng: Double
    )
}
