package com.example.gym_tracker_app_2

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import java.time.format.DateTimeFormatter

class ExerciseDisplayAdapter(private var sets: ArrayList<Set>, private val context: Context, private val recyclerView: RecyclerView)
    : RecyclerView.Adapter<ExerciseDisplayAdapter.ExerciseHolder>() {
    class ExerciseHolder(view: View) : RecyclerView.ViewHolder(view) {
        val setCount = view.findViewById<EditText>(R.id.setCount)
        val setWeight = view.findViewById<EditText>(R.id.setWeight)
        val setUnit = view.findViewById<Spinner>(R.id.setUnit)
        val setTimeStamp = view.findViewById<TextView>(R.id.setTimeStamp)
        val setWarmup = view.findViewById<CheckBox>(R.id.setWarmup)
    }

    private class CountChangeListener(val set: Set, val setCount : EditText) : TextWatcher {
        override fun beforeTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(text: Editable?) {}

        override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
            if(text.isNullOrEmpty()) set.count = 0
            else set.count = text.toString().toInt()
            if(set.count.toString() != setCount.text.toString()) {
                setCount.setText(set.count.toString())
                setCount.setSelection(setCount.text.length)
            }
        }
    }

    private class WeightChangeListener(val set: Set, val setWeight : EditText) : TextWatcher {
        override fun beforeTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(text: Editable?) {}

        @SuppressLint("SetTextI18n")
        override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
            if (text.isNullOrEmpty() || text.toString()
                    .toFloatOrNull() == null
            ) set.weight = 0f
            else set.weight = text.toString().toFloat()

            val isWhole = set.weight % 1f == 0f
            var moveCursor = true

            if (set.weight == 0f && setWeight.text.toString() != "0") setWeight.setText("0")
            else if (!isWhole && set.weight.toString() != setWeight.text.toString()) setWeight.setText(
                set.weight.toString()
            )
            else if (isWhole && set.weight.toInt()
                    .toString() != setWeight.text.toString() && setWeight.text.last() != '.'
            ) setWeight.setText(set.weight.toInt().toString())
            else moveCursor = false

            if (moveCursor) setWeight.setSelection(setWeight.text.length)
        }
    }

    private class UnitChangeListener(val set: Set, val setUnit: Spinner, val setCount: EditText, val setWeight: EditText)
        : AdapterView.OnItemSelectedListener{
        @SuppressLint("SetTextI18n")
        override fun onItemSelected(
            parent: AdapterView<*>?,
            selected: View?,
            index: Int,
            id: Long
        ) {
            set.unit = Unit.getUnit(index)
            when(set.unit.type) {
                "time" -> {
                    setCount.isVisible = false
                    setWeight.isVisible = true
                    setCount.setText("1")
                }
                "rep" -> {
                    setCount.isVisible = true
                    setWeight.isVisible = false
                    setWeight.setText("1")
                }
                else -> {
                    setCount.isVisible = true
                    setWeight.isVisible = true
                }
            }
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
            setUnit.setSelection(0)
        }
    }

    private val forDeletion = ArrayList<Long>()

    fun addSet() {
        sets.add(Set(HomeScreen.databaseInterface.getNextSetID()))
        notifyItemInserted(sets.size - 1)
    }

    fun removeSet() {
        forDeletion.add(sets.last().id)
        sets.removeLast()
        notifyItemRemoved(sets.size)
    }

    private fun getUnits(unitType: String): ArrayList<String> {
        val units = ArrayList<String>()
        for(i in 0 until Unit.getUnitCount())
            if(Unit.getUnit(i).type == unitType || unitType.isEmpty()) units.add(Unit.getUnit(i).name)
        return units
    }

    fun updateUnits(unitType: String) {
        val units = getUnits(unitType)

        try {
            for (i in 0 until sets.size) {
                val holder = recyclerView.findViewHolderForAdapterPosition(i) as ExerciseHolder
                val unitAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, units)
                unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                holder.setUnit.adapter = unitAdapter
            }
        }
        catch (_: NullPointerException) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.set_layout, parent, false)

        return ExerciseHolder(view)
    }

    override fun getItemCount(): Int {
        return sets.size
    }

    override fun onBindViewHolder(holder: ExerciseHolder, position: Int) {
        val set = sets[position]
        holder.setCount.setText(set.count.toString())
        if (set.weight % 1f == 0f) holder.setWeight.setText(set.weight.toInt().toString())
        else holder.setWeight.setText(set.weight.toString())
        holder.setTimeStamp.text = set.timeStamp.format(DateTimeFormatter.ofPattern("mm:ss"))
        holder.setWarmup.isChecked = set.warmup

        val units = getUnits(if(sets.size > 0) sets[0].unit.type else "")
        val unitAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, units)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.setUnit.adapter = unitAdapter
        holder.setUnit.setSelection(units.indexOf(set.unit.name))

        holder.setCount.addTextChangedListener(CountChangeListener(set, holder.setCount))
        holder.setWeight.addTextChangedListener(WeightChangeListener(set, holder.setWeight))
        holder.setUnit.onItemSelectedListener = UnitChangeListener(set, holder.setUnit, holder.setCount, holder.setWeight)
        holder.setWarmup.setOnCheckedChangeListener { _, isWarmup -> set.warmup = isWarmup}
    }

    fun save() {
        for(set in forDeletion) HomeScreen.databaseInterface.deleteSet(set)
        forDeletion.clear()
    }
}