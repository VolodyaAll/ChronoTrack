package com.sharai.chronotrack.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sharai.chronotrack.data.dao.ActivityDao
import com.sharai.chronotrack.data.dao.CommentDao
import com.sharai.chronotrack.data.dao.TimeEntryDao
import com.sharai.chronotrack.data.model.Activity
import com.sharai.chronotrack.data.model.Comment
import com.sharai.chronotrack.data.model.TimeEntry

@Database(
    entities = [Activity::class, TimeEntry::class, Comment::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun timeEntryDao(): TimeEntryDao
    abstract fun commentDao(): CommentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private fun insertPresetActivities(database: SupportSQLiteDatabase) {
            // Добавляем предустановленные активности
            database.execSQL("""
                INSERT INTO activities (name, color, icon, isActive, isArchived) VALUES 
                ('Саморазвитие', ${0xFF9C27B0.toInt()}, 'Icons.Default.Star', 1, 0),
                ('Работа', ${0xFFFF4081.toInt()}, 'Icons.Default.Work', 0, 0),
                ('Сон', ${0xFF3F51B5.toInt()}, 'Icons.Default.Nightlight', 0, 0),
                ('Хобби', ${0xFF4CAF50.toInt()}, 'Icons.Default.Star', 0, 0),
                ('Семья', ${0xFFFF9800.toInt()}, 'Icons.Default.Group', 0, 0),
                ('Спорт', ${0xFF2196F3.toInt()}, 'Icons.AutoMirrored.Filled.DirectionsRun', 0, 0)
            """)
        }

        private val roomCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                insertPresetActivities(db)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chronotrack_database"
                )
                .addCallback(roomCallback)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 