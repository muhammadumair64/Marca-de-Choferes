package com.example.marcadechoferes.myApplication

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
        MyApplication.check=0
        MyApplication.TotalTime=0
        MyApplication.TotalBreak=0
    }

    companion object {

        lateinit  var appContext: Context
lateinit var loadingContext:Context
        var check by Delegates.notNull<Int>()
        var TotalTime  by Delegates.notNull<Int>()
        var TotalBreak by Delegates.notNull<Int>()
    }
}