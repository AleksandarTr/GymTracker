package com.example.gym_tracker_app_2

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.patrykandpatrick.vico.core.cartesian.axis.BaseAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.views.cartesian.CartesianChartView
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StatsScreen: ComponentActivity() {
    class PeriodSelectorListener(private val chartView: CartesianChartView,
                                 private val exerciseID: Int,
                                 private val missingData: TextView) : OnItemSelectedListener {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val currentDate = when(position) {
                0 -> LocalDate.now().minusMonths(1)
                1 -> LocalDate.now().minusMonths(3)
                2 -> LocalDate.now().minusMonths(6)
                3 -> LocalDate.now().minusYears(1)
                4 -> LocalDate.MIN
                else -> LocalDate.MIN
            }

            val dateFormat = if (position in 0..3) DateTimeFormatter.ofPattern("d MMM")
                else DateTimeFormatter.ofPattern("d.M.yyyy")

            val exerciseData = HomeScreen.databaseInterface.getExerciseStats(exerciseID, currentDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
            if(exerciseData.isEmpty()) {
                chartView.isVisible = false
                missingData.isVisible = true
                return
            }
            else {
                chartView.isVisible = true
                missingData.isVisible = false
            }

            val xToDates = exerciseData.keys.associateBy { it.toEpochDay().toDouble() }
            val xToDateMapKey = ExtraStore.Key<Map<Double, LocalDate>>()
            (chartView.chart?.bottomAxis as BaseAxis).valueFormatter = CartesianValueFormatter { x, chartValues, _ ->
                (chartValues.model.extraStore[xToDateMapKey][x] ?: LocalDate.ofEpochDay(x.toLong()))
                    .format(dateFormat)
            }

            runBlocking {
                chartView.modelProducer?.runTransaction {
                    lineSeries {
                        series(x = xToDates.keys, y = exerciseData.values)
                        extras { it[xToDateMapKey] = xToDates }
                    }
                }
            }
        }

        override fun onNothingSelected(periodSelector: AdapterView<*>) {
            periodSelector.setSelection(0)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stats_layout)

        val chartView = findViewById<CartesianChartView>(R.id.exerciseChart)
        chartView.chart?.marker = DefaultCartesianMarker(TextComponent(),
            labelPosition = DefaultCartesianMarker.LabelPosition.AbovePoint)
        chartView.modelProducer = CartesianChartModelProducer()

        val missingData = findViewById<TextView>(R.id.missingData)
        val periodSelector = findViewById<Spinner>(R.id.periodSelector)
        periodSelector.onItemSelectedListener = PeriodSelectorListener(chartView,
            (intent.getIntExtra("exerciseTypeID", -1)), missingData)
    }


}