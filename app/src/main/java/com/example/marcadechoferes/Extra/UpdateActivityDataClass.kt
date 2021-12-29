package com.example.marcadechoferes.Extra

import com.example.marcadechoferes.auth.repository.AuthRepository
import com.example.marcadechoferes.network.GeoPosition
import com.example.marcadechoferes.network.signinResponse.Vehicle

data class UpdateActivityDataClass(var datetime: String?,
                                    var  totalTime: Int?,
                                    var activity: Int?,
                                    var  geoPosition: GeoPosition?,
                                     var vehicle: Vehicle?
)