package com.logicasur.appchoferes.myApplication

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import kotlin.properties.Delegates

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        loadingContext = applicationContext
        activityContext = applicationContext
        check = 0
        TotalTime = 0
        TotalBreak = 0
        navBarSelection = 0
        TimeToSend = 0
        BreakToSend = 0
        checkForLanguageChange = 0
        checkForResume = 0
        dayEndCheck = 0
        backPressCheck = 0
        breakBarProgress = 0
        checKForPopup = false
        checKForActivityLoading = false
        checKForSyncLoading = false
        authCheck = false
        isExistInDB = false
    }

    companion object {

        lateinit var appContext: Context
        lateinit var loadingContext: Context
        lateinit var activityContext: Context
        var check by Delegates.notNull<Int>()
        var TotalTime by Delegates.notNull<Int>()
        var navBarSelection by Delegates.notNull<Int>()
        var TotalBreak by Delegates.notNull<Int>()
        var TimeToSend by Delegates.notNull<Int>()
        var BreakToSend by Delegates.notNull<Int>()
        var checkForLanguageChange by Delegates.notNull<Int>()
        var checkForResume by Delegates.notNull<Int>()
        var backPressCheck by Delegates.notNull<Int>()
        var dayEndCheck by Delegates.notNull<Int>()
        var breakBarProgress by Delegates.notNull<Int>()
        var checKForPopup by Delegates.notNull<Boolean>()
        var checKForActivityLoading by Delegates.notNull<Boolean>()
        var checKForSyncLoading by Delegates.notNull<Boolean>()
        var authCheck by Delegates.notNull<Boolean>()
        var isExistInDB by Delegates.notNull<Boolean>()
    }


}