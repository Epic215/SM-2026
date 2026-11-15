package com.example.habittracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.HabitEntry
import com.example.habittracker.data.model.HabitSetting

@Database(
    entities = [Habit::class, HabitEntry::class, HabitSetting::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `habit_settings` (" +
                        "`habitId` INTEGER NOT NULL, `key` TEXT NOT NULL, `value` TEXT NOT NULL, " +
                        "PRIMARY KEY(`habitId`, `key`), " +
                        "FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
            }
        }

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "habit_tracker.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
