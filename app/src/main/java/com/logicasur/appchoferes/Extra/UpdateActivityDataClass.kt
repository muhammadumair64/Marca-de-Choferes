package com.logicasur.appchoferes.Extra

import com.logicasur.appchoferes.network.GeoPosition
import com.logicasur.appchoferes.network.signinResponse.Vehicle

data class UpdateActivityDataClass(var datetime: String?,
                                    var  totalTime: Int?,
                                    var activity: Int?,
                                    var  geoPosition: GeoPosition?,
                                     var vehicle: Vehicle?
)