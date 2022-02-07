package com.logicasur.appchoferes.network.unsentApis

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.logicasur.appchoferes.network.GeoPosition

@Entity
data class UnsentUploadActivity( @PrimaryKey(autoGenerate = true)
                                 var roomDBId: Int =0, var activity: Int, var totalTime: Int?
)