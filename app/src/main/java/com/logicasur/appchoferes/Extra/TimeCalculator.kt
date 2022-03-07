package com.logicasur.appchoferes.Extra

import android.content.Context
import android.util.Log
import com.logicasur.appchoferes.mainscreen.MainActivity
import com.logicasur.appchoferes.mainscreen.repository.MainRepository
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.signinResponse.LastVar
import com.logicasur.appchoferes.network.signinResponse.SigninResponse
import com.logicasur.appchoferes.network.wrongData.wrongDataReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule

class TimeCalculator constructor(var mainRepository: MainRepository) {


    fun timeDifference(
        tinyDB: TinyDB,
        context: Context,
        resumeCheck: Boolean,
        workBreak: Int,
        response: SigninResponse?
    ) {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        var currentDate = sdf.format(Date())
        Log.d("check_the_time_i_store ", "$currentDate")
        var currentTime = getDateFromString(currentDate)


        var lastTimetoGo = tinyDB.getString("goBackTime")
        var lastTime = getDateFromString(lastTimetoGo.toString())

        Log.d("check_the_time_i_store ", "$lastTimetoGo")
        var finalTimeDiff = printDifference(lastTime, currentTime)
        Log.d("TIME_TESTING", " final test $finalTimeDiff")
        if (finalTimeDiff < 0) {
            Log.d("TimeTesting", "in Negative")
            if(response != null){
                storeWrongData(response)
            }

            var checkTimer = tinyDB.getString("checkTimer")
            if (checkTimer == "workTime") {
                var lastActivityDate = tinyDB.getString("ActivityDate")
                if(lastActivityDate != null){
                   lastActivityDate= lastActivityDate.replace("-","/")
                    var time = getDateFromString(lastActivityDate)
                    finalTimeDiff = printDifference(time, currentTime)
                }else{
                    finalTimeDiff = 0
                }

            } else {

                var lastActivityDate = tinyDB.getString("BreakDate")
                if(lastActivityDate != null ){
                    lastActivityDate= lastActivityDate.replace("-","/")
                    var time = getDateFromString(lastActivityDate)
                    finalTimeDiff = printDifference(time, currentTime)
                }else{
                    finalTimeDiff = 0
                }

            }

        }
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
        val formatter: DateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        return formatter.parse(dateStr) as Date
    }


    fun storeWrongData(response: SigninResponse?) {
        CoroutineScope(Job()).launch(Dispatchers.IO) {
            if (response?.lastVar != null) {
                response.lastVar.apply {
                    var lastVar = wrongDataReport(
                        0, response.profile!!.name,
                        LastWorkBreaklatitud,
                        lastActivity,
                        lastState,
                        lastStateDate,
                        lastStateLatitud,
                        lastStateLongitud,
                        lastWorkBreakDateEnd,
                        lastWorkBreakDateIni,
                        lastWorkBreakLongitud,
                        lastWorkBreakTotal,
                        lastWorkedHoursDateEnd,
                        lastWorkedHoursDateIni,
                        lastWorkedHoursLatitud,
                        lastWorkedHoursLongitud,
                        lastWorkedHoursTotal
                    )
                    mainRepository.insertWrongDataReport(lastVar)
                }

            }


        }
    }
}