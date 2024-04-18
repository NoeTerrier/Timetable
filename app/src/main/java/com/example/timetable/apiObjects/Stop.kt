package com.example.timetable.apiObjects

import java.util.Date

data class Stop(
    val station: Location,
    val arrival: Date,
    val departure: Date,
    val delay: Int,
    val platform: String,
    val prognosis: Prognosis
)
