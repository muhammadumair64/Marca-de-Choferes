package com.logicasur.appchoferes.network.unsentApis

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UnsentLanguageUpdation( @PrimaryKey(autoGenerate = true)
                                   var roomDBId: Int =0,var language:Int)
