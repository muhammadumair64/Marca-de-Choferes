package com.example.marcadechoferes.myApplication

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        MyApplication.appContext = applicationContext
        MyApplication.loadingContext=applicationContext
    }

    companion object {

        lateinit  var appContext: Context
lateinit var loadingContext:Context
    }
}