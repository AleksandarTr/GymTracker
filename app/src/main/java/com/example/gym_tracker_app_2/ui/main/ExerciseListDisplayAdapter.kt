package com.example.gym_tracker_app_2.ui.main

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.gym_tracker_app_2.HomeScreen
import com.example.gym_tracker_app_2.R
import com.example.gym_tracker_app_2.WorkoutScreen

class ExerciseListDisplayAdapter(private val exercises: ArrayList<String>, private val context: Context)
    : RecyclerView.Adapter<ExerciseListDisplayAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.exerciseName)
        val open: Button = view.findViewById(R.id.exerciseButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercise_row_layout, parent, false)

        return ViewHolder(view)
    }

    class openExerciseStats(val id: Int, val context: Context) : View.OnClickListener {
        override fun onClick(button: View) {
            val intent = Intent(context, WorkoutScreen::class.java)
            intent.putExtra("workoutID", id)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(context, intent, null)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = exercises[position]
        holder.open.setOnClickListener(
            HomeScreen.databaseInterface.getExerciseTypeID(exercises[position], false)?.let {
                openExerciseStats(it, context)
            })
    }

    override fun getItemCount(): Int {
        return exercises.size
    }
}