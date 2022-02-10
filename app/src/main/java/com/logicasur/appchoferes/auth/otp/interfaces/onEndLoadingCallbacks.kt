package com.logicasur.appchoferes.auth.otp.interfaces

import java.util.*

interface onEndLoadingCallbacks {
    fun endLoading()
    fun openPopup(myTimer: Timer)
    fun openServerPopup()
}