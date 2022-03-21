package com.logicasur.appchoferes.beforeAuth.otpScreen.interfaces

import java.util.*

interface OnEndLoadingCallbacks {
    fun endLoading(messsage:String)
    fun openPopup(myTimer: Timer?, b: Boolean, forServer: Boolean)
   fun openServerPopup(b: Boolean)
   fun calculateTimeFromLocalDB()
}