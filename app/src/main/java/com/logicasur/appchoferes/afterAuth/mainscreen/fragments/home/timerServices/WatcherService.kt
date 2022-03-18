package com.logicasur.appchoferes.afterAuth.mainscreen.fragments.home.timerServices

import android.app.Service
import android.content.Intent
import android.os.IBinder

class WatcherService: Service()  {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        return START_NOT_STICKY
    }

    override fun onDestroy() {
        println("Watch service is stopped in on destroy")
        super.onDestroy()

    }

    override fun onCreate() {
        super.onCreate()
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        println("Watch service is stopped")
    }




}