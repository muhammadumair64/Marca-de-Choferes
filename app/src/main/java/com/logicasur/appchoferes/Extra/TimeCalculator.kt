package com.logicasur.appchoferes.Extra

import android.content.Context
import android.util.Log
import com.logicasur.appchoferes.mainscreen.MainActivity
import com.logicasur.appchoferes.myApplication.MyApplication
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule

class TimeCalculator {



    fun timeDifference(tinyDB: TinyDB, context: Context, resumeCheck: Boolean, workBreak: Int) {


        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        var currentDate = sdf.format(Date())
        Log.d("check_the_time_i_store ", "$currentDate")
        var datetest2 = getDateFromString("2022/02/15 01:10:06")


        var lastTimetoGo = tinyDB.getString("goBackTime")
        var datetest1 = getDateFromString("2022/02/13 23:16:06")

        Log.d("check_the_time_i_store ", "$lastTimetoGo")
        var finalTimeDiff = printDifference(datetest1, datetest2)
        Log.d("TIME_TESTING", " final test $finalTimeDiff")

        MyApplication.backPressCheck = 200


        var checkTimer = tinyDB.getString("checkTimer")
        if (checkTimer == "workTime") {
            if (resumeCheck) {
                var previoustime = tinyDB.getInt("lasttimework")
                Log.d("TimerTESTING", "1----- $previoustime")
                var newTime = previoustime + finalTimeDiff
                tinyDB.putInt("lasttimework", newTime.toInt())
                Log.d("TimerTESTING", "2---- $newTime")
                var intent = (context as MainActivity)
                intent.setTimer()
                Timer().schedule(200) {
                    intent.startTimer()
                }
            } else {
                Log.d("TimerTESTING", "1.1.1----- $finalTimeDiff")
                tinyDB.putInt("lasttimework", finalTimeDiff.toInt())
            }


        } else if (checkTimer == "breakTime") {
            if (resumeCheck) {
                var previoustime = tinyDB.getInt("lasttimebreak")
                var newTime = previoustime + finalTimeDiff
                Log.d("TimerTESTING", "1.1----- $previoustime")
                var defaultBreak = workBreak * 60
                if (newTime > defaultBreak) {
                    newTime = defaultBreak.toLong()
                }
                tinyDB.putInt("lasttimebreak", newTime.toInt())
                Log.d("TimerTESTING", "2.1----- $newTime ")
                var intent = (context as MainActivity)
                intent.setTimer()
                Timer().schedule(200) {
                    intent.startTimerBreak()
                }
            } else {
                var serverBreak = tinyDB.getInt("ServerBreakTime")
                finalTimeDiff += serverBreak
                tinyDB.putInt("lasttimebreak", finalTimeDiff.toInt())
            }


        }


    }


    //1 minute = 60 seconds
    //1 hour = 60 x 60 = 3600
    //1 day = 3600 x 24 = 86400
    open fun printDifference(startDate: Date, endDate: Date): Long {
        //milliseconds
        var different = endDate.time - startDate.time
        println("startDate : $startDate")
        println("endDate : $endDate")
        println("different : $different")
        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24
        val elapsedDays = different / daysInMilli
        different = different % daysInMilli
        val elapsedHours = different / hoursInMilli
        different = different % hoursInMilli
        val elapsedMinutes = different / minutesInMilli
        different = different % minutesInMilli
        val elapsedSeconds = different / secondsInMilli
        System.out.printf(
            "%d days, %d hours, %d minutes, %d seconds%n",
            elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds
        )
        Log.d(
            "TESTING_TIME",
            "    $elapsedDays, $elapsedHours, $elapsedMinutes, $elapsedSeconds"
        )

        var dayInSec = elapsedDays * 86400
        var hourSEC = elapsedHours * 3600
        var minutesSEC = elapsedMinutes * 60
        var finalTimeInSec = hourSEC + minutesSEC + elapsedSeconds + dayInSec



        return finalTimeInSec
    }

    fun getDateFromString(dateStr: String): Date {
        val formatter: DateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
        return formatter.parse(dateStr) as Date
    }

}