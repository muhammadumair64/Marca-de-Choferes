package com.logicasur.appchoferes.network.signinResponse

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Profile(
    @PrimaryKey(autoGenerate = true)
    var roomDBId: Int =0,

    val avatar: String?,
    val language: Int?,
    val name: String="Muhammad Waleed",
    val notify: Boolean?,
    val surname: String = "Amir"
)