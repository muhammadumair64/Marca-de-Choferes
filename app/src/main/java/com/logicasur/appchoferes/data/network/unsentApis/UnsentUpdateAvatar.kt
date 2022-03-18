package com.logicasur.appchoferes.data.network.unsentApis

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UnsentUpdateAvatar ( @PrimaryKey(autoGenerate = true)
                                var roomDBId: Int =0,var avatar:String)