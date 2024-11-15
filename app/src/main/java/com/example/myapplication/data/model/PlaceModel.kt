package com.example.myapplication.data.model

data class PlaceModel(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val rating: Double?,
    val isOpen: Boolean?,
    val placeType: String
)