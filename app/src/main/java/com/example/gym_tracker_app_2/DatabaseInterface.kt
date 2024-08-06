package com.example.gym_tracker_app_2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.TreeMap

class DatabaseInterface (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "workoutDatabase.db"
        const val DATABASE_VERSION = 1
    }

    private var workoutID = -1
    private var exerciseID = -1
    private var setID = -1L
    private val exerciseTypes = ArrayList<String>()

    private val loadFormula = "Sum(S.count * S.weight * S.weight)"

    override fun onCreate(db: SQLiteDatabase) {
        val WORKOUT_CREATE =
            "CREATE TABLE Workout (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "date TEXT NOT NULL)"

        val EXERCISE_TYPE_CREATE =
            "CREATE TABLE ExerciseType (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT UNIQUE NOT NULL)"

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
        if(workoutID == -1) {
            var nextId = 0
            val cursor = readableDatabase.rawQuery("Select MAX(id) from Workout", null)
            if (cursor.count == 1) {
                cursor.moveToFirst()
                nextId = cursor.getInt(0) + 1
            }
            cursor.close()

            workoutID = nextId
            return nextId
        }
        return ++workoutID
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
        if(exerciseID == -1) {
            var nextId = 0
            val cursor = readableDatabase.rawQuery("Select MAX(id) from Exercise", null)
            if (cursor.count == 1) {
                cursor.moveToFirst()
                nextId = cursor.getInt(0) + 1
            }
            cursor.close()

            exerciseID = nextId
            return nextId
        }
        return ++exerciseID
    }

    fun getNextSetID(): Long {
        if(setID == -1L) {
            var nextId = 0L
            val cursor = readableDatabase.rawQuery("Select MAX(id) from ExerciseSet", null)
            if (cursor.count == 1) {
                cursor.moveToFirst()
                nextId = cursor.getLong(0) + 1
            }
            cursor.close()

            setID = nextId
            return nextId
        }
        return ++setID
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateWorkout(id: Int, name: String, date: LocalDate) {
        val values = ContentValues()
        values.put("name", name)
        values.put("date", date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
        if(writableDatabase.update("Workout", values, "id = ?", arrayOf(id.toString())) < 1) {
            values.put("id", id)
            writableDatabase.insert("Workout", null, values)
        }
    }

    fun getExerciseTypeID(name: String, addExercise: Boolean): Int? {
        var result = -1
        val cursor = readableDatabase.rawQuery("Select id from ExerciseType where name = ?", arrayOf(name))
        if(cursor.count > 0) {
            cursor.moveToFirst()
            result = cursor.getInt(0)
        }
        cursor.close()
        if(result != -1) return result

        if(!addExercise) return null

        val idCursor = readableDatabase.rawQuery("Select coalesce(MAX(id), 0) from ExerciseType", null)
        result = if(idCursor.moveToFirst()) idCursor.getInt(0) + 1
        else 0
        idCursor.close()

        exerciseTypes.add(name)
        val values = ContentValues()
        values.put("id", result)
        values.put("name", name)
        writableDatabase.insert("ExerciseType", null, values)
        return result
    }

    fun updateExercise(id: Int, name: String, workoutID: Int) {
        val values = ContentValues()
        values.put("exerciseType", getExerciseTypeID(name, true))
        values.put("workoutID", workoutID)

        if(writableDatabase.update("Exercise", values, "id = ?", arrayOf(id.toString())) < 1) {
            values.put("id", id.toString())
            writableDatabase.insert("Exercise", null, values)
        }
    }

    fun updateSet(id: Long, count: Int, weight: Float, exerciseID: Int) {
        val values = ContentValues()
        values.put("count", count)
        values.put("weight", weight)
        values.put("exerciseID", exerciseID)

        if(writableDatabase.update("ExerciseSet", values, "id = ?", arrayOf(id.toString())) < 1) {
            values.put("id", id)
            writableDatabase.insert("ExerciseSet", null, values)
        }
    }

    fun getExerciseTypes(): ArrayList<String> {
        if(exerciseTypes.isNotEmpty()) return exerciseTypes

        val cursor = readableDatabase.rawQuery("Select name from ExerciseType", null)
        while (cursor.moveToNext()) exerciseTypes.add(cursor.getString(0))
        cursor.close()

        return exerciseTypes
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getWorkouts(): ArrayList<Workout> {
        val result = ArrayList<Workout>()

        val cursor = readableDatabase.rawQuery("Select id, name, date from Workout", null)
        while(cursor.moveToNext())
            result.add(Workout(cursor.getInt(0), cursor.getString(1),
                LocalDate.parse(cursor.getString(2), DateTimeFormatter.ofPattern("yyyy.MM.dd"))))
        cursor.close()

        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getWorkout(id: Int): Workout? {
        val cursor = readableDatabase.rawQuery("Select name, date from Workout where id = ?", arrayOf(id.toString()))
        if(cursor.count == 0) {
            cursor.close()
            return null
        }

        cursor.moveToFirst()
        val result = Workout(id, cursor.getString(0), LocalDate.parse(cursor.getString(1), DateTimeFormatter.ofPattern("yyyy.MM.dd")))
        cursor.close()
        return result
    }

    fun getExercisePR(id: Int) : Int? {
        val cursor = readableDatabase.rawQuery("Select E.id, $loadFormula as Load " +
                "from Exercise as E join ExerciseSet as S on E.id = S.exerciseID " +
                "where E.exerciseType = ? " +
                "group by E.id " +
                "order by Load Desc", arrayOf(id.toString()))
        if(cursor.moveToFirst()) {
            val result = cursor.getInt(0)
            cursor.close()
            return result
        }

        cursor.close()
        return null
    }

    fun getLastExercise(id: Int): Int? {
        val cursor = readableDatabase.rawQuery("Select E.id " +
                "from Exercise as E join Workout as W on E.workoutID = W.id " +
                "where exerciseType = ? " +
                "order by date DESC", arrayOf(id.toString()))
        if(cursor.moveToFirst()) {
            val result = cursor.getInt(0)
            cursor.close()
            return result
        }

        cursor.close()
        return null
    }

    fun getExercise(id: Int): ArrayList<Set> {
        val cursor = readableDatabase.rawQuery("Select S.id, count, weight " +
                "from Exercise as E join ExerciseSet as S on E.id = S.ExerciseID " +
                "where E.id = ?" +
                "order by S.id ASC", arrayOf(id.toString()))

        val result = ArrayList<Set>()
        while(cursor.moveToNext()) {
            val set = Set(cursor.getLong(0))
            set.count = cursor.getInt(1)
            set.weight = cursor.getFloat(2)
            result.add(set)
        }

        cursor.close()
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getExerciseStats(id: Int, dateCutoff: String): Map<LocalDate, Double> {
        val result = TreeMap<LocalDate, Double>()
        val cursor = readableDatabase.rawQuery("Select date, $loadFormula" +
                "from Workout W join Exercise E on W.id = E.WorkoutID join ExerciseSet S on S.exerciseID = E.id " +
                "where exerciseType = ? and date > ? " +
                "group by date " +
                "order by date ASC", arrayOf(id.toString(), dateCutoff))

        while(cursor.moveToNext())
            result[LocalDate.parse(cursor.getString(0), DateTimeFormatter.ofPattern("yyyy.MM.dd"))] = cursor.getDouble(1)

        cursor.close()
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getExerciseWorkouts(id: Int, dateCutoff: String): ArrayList<Workout> {
        val result = ArrayList<Workout>()
        val cursor = readableDatabase.rawQuery("Select distinct(W.id), W.name, date " +
                "from Workout W join Exercise E on W.id = WorkoutID " +
                "where exerciseType = ? and date > ? " +
                "order by date DESC", arrayOf(id.toString(), dateCutoff))

        while(cursor.moveToNext())
            result.add(Workout(cursor.getInt(0), cursor.getString(1),
                LocalDate.parse(cursor.getString(2), DateTimeFormatter.ofPattern("yyyy.MM.dd"))))
        cursor.close()

        return result
    }
}