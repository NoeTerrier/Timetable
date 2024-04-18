package com.example.timetable.apiObjects

import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

data class Journey(
    val stop: Stop,
    val name: String,
    val category: String,
    val categoryCode: Int,
    val number: String?, //sometimes null (why ?)
    val operator: String,
    var to: String,
    val passList: List<Stop>,
    val capacity1st: Int?,
    val capacity2nd: Int?
) {

    fun departureDate(): ZonedDateTime {
        return stop.departure.toInstant().atZone(ZoneId.of("CET"))
    }

    fun untilDeparture(): Duration {
        return Duration.between(ZonedDateTime.now(ZoneId.of("CET")), departureDate())
    }
}
