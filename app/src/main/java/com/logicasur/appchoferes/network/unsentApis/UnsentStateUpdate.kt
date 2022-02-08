package com.logicasur.appchoferes.network.unsentApis

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.logicasur.appchoferes.network.GeoPosition
import com.logicasur.appchoferes.network.signinResponse.State
import com.logicasur.appchoferes.network.signinResponse.Vehicle

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