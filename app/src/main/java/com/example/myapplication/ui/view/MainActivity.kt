package com.example.myapplication.ui.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import com.example.myapplication.R
import com.example.myapplication.data.model.PlaceModel
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.viewModel.MapViewModel
import com.example.myapplication.utils.LocationUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mMap: GoogleMap
    private lateinit var searchView: SearchView
    private val viewModel: MapViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMap()
        setupSearchView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupSearchView() {
        searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.searchPlaces(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }


    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        if (mapFragment != null) {
            mapFragment.getMapAsync(this)
        } else {
            Log.e("MainActivity", "Map fragment is null")
        }
    }


    private fun setupObservers() {
        viewModel.currentLocation.observe(this) { location ->
            location?.let {
                updateMapLocation(LatLng(it.latitude, it.longitude))
            }
        }

        viewModel.places.observe(this) { places ->
            updatePlaceMarkers(places)
        }

        viewModel.searchResults.observe(this) { results ->
            updatePlaceMarkers(results)
            if (results.isNotEmpty()) {
                val position = LatLng(results[0].latitude, results[0].longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
            }
        }

        viewModel.locationPermissionGranted.observe(this) { isGranted ->
            if (isGranted) {
                enableMyLocation()
            } else {
                LocationUtils.requestLocationPermission(this)
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }



    private fun updatePlaceMarkers(places: List<PlaceModel>) {
        mMap.clear()
        places.forEach { place ->
            val position = LatLng(place.latitude, place.longitude)
            val markerOptions = MarkerOptions()
                .position(position)
                .title(place.name)
                .snippet(place.address)

            when (place.placeType) {
                "ATM" -> markerOptions.icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
                else -> markerOptions.icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }

            mMap.addMarker(markerOptions)
        }
    }

    private fun setupClickListeners() {
        binding.fabLocate.setOnClickListener {
            viewModel.updateCurrentLocation()
        }

        binding.fabAtm.setOnClickListener {
            val currentLocation = viewModel.currentLocation.value
            val latLng = currentLocation?.let { it1 -> LatLng(it1.latitude, currentLocation.longitude) }
            if (currentLocation != null) {
                viewModel.searchNearbyAtms(latLng)  // Pass currentLocation to ViewModel function
            } else {
                Toast.makeText(this, "Current location is unavailable", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //this is for removing extra location mark on the map
        mMap.uiSettings.isMyLocationButtonEnabled = false
        viewModel.checkLocationPermission()
    }

    private fun enableMyLocation() {
        if (LocationUtils.checkLocationPermission(this)) {
            mMap.isMyLocationEnabled = true
            viewModel.updateCurrentLocation()
        }
    }

    private fun updateMapLocation(latLng: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(latLng).title("Current Location"))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        LocationUtils.handlePermissionResult(
            this,
            requestCode,
            grantResults,
            { enableMyLocation() },
            { Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show() }
        )
    }
}