package com.example.marcadechoferes.network.signinResponse

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.marcadechoferes.network.GeoPosition

@Entity
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    var roomDBId: Int =0,
      val id: Int,
    val description: String,
    val plateNumber: String
)


data class WrapVehicle (val datetime: String?,
                 val  totalTime: Int?,
                 val  activity: Int?,
                 val  geoPosition: GeoPosition?,
                 val  vehicle: Vehicle?)