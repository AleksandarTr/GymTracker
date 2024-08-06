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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gym_tracker_app_2.ui.main.HistoryDisplayAdapter
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
import kotlin.properties.Delegates

class StatsScreen: ComponentActivity() {
    private var exerciseType = 0
    private lateinit var chartView: CartesianChartView
    private lateinit var missingData: TextView
    private lateinit var periodSelector: Spinner
    private lateinit var workoutList: RecyclerView
    private val workouts = ArrayList<Workout>()

    inner class PeriodSelectorListener : OnItemSelectedListener {
        @SuppressLint("NotifyDataSetChanged")
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val currentDate = when(position) {
                0 -> LocalDate.now().minusMonths(1)
                1 -> LocalDate.now().minusMonths(3)
                2 -> LocalDate.now().minusMonths(6)
                3 -> LocalDate.now().minusYears(1)
                else -> LocalDate.MIN
            }

            val dateFormat = if (position in 0..3) DateTimeFormatter.ofPattern("d MMM")
                else DateTimeFormatter.ofPattern("d.M.yyyy")

            val exerciseData = HomeScreen.databaseInterface.getExerciseStats(exerciseType, currentDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
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

            workouts.clear()
            workouts.addAll(HomeScreen.databaseInterface.getExerciseWorkouts(exerciseType,
                currentDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))))
            workoutList.adapter?.notifyDataSetChanged()

        }

        override fun onNothingSelected(periodSelector: AdapterView<*>) {
            periodSelector.setSelection(0)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exerciseType = intent.getIntExtra("exerciseTypeID", -1)
        setContentView(R.layout.stats_layout)

        chartView = findViewById(R.id.exerciseChart)
        missingData = findViewById(R.id.missingData)
        periodSelector = findViewById(R.id.periodSelector)
        workoutList = findViewById(R.id.workoutList)

        chartView.chart?.marker = DefaultCartesianMarker(TextComponent(),
            labelPosition = DefaultCartesianMarker.LabelPosition.AbovePoint)
        chartView.modelProducer = CartesianChartModelProducer()

        periodSelector.onItemSelectedListener = PeriodSelectorListener()
        workoutList.layoutManager = LinearLayoutManager(this)
        workoutList.adapter = HistoryDisplayAdapter(workouts, applicationContext)
    }


}