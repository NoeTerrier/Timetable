package com.example.timetable.apiObjects

data class Location(
    val id: String,
    val type: String,
    val name: String,
    val score: String,
    val coordinates: Coordinates,
    val distance: Float
)