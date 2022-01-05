package com.example.marcadechoferes.Extra

import android.content.Context
import android.util.Log
import com.example.marcadechoferes.mainscreen.MainActivity
import com.example.marcadechoferes.myApplication.MyApplication
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.min

class K {
    companion object {
        var primaryColor = "#7A59FC"
        var secondrayColor = "#653FFB"

        fun timeDifference(tinyDB: TinyDB, context: Context) {

            var lastTimetoGo = tinyDB.getString("goBackTime")

             Log.d("check the time i store ", "$lastTimetoGo")

            val simpleDateFormat = SimpleDateFormat("HH:mm:ss")

            val sdf = SimpleDateFormat("HH:mm:ss")
            val currentDate = sdf.format(Date())

            var date1 = simpleDateFormat.parse(lastTimetoGo)
            var date2 = simpleDateFormat.parse(currentDate)

            val difference: Long = date2.getTime() - date1.getTime()
            var days = (difference / (1000 * 60 * 60 * 24)).toInt()
            var hours = ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60)) as Long
            var min =
                (difference - 1000 * 60 * 60 * 24 * days - 1000 * 60 * 60 * hours) as Long / (1000 * 60)
            val sec =
                (difference - 1000 * 60 * 60 * 24 * days - 1000 * 60 * 60 * hours - 1000 * 60 * min).toInt() / 1000
            hours = if (hours < 0) -hours else hours
            println("======= Hours :: $hours   &&&&  $min   &&& $sec")
            var check: Long = 0
            var hoursInSec: Long = 0
            var mintInSec: Long = 0

            if (hours != check) {
                hoursInSec = hours * 3600
            }
            if (min != check) {
                mintInSec = min * 60
            }

            var finalTimeDiff = hoursInSec + mintInSec + sec

            println("final time difference in sec is == $finalTimeDiff")
            Log.d("finalssss time difference in sec is","$finalTimeDiff")
            MyApplication.backPressCheck=200

            //check which time is need to set

            var intent= (context as MainActivity)

            var checkTimer = tinyDB.getString("checkTimer")
            if(checkTimer=="workTime"){
                var previoustime=tinyDB.getInt("lasttimework")
                var newTime = previoustime+finalTimeDiff
                tinyDB.putInt("lasttimework",newTime.toInt())
                intent.setTimer()
                Timer().schedule(200) {
                    intent.startTimer()
                }

            }
            else if(checkTimer=="breakTime"){
                var previoustime=tinyDB.getInt("lasttimebreak")
                var newTime = previoustime+finalTimeDiff
                tinyDB.putInt("lasttimebreak",newTime.toInt())
                intent.setTimer()
                Timer().schedule(200) {
                    intent.startTimerBreak()
                }

            }



        }


    }
}