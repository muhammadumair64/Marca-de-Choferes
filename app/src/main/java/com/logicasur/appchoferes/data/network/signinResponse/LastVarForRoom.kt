package com.logicasur.appchoferes.data.network.signinResponse

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class LastVarForRoom(
    @PrimaryKey(autoGenerate = true)
    var roomDBId: Int =0,

    val LastWorkBreaklatitud: Float,
    val lastActivity: Int?,
    val lastState: Int?,
    val lastStateDate: String?,
    val lastStateLatitud: Float,
    val lastStateLongitud: Float,
    val lastWorkBreakDateEnd: String?,
    val lastWorkBreakDateIni: String?,
    val lastWorkBreakLongitud: Float,
    val lastWorkBreakTotal: Int?,
    val lastWorkedHoursDateEnd: String?,
    val lastWorkedHoursDateIni: String?,
    val lastWorkedHoursLatitud: Float,
    val lastWorkedHoursLongitud: Float,
    val lastWorkedHoursTotal: Int?

)