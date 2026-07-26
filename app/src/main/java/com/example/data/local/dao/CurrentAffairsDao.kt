package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.CurrentAffairsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrentAffairsDao {
    @Query("SELECT * FROM current_affairs ORDER BY id DESC")
    fun getAllCurrentAffairs(): Flow<List<CurrentAffairsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentAffairs(items: List<CurrentAffairsEntity>)

    @Query("SELECT COUNT(*) FROM current_affairs")
    suspend fun getCount(): Int
}
