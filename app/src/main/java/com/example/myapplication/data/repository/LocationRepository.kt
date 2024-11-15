package com.example.myapplication.data.repository
import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.data.model.LocationModel
import com.example.myapplication.utils.LocationUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationRepository(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableLiveData<LocationModel?>()
    val currentLocation: LiveData<LocationModel?> = _currentLocation

    suspend fun getCurrentLocation() {
        if (LocationUtils.checkLocationPermission(context)) {
            try {
                withContext(Dispatchers.IO) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        location?.let {
                            _currentLocation.postValue(
                                LocationModel(
                                    latitude = it.latitude,
                                    longitude = it.longitude
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _currentLocation.postValue(null)
            }
        }
    }
}
