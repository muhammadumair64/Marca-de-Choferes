package com.example.marcadechoferes.network.signinResponse

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LastIdVehicle(
    @PrimaryKey(autoGenerate = true)
    var roomDBId: Int =0,

    val description: String?,
    val id: Int?,
    val plateNumber: String?
)