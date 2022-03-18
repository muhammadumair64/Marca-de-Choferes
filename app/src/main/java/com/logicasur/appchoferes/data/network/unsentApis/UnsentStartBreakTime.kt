package com.logicasur.appchoferes.data.network.unsentApis

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class UnsentStartBreakTime(
    @PrimaryKey(autoGenerate = true)
    var roomid: Int = 0,
    var time: String
)