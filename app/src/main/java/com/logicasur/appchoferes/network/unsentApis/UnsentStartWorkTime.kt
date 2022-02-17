package com.logicasur.appchoferes.network.unsentApis

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UnsentStartWorkTime(
    @PrimaryKey(autoGenerate = true)
    var roomid: Int = 0,
    var time: String?
)