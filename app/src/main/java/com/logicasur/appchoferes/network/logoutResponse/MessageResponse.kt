package com.logicasur.appchoferes.network.logoutResponse

data class MessageResponse(
    val message: String
){


    fun checkIfMessageIsOkay(): Boolean{
        return message == "ok"
    }

}