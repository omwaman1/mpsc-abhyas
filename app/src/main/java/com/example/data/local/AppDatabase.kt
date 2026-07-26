package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CurrentAffairsDao
import com.example.data.local.dao.QuestionDao
import com.example.data.local.dao.TestAttemptDao
import com.example.data.local.dao.TestPaperDao
import com.example.data.local.entities.CurrentAffairsEntity
import com.example.data.local.entities.QuestionEntity
import com.example.data.local.entities.TestAttemptEntity
import com.example.data.local.entities.TestPaperEntity

@Database(
    entities = [
        QuestionEntity::class,
        TestPaperEntity::class,
        TestAttemptEntity::class,
        CurrentAffairsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun testPaperDao(): TestPaperDao
    abstract fun testAttemptDao(): TestAttemptDao
    abstract fun currentAffairsDao(): CurrentAffairsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpsc_prep_database.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
