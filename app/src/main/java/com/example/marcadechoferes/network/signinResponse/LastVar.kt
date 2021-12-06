package com.example.marcadechoferes.network.signinResponse


data class LastVar(
    val LastWorkBreaklatitud: Int?,
    val lastActivity: Int?,
    val lastIdVehicle: LastIdVehicle?,
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