package com.logicasur.appchoferes.mainscreen.home.timerServices

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.mainscreen.MainActivity
import com.logicasur.appchoferes.myApplication.MyApplication
import java.util.*

class TimerService : Service()
{

    override fun onBind(p0: Intent?): IBinder? = null

    private val timer = Timer()
    lateinit var tinyDB: TinyDB


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        tinyDB = TinyDB(MyApplication.appContext)
        var workTime=tinyDB.getInt("lasttimework")
        if(intent != null){
            val time = intent.getDoubleExtra(TIME_EXTRA,workTime.toDouble())
            timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
        }

        return START_STICKY
    }

    override fun onDestroy()
    {
        println("i am here 012")
        timer.cancel()
        try{
            (MyApplication.activityContext as MainActivity).checkScreen()
        }
        catch (e:Exception){
            Log.d("Testing","stop")
        }

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