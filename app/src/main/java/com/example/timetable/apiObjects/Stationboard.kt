package com.example.timetable.apiObjects

data class StationBoard(
    val station: Location,
    val stationboard: List<Journey>
)
