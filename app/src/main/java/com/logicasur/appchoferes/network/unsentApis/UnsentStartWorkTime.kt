package com.logicasur.appchoferes.network.unsentApis

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UnsentStartWorkTime(
    @PrimaryKey
    var time: String
)