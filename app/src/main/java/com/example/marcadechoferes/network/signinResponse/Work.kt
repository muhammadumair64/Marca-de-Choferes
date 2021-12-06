package com.example.marcadechoferes.network.signinResponse

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Work(
    @PrimaryKey(autoGenerate = true)
    var roomDBId: Int =0,

    val workBreak: Int,
    val workingHours: Int
)