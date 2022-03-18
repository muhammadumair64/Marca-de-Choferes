package com.logicasur.appchoferes.data.network.logoutResponse

data class MessageResponse(
    val msg: String
){


    fun checkIfMessageIsOkay(): Boolean{
        return msg == "ok"
    }

}