package com.logicasur.appchoferes.mainscreen.home.timerServices

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.myApplication.MyApplication
import java.util.*

class BreakTimerService : Service()
{
    override fun onBind(p0: Intent?): IBinder? = null

    private val timer = Timer()
    lateinit var tinyDB:TinyDB
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        tinyDB = TinyDB(MyApplication.appContext)
        var workTime=tinyDB.getInt("lasttimebreak")
        val time = intent.getDoubleExtra(TIME_EXTRA_B, workTime.toDouble())
        timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
        return START_NOT_STICKY
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
            val intent = Intent(TIMER_UPDATED_B)
            time++
            intent.putExtra(TIME_EXTRA_B, time)
            sendBroadcast(intent)
        }
    }

    companion object
    {
        const val TIMER_UPDATED_B = "timerUpdatedB"
        const val TIME_EXTRA_B = "timeExtraB"
    }

}