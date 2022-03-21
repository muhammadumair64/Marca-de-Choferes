package com.logicasur.appchoferes.beforeAuth.otpScreen.interfaces

import java.util.*

interface OnEndLoadingCallbacks {
    fun endLoading(messsage:String)
    fun openPopup(myTimer: Timer?, b: Boolean)
   fun openServerPopup()
   fun calculateTimeFromLocalDB()
}