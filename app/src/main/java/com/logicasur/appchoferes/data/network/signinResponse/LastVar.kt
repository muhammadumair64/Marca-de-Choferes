package com.logicasur.appchoferes.data.network.signinResponse


data class LastVar(
    val LastWorkBreaklatitud: Float,
    val lastActivity: Int?,
    val lastIdVehicle: LastIdVehicle?,
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
    val lastWorkedHoursLongitud:Float,
    val lastWorkedHoursTotal: Int?
)