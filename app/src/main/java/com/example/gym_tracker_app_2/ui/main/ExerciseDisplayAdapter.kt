package com.example.gym_tracker_app_2.ui.main

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.Spinner
import com.example.gym_tracker_app_2.R
import com.example.gym_tracker_app_2.Set

class ExerciseDisplayAdapter(private val context: Context, private var sets: ArrayList<Set>) : BaseAdapter() {
    companion object {
        private var nextId : Long = 0
    }

    override fun getCount(): Int {
        return sets.size
    }

    override fun getItem(index: Int): Set {
        return sets[index]
    }

    override fun getItemId(index: Int): Long {
        return sets[index].id
    }

    override fun getView(index: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.set_layout, parent, false)

            val setCount = view!!.findViewById<EditText>(R.id.setCount)
            val setWeight = view.findViewById<EditText>(R.id.setWeight)
            val setUnit = view.findViewById<Spinner>(R.id.setUnit)
            val set = getItem(index)

            setCount.addTextChangedListener(object : TextWatcher {
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
            })

            setWeight.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(text: Editable?) {}

                override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                    if(text.isNullOrEmpty() || text.toString().toFloatOrNull() == null) set.weight = 0f
                    else set.weight = text.toString().toFloat()

                    val isWhole = set.weight % 1f == 0f
                    var moveCursor = true

                    if(set.weight == 0f && setWeight.text.toString() != "0") setWeight.setText("0")
                    else if(!isWhole && set.weight.toString() != setWeight.text.toString()) setWeight.setText(set.weight.toString())
                    else if(isWhole && set.weight.toInt().toString() != setWeight.text.toString() && setWeight.text.last() != '.') setWeight.setText(set.weight.toInt().toString())
                    else moveCursor = false

                    if(moveCursor) setWeight.setSelection(setWeight.text.length)
                }
            })

            setUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, selected: View?, index: Int, id: Long) {
                    set.unit = index.toByte()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    setUnit.setSelection(0)
                }
            }

            setCount.setText(set.count.toString())
            if(set.weight % 1f == 0f) setWeight.setText(set.weight.toInt().toString())
            else setWeight.setText(set.weight.toString())
            setUnit.setSelection(set.unit.toInt())
        }

        return view
    }

    fun addSet() {
        sets.add(Set(nextId++))
        notifyDataSetChanged()
    }

    fun removeSet() {
        sets.removeLast()
        notifyDataSetChanged()
    }
}