package com.example.marcadechoferes.myApplication

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import com.example.marcadechoferes.mainscreen.home.timerServices.BreakTimerService
import com.example.marcadechoferes.mainscreen.home.timerServices.TimerService
import dagger.hilt.android.HiltAndroidApp
import kotlin.properties.Delegates

@HiltAndroidApp
class MyApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        MyApplication.appContext = applicationContext
        MyApplication.loadingContext=applicationContext
        MyApplication.check=0
        MyApplication.TotalTime=0
        MyApplication.TotalBreak=0
        MyApplication.navBarSelection=0
        MyApplication.TimeToSend=0
        MyApplication.BreakToSend=0
        MyApplication.checkForLanguageChange=0
        MyApplication.checkForResume=0
        MyApplication.backPressCheck=0
    }

    companion object {

        lateinit  var appContext: Context
        lateinit var loadingContext:Context
        var check by Delegates.notNull<Int>()
        var TotalTime  by Delegates.notNull<Int>()
        var navBarSelection by Delegates.notNull<Int>()
        var TotalBreak by Delegates.notNull<Int>()
        var TimeToSend by Delegates.notNull<Int>()
        var BreakToSend  by Delegates.notNull<Int>()
        var checkForLanguageChange by Delegates.notNull<Int>()
        var checkForResume by Delegates.notNull<Int>()
        var backPressCheck by Delegates.notNull<Int>()
    }



}