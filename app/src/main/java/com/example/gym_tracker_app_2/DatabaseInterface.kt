package com.example.gym_tracker_app_2

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseInterface (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "workoutDatabase.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val WORKOUT_CREATE =
            "CREATE TABLE Workout (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "date TEXT NOT NULL)"

        val EXERCISE_TYPE_CREATE =
            "CREATE TABLE ExerciseType (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL)"

        val EXERCISE_CREATE =
            "CREATE TABLE Exercise (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "exerciseType INTEGER NOT NULL REFERENCES ExerciseType(id)," +
                    "workoutID INTEGER NOT NULL REFERENCES Workout(id))"

        val SET_CREATE =
            "CREATE TABLE ExerciseSet (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "count INTEGER NOT NULL," +
                    "weight REAL NOT NULL," +
                    "exerciseID INTEGER NOT NULL REFERENCES Exercise(id))"

        db.execSQL(WORKOUT_CREATE)
        db.execSQL(EXERCISE_TYPE_CREATE)
        db.execSQL(EXERCISE_CREATE)
        db.execSQL(SET_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //TODO("Not yet implemented")
    }

    fun getNextWorkoutID() : Int {
        var nextId = 0
        val cursor = readableDatabase.rawQuery("Select MAX(id) from Workout", null)
        if(cursor.count == 1) {
            cursor.moveToFirst()
            nextId = cursor.getInt(0) + 1
        }
        cursor.close()

        return nextId
    }

    fun getExerciseName(id: Int): String? {
        val typeCursor = readableDatabase.rawQuery("Select exerciseType from Exercise where id = ?", arrayOf(id.toString()))
        if(typeCursor.count == 0) return null
        typeCursor.moveToFirst()
        val type = typeCursor.getInt(0)
        typeCursor.close()

        val nameCursor = readableDatabase.rawQuery("Select name from ExerciseType where id = ?", arrayOf(type.toString()))
        if(nameCursor.count == 0) return null
        nameCursor.moveToFirst()
        val name = nameCursor.getString(0)
        nameCursor.close()

        return name
    }

    fun getExerciseSets(id: Int): ArrayList<Set> {
        val result = ArrayList<Set>()
        val cursor = readableDatabase.rawQuery("Select id, count, weight from ExerciseSet where exerciseID = ?", arrayOf(id.toString()))
        with(cursor) {
            while(moveToNext()) {
                val set = Set(getLong(0))
                set.count = getInt(1)
                set.weight = getFloat(2)
                result.add(set)
            }
        }

        cursor.close()
        return result
    }

    fun getWorkoutExercises(id: Int): ArrayList<Int> {
        val result = ArrayList<Int>()
        val cursor = readableDatabase.rawQuery("Select id from Exercise where workoutID = ?", arrayOf(id.toString()))
        with(cursor) {
            while(moveToNext())
                result.add(getInt(0))
        }

        cursor.close()
        return result
    }

    fun getNextExerciseID(): Int {
        var nextId = 0
        val cursor = readableDatabase.rawQuery("Select MAX(id) from Exercise", null)
        if(cursor.count == 1) {
            cursor.moveToFirst()
            nextId = cursor.getInt(0) + 1
        }
        cursor.close()

        return nextId
    }
}