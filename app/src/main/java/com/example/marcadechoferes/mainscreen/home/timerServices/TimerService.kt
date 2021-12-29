package com.example.marcadechoferes.mainscreen.home.timerServices

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.myApplication.MyApplication
import java.util.*

class TimerService : Service()
{
    override fun onBind(p0: Intent?): IBinder? = null

    private val timer = Timer()
    lateinit var tinyDB: TinyDB

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        tinyDB = TinyDB(MyApplication.appContext)
        var workTime=tinyDB.getInt("lasttimework")
        val time = intent.getDoubleExtra(TIME_EXTRA,workTime.toDouble())
        timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
        return START_STICKY
    }

    override fun onDestroy()
    {
        println("i am here 012")
        timer.cancel()
        super.onDestroy()
    }

    private inner class TimeTask(private var time: Double) : TimerTask()
    {
        override fun run()
        {
            val intent = Intent(TIMER_UPDATED)
            time++
            intent.putExtra(TIME_EXTRA, time)
            sendBroadcast(intent)
        }
    }

    companion object
    {
        const val TIMER_UPDATED = "timerUpdated"
        const val TIME_EXTRA = "timeExtra"
    }


}