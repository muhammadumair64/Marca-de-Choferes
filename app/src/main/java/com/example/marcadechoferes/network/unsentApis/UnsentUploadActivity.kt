package com.example.marcadechoferes.network.unsentApis

import androidx.room.Entity
import com.example.marcadechoferes.network.GeoPosition
@Entity
data class UnsentUploadActivity(
    var activity: Int, var totalTime: Int?, var geoPosition: GeoPosition?
)