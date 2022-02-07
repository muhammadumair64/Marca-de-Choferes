package com.example.marcadechoferes.network.unsentApis

import androidx.room.Entity
import com.example.marcadechoferes.network.GeoPosition
import com.example.marcadechoferes.network.signinResponse.State
import com.example.marcadechoferes.network.signinResponse.Vehicle
@Entity
data class UnsentStateUpdate(
    var datetime: String?,
    var totalTime: Int?,
    var state: State?,
    var geoPosition: GeoPosition?,
    var vehicle: Vehicle?
)