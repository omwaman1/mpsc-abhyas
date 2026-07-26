package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_affairs")
data class CurrentAffairsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titleMarathi: String,
    val titleEnglish: String,
    val summaryMarathi: String,
    val summaryEnglish: String,
    val category: String, // Maharashtra, Economy, Polity, Environment
    val date: String,
    val isRead: Boolean = false
)
