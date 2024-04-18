package com.example.timetable

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timetable.apiObjects.Journey
import com.example.timetable.apiObjects.StationBoard
import com.example.timetable.ui.theme.TimetableTheme
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimetableTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    DepartureList()
                }
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun DepartureList() {

    val stationBoard = remember<MutableList<Journey>> { mutableStateListOf() }

//     Launch a coroutine for fetching data periodically
    LaunchedEffect(Unit) {
        while (true) {
            cleanDepartures(stationBoard)
            // Fetch data and update the state
            getDeparture(stationBoard, "Renens VD, 14 Avril", "Lausanne-Flon")
            getDeparture(stationBoard, "Renens VD, gare", "Ecublens VD, EPFL")

            // Wait for 10 seconds before the next iteration
            delay(15000)
        }
    }

    val lazyListState = rememberLazyListState()
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        LazyRow(state = lazyListState) {
            items(stationBoard.sortedBy { a -> a.stop.departure.time }) {
                BigBusInfo(it)
            }
        }
    }

    LaunchedEffect(Unit) {
        autoScroll(lazyListState)
    }
}

private tailrec suspend fun autoScroll(lazyListState: LazyListState) {
    lazyListState.scroll(MutatePriority.PreventUserInput) {
        val x = scrollBy(SCROLL_DX)
        if (x == 0f) {
            scrollBy(-5000f)
            delay(3000)
        }
    }
    delay(DELAY_BETWEEN_SCROLL_MS)

    autoScroll(lazyListState)
}

private const val DELAY_BETWEEN_SCROLL_MS = 1L
private const val SCROLL_DX = 1f

fun cleanDepartures(stationBoard: MutableList<Journey>) {
    stationBoard.removeIf {
        it.departureDate().toEpochSecond() - ZonedDateTime.now(ZoneId.of("CET")).toEpochSecond() < 0
    }
}

fun getDeparture(stationBoard: MutableList<Journey>, stationDeparture : String, stationArrival: String) : List<Journey> {
    val baseUrl = "https://transport.opendata.ch/v1/stationboard"
    val queryParams = listOf(
        "station" to stationDeparture,
        "limit" to "4",
        "to" to stationArrival
    )

     Fuel.get(baseUrl, queryParams)
        .response { _, _, result ->
         result.fold(
            success = {
                val sb = Gson().fromJson(String(it), StationBoard::class.java)
                for (journey in sb.stationboard) {
                    if ( stationBoard.find { a -> a.name == journey.name } == null && journey.number != "32" ) {
                        journey.to = stationArrival
                        stationBoard.add(journey)
                    }
                }
            },
            failure = { error ->
                // Handle error
                Log.e("ERROR", "Error: $error")
            }
        )
    }

    return stationBoard.toList()
}

@Composable
fun BigBusInfo(journey: Journey) {
    if (journey.number == null) return
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.Black)
            .padding(5.dp)
            .width(300.dp)
            .wrapContentHeight()) {

        val color = if (journey.untilDeparture().toMinutes() <= 5) Color.Red else Color.White
        val image = if (journey.category == "M") R.drawable.train else R.drawable.bus
//        val tinyFont = FontFamily(
//            Font(R.font.tiny_80, FontWeight.Medium),
//        )

        Row (verticalAlignment = Alignment.CenterVertically){
            Image(
                painter = painterResource(id = image),
                contentDescription = "bus", Modifier.height(60.dp)
            )
            Text(text = journey.number, color = Color.White, fontSize = 70.sp)
        }
        val text = if (journey.untilDeparture().toMinutes() == 0L) "NOW" else "%02d'".format(journey.untilDeparture().toMinutes())
        val fontSize = if (journey.untilDeparture().toMinutes() == 0L) 100.sp else 150.sp
        Text(text = text,
            color = color,
            textAlign = TextAlign.Center,
            fontSize = fontSize,
            modifier = Modifier
                .width(300.dp)
                .height(200.dp)
                .wrapContentHeight(Alignment.CenterVertically))
    }
}
