package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

object LocationHelper {
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): LocationData =
        suspendCancellableCoroutine { continuation ->
            val client = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        val address = getReadableAddress(context, loc.latitude, loc.longitude)
                        continuation.resume(LocationData(loc.latitude, loc.longitude, address))
                    } else {
                        client.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                            if (lastLoc != null) {
                                val address = getReadableAddress(context, lastLoc.latitude, lastLoc.longitude)
                                continuation.resume(LocationData(lastLoc.latitude, lastLoc.longitude, address))
                            } else {
                                continuation.resume(LocationData(12.9716, 77.5946, "Ward 42, Indiranagar, Bengaluru"))
                            }
                        }.addOnFailureListener {
                            continuation.resume(LocationData(12.9716, 77.5946, "Ward 42, Indiranagar, Bengaluru"))
                        }
                    }
                }
                .addOnFailureListener {
                    continuation.resume(LocationData(12.9716, 77.5946, "Ward 42, Indiranagar, Bengaluru"))
                }

            continuation.invokeOnCancellation {
                cts.cancel()
            }
        }

    fun getReadableAddress(context: Context, lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val list = geocoder.getFromLocation(lat, lng, 1)
            if (!list.isNullOrEmpty()) {
                val addr = list[0]
                val feature = addr.featureName ?: ""
                val street = addr.thoroughfare ?: addr.subLocality ?: ""
                val locality = addr.locality ?: ""
                val parts = listOfNotNull(
                    feature.ifBlank { null },
                    street.ifBlank { null },
                    locality.ifBlank { null }
                ).distinct()
                if (parts.isNotEmpty()) parts.joinToString(", ") else "Lat: ${String.format("%.4f", lat)}, Lng: ${String.format("%.4f", lng)}"
            } else {
                "Lat: ${String.format("%.4f", lat)}, Lng: ${String.format("%.4f", lng)}"
            }
        } catch (_: Exception) {
            "Lat: ${String.format("%.4f", lat)}, Lng: ${String.format("%.4f", lng)}"
        }
    }
}
