package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.TestPaperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestPaperDao {
    @Query("SELECT * FROM test_papers")
    fun getAllTestPapers(): Flow<List<TestPaperEntity>>

    @Query("SELECT * FROM test_papers WHERE category = :category")
    fun getTestPapersByCategory(category: String): Flow<List<TestPaperEntity>>

    @Query("SELECT * FROM test_papers WHERE testId = :testId")
    suspend fun getTestPaperById(testId: String): TestPaperEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestPapers(testPapers: List<TestPaperEntity>)

    @Query("SELECT COUNT(*) FROM test_papers")
    suspend fun getTestPaperCount(): Int

    @Query("DELETE FROM test_papers")
    suspend fun deleteAllTestPapers()
}
