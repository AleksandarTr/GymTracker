package com.example.gym_tracker_app_2

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.FOCUSABLE
import android.widget.AdapterView.FOCUSABLE_AUTO
import android.widget.AdapterView.NOT_FOCUSABLE
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.patrykandpatrick.vico.core.cartesian.axis.BaseAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.views.cartesian.CartesianChartView
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StatsScreen: ComponentActivity() {
    private var exerciseType = 0
    private lateinit var chartView: CartesianChartView
    private lateinit var missingData: TextView
    private lateinit var periodSelector: Spinner
    private lateinit var workoutList: RecyclerView
    private lateinit var exerciseTitle: EditText
    private lateinit var statsContainer: ConstraintLayout
    private val workouts = ArrayList<Workout>()

    inner class PeriodSelectorListener : OnItemSelectedListener {
        @SuppressLint("NotifyDataSetChanged")
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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val location = IntArray(2)
        exerciseTitle.getLocationOnScreen(location)
        if((location[0] > ev.rawX || ev.rawX > location[0] + exerciseTitle.width ||
            location[1] > ev.rawY || ev.rawY > location[1] + exerciseTitle.height)
            && ev.action == MotionEvent.ACTION_DOWN) exerciseTitle.clearFocus()
        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exerciseType = intent.getIntExtra("exerciseTypeID", -1)
        setContentView(R.layout.stats_layout)

        chartView = findViewById(R.id.exerciseChart)
        missingData = findViewById(R.id.missingData)
        periodSelector = findViewById(R.id.periodSelector)
        workoutList = findViewById(R.id.workoutList)
        exerciseTitle = findViewById(R.id.exerciseTitle)
        statsContainer = findViewById(R.id.statsContainer)

        exerciseTitle.setText(HomeScreen.databaseInterface.getExerciseName(exerciseType))
        exerciseTitle.isFocusableInTouchMode = false
        exerciseTitle.isCursorVisible = false
        exerciseTitle.isClickable = false
        exerciseTitle.isSelected = false

        val editableBackground = exerciseTitle.background
        var prevName = exerciseTitle.text.toString()
        exerciseTitle.setOnLongClickListener {
            if(!exerciseTitle.hasFocus()) {
                exerciseTitle.text.toString()
                exerciseTitle.background = editableBackground
                exerciseTitle.isFocusableInTouchMode = true
                exerciseTitle.isCursorVisible = true
                exerciseTitle.setSelection(exerciseTitle.text.length)
                exerciseTitle.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(exerciseTitle, InputMethodManager.SHOW_IMPLICIT)
            }
            true
        }
        exerciseTitle.setBackgroundResource(android.R.color.transparent)

        exerciseTitle.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus) {
                exerciseTitle.isFocusableInTouchMode = false
                exerciseTitle.isCursorVisible = false
                exerciseTitle.isSelected = false
                exerciseTitle.setBackgroundResource(android.R.color.transparent)
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(exerciseTitle.windowToken, 0)

                if(!HomeScreen.databaseInterface.setExerciseName(exerciseType, exerciseTitle.text.toString())) {
                    exerciseTitle.setText(prevName)
                    val dialog = AlertDialog.Builder(this)
                    dialog.setMessage(R.string.same_name_exercise)
                    dialog.setPositiveButton("Ok") { _, _ ->}
                    dialog.create().show()
                }
            }
        }

        chartView.chart?.marker = DefaultCartesianMarker(TextComponent(),
            labelPosition = DefaultCartesianMarker.LabelPosition.AbovePoint)
        chartView.modelProducer = CartesianChartModelProducer()

        periodSelector.onItemSelectedListener = PeriodSelectorListener()
        workoutList.layoutManager = LinearLayoutManager(this)
        workoutList.adapter = HistoryDisplayAdapter(workouts, applicationContext)
    }


}