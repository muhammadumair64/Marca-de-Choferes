package com.logicasur.appchoferes.data.network.wrongData

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class wrongDataReport(
    @PrimaryKey(autoGenerate = true)
    var roomDBId: Int = 0,
    var username: String = "",

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