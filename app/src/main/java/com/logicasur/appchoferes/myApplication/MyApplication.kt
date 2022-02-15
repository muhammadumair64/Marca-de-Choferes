package com.logicasur.appchoferes.myApplication

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import kotlin.properties.Delegates

@HiltAndroidApp
class MyApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        MyApplication.appContext = applicationContext
        MyApplication.loadingContext=applicationContext
        MyApplication.activityContext=applicationContext
        MyApplication.check=0
        MyApplication.TotalTime=0
        MyApplication.TotalBreak=0
        MyApplication.navBarSelection=0
        MyApplication.TimeToSend=0
        MyApplication.BreakToSend=0
        MyApplication.checkForLanguageChange=0
        MyApplication.checkForResume=0
        MyApplication.dayEndCheck = 0
        MyApplication.backPressCheck=0
        MyApplication.breakBarProgress=0
        MyApplication.checKForPopup=false
        MyApplication.checKForActivityLoading = false
    }

    companion object {

        lateinit  var appContext: Context
        lateinit var loadingContext:Context
        lateinit var activityContext:Context
        var check by Delegates.notNull<Int>()
        var TotalTime  by Delegates.notNull<Int>()
        var navBarSelection by Delegates.notNull<Int>()
        var TotalBreak by Delegates.notNull<Int>()
        var TimeToSend by Delegates.notNull<Int>()
        var BreakToSend  by Delegates.notNull<Int>()
        var checkForLanguageChange by Delegates.notNull<Int>()
        var checkForResume by Delegates.notNull<Int>()
        var backPressCheck by Delegates.notNull<Int>()
         var dayEndCheck by Delegates.notNull<Int>()
        var breakBarProgress by Delegates.notNull<Int>()
        var checKForPopup by Delegates.notNull<Boolean>()
        var checKForActivityLoading by Delegates.notNull<Boolean>()
    }



}