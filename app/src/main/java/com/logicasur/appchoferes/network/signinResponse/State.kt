package com.logicasur.appchoferes.network.signinResponse

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.logicasur.appchoferes.network.GeoPosition

@Entity
data class State(
    @PrimaryKey(autoGenerate = true)
    var roomDBId: Int =0,
    val id: Int,
    val description: String

)

data class Wrap ( val datetime: String?,
                 val  totalTime: Int?,
                val  state:State,
                 val  geoPosition: GeoPosition?,
                val  vehicle: Vehicle?)