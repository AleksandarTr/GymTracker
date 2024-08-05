package com.example.gym_tracker_app_2

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.BaseAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ChartValues
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarkerValueFormatter
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.views.cartesian.CartesianChartView
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StatsScreen: ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stats_layout)

        val chartView = findViewById<CartesianChartView>(R.id.ExerciseChart)
        val modelProducer = CartesianChartModelProducer()
        chartView.chart?.marker = DefaultCartesianMarker(TextComponent(),
            labelPosition = DefaultCartesianMarker.LabelPosition.AbovePoint)
        chartView.modelProducer = modelProducer

        val exerciseData = HomeScreen.databaseInterface.getExerciseStats(intent.getIntExtra("exerciseTypeID", -1))
        val xToDates = exerciseData.keys.associateBy { it.toEpochDay().toDouble() }
        val xToDateMapKey = ExtraStore.Key<Map<Double, LocalDate>>()
        (chartView.chart?.bottomAxis as BaseAxis).valueFormatter = CartesianValueFormatter { x, chartValues, _ ->
            (chartValues.model.extraStore[xToDateMapKey][x] ?: LocalDate.ofEpochDay(x.toLong()))
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        }

        lifecycleScope.launch {
            modelProducer.runTransaction {
                lineSeries {
                    series(x = xToDates.keys, y = exerciseData.values)
                    extras { it[xToDateMapKey] = xToDates }
                }
            }
        }
    }
}