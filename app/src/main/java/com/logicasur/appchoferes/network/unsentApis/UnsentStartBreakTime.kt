package com.logicasur.appchoferes.network.unsentApis

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class UnsentStartBreakTime(
    @PrimaryKey
    var roomid: Int = 0,
    var time: String
)