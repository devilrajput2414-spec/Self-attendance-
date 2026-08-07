package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlin.math.*

object GeoFenceUtils {

    /**
     * Haversine formula to calculate distance in meters between two GPS coordinates
     */
    fun calculateDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val earthRadius = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (earthRadius * c).toFloat()
    }

    /**
     * Check if a location is within the defined workplace radius
     */
    fun isInsideGeoFence(
        currentLat: Double, currentLng: Double,
        officeLat: Double, officeLng: Double,
        radiusMeters: Float
    ): Boolean {
        val distance = calculateDistanceMeters(currentLat, currentLng, officeLat, officeLng)
        return distance <= radiusMeters
    }

    fun formatDistance(meters: Float): String {
        return if (meters >= 1000) {
            String.format("%.2f km", meters / 1000f)
        } else {
            String.format("%.0f m", meters)
        }
    }

    /**
     * Runtime Permission Check: Verifies if fine/coarse location permissions are granted
     */
    fun hasLocationPermissions(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    /**
     * Background Location Permission Check (Android 10+)
     */
    fun hasBackgroundLocationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Checks if Location/GPS Services are enabled on device
     */
    fun isLocationProviderEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Returns array of location permissions required for check-in / check-out
     */
    fun getRequiredLocationPermissions(): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        return permissions.toTypedArray()
    }

    /**
     * Fetch current device location via Google Play Services FusedLocationProviderClient
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        if (!hasLocationPermissions(context)) return null
        return try {
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
        } catch (e: Exception) {
            null
        }
    }

    data class GeoFenceCheckResult(
        val isAllowed: Boolean,
        val distanceMeters: Float,
        val message: String
    )

    /**
     * Validates if a check-in/out attempt is eligible based on location permissions,
     * GPS provider state, and geofence boundary distance.
     */
    fun validateCheckInEligibility(
        context: Context,
        currentLat: Double,
        currentLng: Double,
        officeLat: Double,
        officeLng: Double,
        allowedRadiusMeters: Float
    ): GeoFenceCheckResult {
        if (!hasLocationPermissions(context)) {
            return GeoFenceCheckResult(
                isAllowed = false,
                distanceMeters = -1f,
                message = "Location permission required for Check-In/Out"
            )
        }

        if (!isLocationProviderEnabled(context)) {
            return GeoFenceCheckResult(
                isAllowed = false,
                distanceMeters = -1f,
                message = "GPS/Location services are disabled on device"
            )
        }

        val distance = calculateDistanceMeters(currentLat, currentLng, officeLat, officeLng)
        val isInside = distance <= allowedRadiusMeters

        val msg = if (isInside) {
            "Within geofence area (${formatDistance(distance)} from office)"
        } else {
            "Outside geofence area (${formatDistance(distance)} from office, allowed max ${formatDistance(allowedRadiusMeters)})"
        }

        return GeoFenceCheckResult(
            isAllowed = isInside,
            distanceMeters = distance,
            message = msg
        )
    }
}
