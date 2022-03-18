package com.logicasur.appchoferes.beforeAuth.otpScreen.interfaces

import java.util.*

interface OnEndLoadingCallbacks {
    fun endLoading()
    fun openPopup(myTimer: Timer?)
   fun openServerPopup()
   fun calculateTimeFromLocalDB()
}