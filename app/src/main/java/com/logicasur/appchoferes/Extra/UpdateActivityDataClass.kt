package com.logicasur.appchoferes.Extra

import com.logicasur.appchoferes.data.network.GeoPosition
import com.logicasur.appchoferes.data.network.signinResponse.State
import com.logicasur.appchoferes.data.network.signinResponse.Vehicle

data class UpdateActivityDataClass(var datetime: String?,
                                    var  totalTime: Int?,
                                    var activity: Int?,
                                    var  geoPosition: GeoPosition?,
                                     var vehicle: Vehicle?,
                                   var state: State?
)