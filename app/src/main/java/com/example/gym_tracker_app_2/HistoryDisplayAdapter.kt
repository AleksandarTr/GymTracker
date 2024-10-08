package com.example.gym_tracker_app_2

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import java.time.format.DateTimeFormatter

class HistoryDisplayAdapter(private val workouts: ArrayList<Workout>, private val context: Context): RecyclerView.Adapter<HistoryDisplayAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.workoutName)
        val date: TextView = view.findViewById(R.id.workoutDate)
        val open: Button = view.findViewById(R.id.openWorkoutButton)
    }

    private var lastOpenedWorkout = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.workout_row_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return workouts.size
    }

    inner class workoutOpener(private val id: Int, private val context: Context) : OnClickListener {
        override fun onClick(p0: View?) {
            val intent = Intent(context, WorkoutScreen::class.java)
            intent.putExtra("workoutID", id)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            lastOpenedWorkout = id
            startActivity(context, intent, null)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = workouts[position].name
        holder.date.text = workouts[position].date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        holder.open.setOnClickListener(workoutOpener(workouts[position].id, context))
    }

    fun checkIfWorkoutExists() {
        if(lastOpenedWorkout == -1) return
        if(HomeScreen.databaseInterface.getWorkout(lastOpenedWorkout) == null) {
            val forDeletion = workouts.indexOfFirst { workout -> workout.id == lastOpenedWorkout }
            workouts.removeAt(forDeletion)
            notifyItemRemoved(forDeletion)
        }
    }
}