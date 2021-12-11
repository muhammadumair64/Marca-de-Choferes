package com.example.marcadechoferes.network.signinResponse

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.marcadechoferes.network.GeoPosition
import retrofit2.http.Body

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