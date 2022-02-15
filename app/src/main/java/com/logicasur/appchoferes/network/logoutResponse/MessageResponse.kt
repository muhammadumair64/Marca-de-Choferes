package com.logicasur.appchoferes.network.logoutResponse

data class MessageResponse(
    val msg: String
){


    fun checkIfMessageIsOkay(): Boolean{
        return msg == "ok"
    }

}