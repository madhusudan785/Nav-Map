package com.example.myapplication.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.example.myapplication.R
import com.example.myapplication.data.model.PlaceModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PlacesRepository(context: Context) {
    private val placesClient: PlacesClient

    init {
        // Initialize Places with the API key
        Places.initialize(context, context.getString(R.string.maps_api_key))
        placesClient = Places.createClient(context)
    }

    // Helper function to check for location permissions
    private fun hasLocationPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Function to search for nearby ATMs
    suspend fun searchNearbyAtms(context: Context, location: LatLng, radius: Int): List<PlaceModel> {
        if (!hasLocationPermission(context)) {
            throw SecurityException("Location permission not granted")
        }

        return suspendCancellableCoroutine { continuation ->
            val fields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
                Place.Field.RATING,
                Place.Field.BUSINESS_STATUS,
                Place.Field.TYPES
            )

            val request = FindCurrentPlaceRequest.newInstance(fields)

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            placesClient.findCurrentPlace(request)
                .addOnSuccessListener { response ->
                    val atmPlaces = response.placeLikelihoods
                        .filter { it.place.types?.contains(Place.Type.ATM) == true }
                        .map { likelihood ->
                            val place = likelihood.place
                            PlaceModel(
                                id = place.id ?: "",
                                name = place.name ?: "",
                                latitude = place.latLng?.latitude ?: 0.0,
                                longitude = place.latLng?.longitude ?: 0.0,
                                address = place.address,
                                rating = place.rating,
                                isOpen = place.isOpen,
                                placeType = "ATM"
                            )
                        }
                    continuation.resume(atmPlaces)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    // Function to search for places based on a query
    suspend fun searchPlaces(context: Context, query: String): List<PlaceModel> {
        if (!hasLocationPermission(context)) {
            throw SecurityException("Location permission not granted")
        }

        return suspendCancellableCoroutine { continuation ->
            val fields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
                Place.Field.TYPES
            )
            val request = FindCurrentPlaceRequest.newInstance(fields)

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            placesClient.findCurrentPlace(request)
                .addOnSuccessListener { response ->
                    val places = response.placeLikelihoods
                        .filter { likelihood ->
                            likelihood.place.name?.contains(query, ignoreCase = true) == true
                        }
                        .map { likelihood ->
                            val place = likelihood.place
                            PlaceModel(
                                id = place.id ?: "",
                                name = place.name ?: "",
                                latitude = place.latLng?.latitude ?: 0.0,
                                longitude = place.latLng?.longitude ?: 0.0,
                                address = place.address,
                                rating = place.rating,
                                isOpen = place.isOpen,
                                placeType = (place.types?.firstOrNull() ?: "").toString()
                            )
                        }
                    continuation.resume(places)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }
}
