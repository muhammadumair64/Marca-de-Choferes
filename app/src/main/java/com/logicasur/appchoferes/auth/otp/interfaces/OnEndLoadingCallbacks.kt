package com.logicasur.appchoferes.auth.otp.interfaces

import java.util.*

interface OnEndLoadingCallbacks {
    fun endLoading()
    fun openPopup(myTimer: Timer?)
   fun openServerPopup()
}