package com.example.marcadechoferes.network.signinResponse

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class LastVarForRoom(
    @PrimaryKey(autoGenerate = true)
    var roomDBId: Int =0,

    val LastWorkBreaklatitud: Int?,
    val lastActivity: Int?,
    val lastState: Int?,
    val lastStateDate: String?,
    val lastStateLatitud: Int?,
    val lastStateLongitud: Int?,
    val lastWorkBreakDateEnd: String?,
    val lastWorkBreakDateIni: String?,
    val lastWorkBreakLongitud: Int?,
    val lastWorkBreakTotal: Int?,
    val lastWorkedHoursDateEnd: String?,
    val lastWorkedHoursDateIni: String?,
    val lastWorkedHoursLatitud: Int?,
    val lastWorkedHoursLongitud: Int?,
    val lastWorkedHoursTotal: Int?

)