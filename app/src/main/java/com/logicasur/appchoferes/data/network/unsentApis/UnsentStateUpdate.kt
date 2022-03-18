package com.logicasur.appchoferes.data.network.unsentApis

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UnsentStateUpdate(
    @PrimaryKey(autoGenerate = true)
    var roomDBId: Int = 0,
    var datetime: String?,
    var totalTime: Int?,
    val stateId: Int,
    val stateDescription: String,
    val vehicleId: Int,
    val vehicleDescription: String,
    val vehiclePlateNumber: String,
    var latitudeGeoPosition: Double,
    var longitudeGeoPosition: Double


)