package com.logicasur.appchoferes.network.unsentApis

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.logicasur.appchoferes.network.GeoPosition

@Entity
data class UnsentStatusOrUploadActivity(
    @PrimaryKey(autoGenerate = true)
    var roomDBId: Int = 0,
    var dateTime:String,
    val stateId: Int?,
    val stateDescription: String?,
    var activity: Int?,
    var totalTime: Int?,
    val vehicleId: Int,
    val vehicleDescription: String,
    val vehiclePlateNumber: String,
    var latitudeGeoPosition: Double,
    var longitudeGeoPosition: Double
)