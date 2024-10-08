package com.example.gym_tracker_app_2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.TreeMap

class DatabaseInterface (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "workoutDatabase.db"
        const val DATABASE_VERSION = 6
    }

    private var workoutID = -1
    private var exerciseID = -1
    private var setID = -1L

    private val loadFormula = "Sum(S.count * S.weight * (Select ratio from UnitConversion where unit1 = S.unit and unit2 = ?))"

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
                    "exerciseID INTEGER NOT NULL REFERENCES Exercise(id)," +
                    "unit INTEGER NOT NULL REFERENCES Unit(id)," +
                    "warmup INTEGER NOT NULL)"

        val UNIT_CREATE =
            "CREATE TABLE Unit (" +
                    "id INTEGER PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "type TEXT NOT NULL)"

        val UNIT_CONVERSION_CREATE =
            "CREATE TABLE UnitConversion (" +
                    "unit1 INTEGER NOT NULL References Unit(id)," +
                    "unit2 INTEGER NOT NULL References Unit(id)," +
                    "ratio REAL NOT NULL," +
                    "PRIMARY KEY(unit1, unit2))"

        db.execSQL(WORKOUT_CREATE)
        db.execSQL(EXERCISE_TYPE_CREATE)
        db.execSQL(EXERCISE_CREATE)
        db.execSQL(UNIT_CREATE)
        db.execSQL(UNIT_CONVERSION_CREATE)
        db.execSQL(SET_CREATE)

        addVer3Units(db)
        addVer4Units(db)
        addVer6Units(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if(oldVersion < 2 && newVersion >= 2)
            db.execSQL("Alter table ExerciseSet ADD unit INTEGER NOT NULL Default(0)")
        if(oldVersion < 3 && newVersion >= 3) addUnits(db)
        if(oldVersion < 4 && newVersion >= 4) addVer4Units(db)
        if(oldVersion < 5 && newVersion >= 5)
            db.execSQL("Alter table ExerciseSet ADD warmup INTEGER NOT NULL Default(0)")
        if(oldVersion < 6 && newVersion >= 6) addVer6Units(db)
    }

    private fun addVer3Units(db: SQLiteDatabase) {
        db.execSQL("Insert into Unit Values (0, 'kg', 'weight'), (1, 'lbs', 'weight')")
        db.execSQL("Insert into UnitConversion VALUES (0, 1, 2.204622), (1, 0, 0.4535923)")
    }

    private fun addVer4Units(db: SQLiteDatabase) {
        db.execSQL("Insert into UnitConversion VALUES (0, 0, 1), (1, 1, 1)")
    }

    private fun addVer6Units(db: SQLiteDatabase) {
        db.execSQL("Insert into Unit Values (2, 's', 'time'), (3, 'min', 'time'), (4, 'h', 'time'), (5, 'rep', 'rep')")
        db.execSQL("Insert into UnitConversion Values (2, 2, 1), (2, 3, 0.016666), (2, 4, 0.0002777)," +
                "(3, 2, 60), (3, 3, 1), (3, 4, 0.016666), (4, 2, 3600), (4, 3, 60), (4, 4, 1), (5, 5, 1)")
    }

    private fun addUnits(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE Unit (" +
                "id INTEGER PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "type TEXT NOT NULL)")
        db.execSQL("CREATE TABLE UnitConversion (" +
                "unit1 INTEGER NOT NULL References Unit(id)," +
                "unit2 INTEGER NOT NULL References Unit(id)," +
                "ratio REAL NOT NULL," +
                "PRIMARY KEY(unit1, unit2))")
        addVer3Units(db)

        db.execSQL("PRAGMA foreign_keys=off")
        db.execSQL("ALTER TABLE ExerciseSet RENAME TO ExerciseSet_old")
        db.execSQL("CREATE TABLE ExerciseSet (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "count INTEGER NOT NULL," +
                "weight REAL NOT NULL," +
                "exerciseID INTEGER NOT NULL REFERENCES Exercise(id)," +
                "unit INTEGER NOT NULL Default(0) REFERENCES Unit(id))")
        db.execSQL("Insert into ExerciseSet select * from ExerciseSet_old")
        db.execSQL("Drop table ExerciseSet_old")
        db.execSQL("PRAGMA foreign_keys=on")
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
        val cursor = readableDatabase.rawQuery("Select id, count, weight, warmup, unit " +
                "from ExerciseSet " +
                "where exerciseID = ? " +
                "order by id ASC", arrayOf(id.toString()))
        with(cursor) {
            while(moveToNext()) {
                val set = Set(getLong(0))
                set.count = getInt(1)
                set.weight = getFloat(2)
                set.warmup = getInt(3) != 0
                set.unit = Unit.getUnit((getInt(4)))
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
        val cursor = readableDatabase.rawQuery("Select id from ExerciseType where upper(name) = ?", arrayOf(name.uppercase()))
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

    fun updateSet(id: Long, count: Int, weight: Float, exerciseID: Int, unit: Unit, warmup: Boolean) {
        val values = ContentValues()
        values.put("count", count)
        values.put("weight", weight)
        values.put("exerciseID", exerciseID)
        values.put("unit", Unit.getPosition(unit))
        values.put("warmup", warmup)

        if(writableDatabase.update("ExerciseSet", values, "id = ?", arrayOf(id.toString())) < 1) {
            values.put("id", id)
            writableDatabase.insert("ExerciseSet", null, values)
        }
    }

    fun getExerciseTypes(): ArrayList<String> {
        val exerciseTypes = ArrayList<String>()

        val cursor = readableDatabase.rawQuery("Select name from ExerciseType where name != ''", null)
        while (cursor.moveToNext()) exerciseTypes.add(cursor.getString(0))
        cursor.close()

        return exerciseTypes
    }

    fun getWorkouts(): ArrayList<Workout> {
        val result = ArrayList<Workout>()

        val cursor = readableDatabase.rawQuery("Select id, name, date from Workout", null)
        while(cursor.moveToNext())
            result.add(Workout(cursor.getInt(0), cursor.getString(1),
                LocalDate.parse(cursor.getString(2), DateTimeFormatter.ofPattern("yyyy.MM.dd"))))
        cursor.close()

        return result
    }

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

    fun getExercisePR(id: Int, preferredUnit: Unit) : Int? {
        val cursor = readableDatabase.rawQuery("Select E.id, $loadFormula as Load " +
                "from Exercise as E join ExerciseSet as S on E.id = S.exerciseID " +
                "where E.exerciseType = ? and warmup = 0 " +
                "group by E.id " +
                "order by Load Desc", arrayOf(Unit.getPosition(preferredUnit).toString(), id.toString()))
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

    fun getExerciseStats(id: Int, dateCutoff: String): Map<LocalDate, Double> {
        val preferredUnitCursor = readableDatabase.rawQuery("Select type " +
                "from Unit U join ExerciseSet S on U.id = S.unit join Exercise E on E.id = S.exerciseID " +
                "where E.exerciseType = ?", arrayOf(id.toString()))
        preferredUnitCursor.moveToFirst()
        val preferredUnit = SettingsScreen.getPreferredUnit(preferredUnitCursor.getString(0))
        preferredUnitCursor.close()

        val result = TreeMap<LocalDate, Double>()
        val cursor = readableDatabase.rawQuery("Select date, $loadFormula" +
                "from Workout W join Exercise E on W.id = E.WorkoutID join ExerciseSet S on S.exerciseID = E.id " +
                "where exerciseType = ? and date > ? and warmup = 0 " +
                "group by date " +
                "order by date ASC", arrayOf(Unit.getPosition(preferredUnit).toString(), id.toString(), dateCutoff))

        while(cursor.moveToNext())
            result[LocalDate.parse(cursor.getString(0), DateTimeFormatter.ofPattern("yyyy.MM.dd"))] = cursor.getDouble(1)

        cursor.close()
        return result
    }

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

    fun setExerciseName(id: Int, name: String): Boolean {
        val checker = readableDatabase.rawQuery("Select id from ExerciseType where upper(name) = ?", arrayOf(name.uppercase()))
        if(checker.count != 0) {
            checker.close()
            return false
        }
        checker.close()

        val values = ContentValues()
        values.put("name", name)
        return readableDatabase.update("ExerciseType", values, "id = ?", arrayOf(id.toString())) >= 1
    }

    fun getExerciseUnitType(id: Int): String {
        val cursor = readableDatabase.rawQuery("Select type " +
                "from Unit U join ExerciseSet S on U.id = S.unit join Exercise E on E.id = S.exerciseID " +
                "where exerciseType = ? " +
                "order by E.id ASC", arrayOf(id.toString()))

        var result = ""
        if(cursor.moveToFirst()) result = cursor.getString(0)
        cursor.close()
        return result
    }

    fun deleteExercise(id: Int) {
        writableDatabase.delete("Exercise", "id = ?", arrayOf(id.toString()))
    }

    fun deleteSet(id: Long) {
        writableDatabase.delete("ExerciseSet", "id = ?", arrayOf(id.toString()))
    }

    fun deleteWorkout(id: Int) {
        writableDatabase.delete("Workout", "id = ?", arrayOf(id.toString()))
    }

    fun getExerciseTypeName(id: Int) : String? {
        val nameCursor = readableDatabase.rawQuery("Select name from ExerciseType where id = ?", arrayOf(id.toString()))
        if(nameCursor.count == 0) return null
        nameCursor.moveToFirst()
        val name = nameCursor.getString(0)
        nameCursor.close()

        return name
    }
}