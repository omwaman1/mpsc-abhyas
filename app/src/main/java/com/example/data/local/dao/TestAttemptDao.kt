package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.TestAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestAttemptDao {
    @Query("SELECT * FROM test_attempts ORDER BY timestamp DESC")
    fun getAllAttempts(): Flow<List<TestAttemptEntity>>

    @Query("SELECT * FROM test_attempts WHERE testId = :testId ORDER BY timestamp DESC")
    fun getAttemptsForTest(testId: String): Flow<List<TestAttemptEntity>>

    @Query("SELECT * FROM test_attempts WHERE attemptId = :attemptId")
    suspend fun getAttemptById(attemptId: Long): TestAttemptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: TestAttemptEntity): Long
}
