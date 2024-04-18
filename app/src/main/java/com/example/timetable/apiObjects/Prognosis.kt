package com.example.timetable.apiObjects

import java.util.Date

data class Prognosis(
    val platform: String,
    val departure: Date,
    val arrival: Date,
    val capacity1st: Int,
    val capacity2nd: Int
)
