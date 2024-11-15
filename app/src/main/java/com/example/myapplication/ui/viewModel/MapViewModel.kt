package com.example.myapplication.ui.viewModel
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.LocationModel
import com.example.myapplication.data.model.PlaceModel
import com.example.myapplication.data.repository.LocationRepository
import com.example.myapplication.data.repository.PlacesRepository
import com.example.myapplication.utils.LocationUtils
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LocationRepository = LocationRepository(application)
    private val placesRepository = PlacesRepository(application)
    val currentLocation: LiveData<LocationModel?> = repository.currentLocation

    private val _locationPermissionGranted = MutableLiveData<Boolean>()
    val locationPermissionGranted: LiveData<Boolean> = _locationPermissionGranted

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _places = MutableLiveData<List<PlaceModel>>()
    val places: LiveData<List<PlaceModel>> = _places

    private val _searchResults = MutableLiveData<List<PlaceModel>>()
    val searchResults: LiveData<List<PlaceModel>> = _searchResults



    fun checkLocationPermission() {
        _locationPermissionGranted.value =
            LocationUtils.checkLocationPermission(getApplication())
    }

    fun updateCurrentLocation() {
        viewModelScope.launch {
            try {
                repository.getCurrentLocation()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get location: ${e.message}"
            }
        }
    }
    fun searchNearbyAtms(currentLocation: LatLng?) {
        viewModelScope.launch {
            try {
                if (currentLocation != null) {
                    val atms = placesRepository.searchNearbyAtms(
                        getApplication(),
                        currentLocation,
                        5000 // 5km radius
                    )
                    _places.value = atms
                } else {
                    _errorMessage.value = "Current location is null."
                }
            } catch (e: SecurityException) {
                _errorMessage.value = "Location permission not granted."
                Toast.makeText(getApplication(), "Please enable location permissions", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to find ATMs: ${e.message}"
                Log.e("ATM_FINDER", "Error finding ATMs", e)
            }
        }
    }
    fun searchPlaces(query: String) {
        viewModelScope.launch {
            try {
                val results = placesRepository.searchPlaces(getApplication(), query)
                _searchResults.value = results
            } catch (e: SecurityException) {
                _errorMessage.value = "Location permission not granted."
                Toast.makeText(getApplication(), "Please enable location permissions", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                _errorMessage.value = "Search failed: ${e.message}"
            }
        }
    }


}
