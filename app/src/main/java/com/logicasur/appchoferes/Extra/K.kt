package com.logicasur.appchoferes.Extra

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.logicasur.appchoferes.Extra.serverCheck.ServerCheck
import com.logicasur.appchoferes.auth.repository.AuthRepository
import com.logicasur.appchoferes.mainscreen.MainActivity
import com.logicasur.appchoferes.mainscreen.repository.MainRepository
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.ApiException
import com.logicasur.appchoferes.network.GeoPosition
import com.logicasur.appchoferes.network.NoInternetException
import com.logicasur.appchoferes.network.ResponseException
import com.logicasur.appchoferes.network.signinResponse.State
import com.logicasur.appchoferes.network.signinResponse.Vehicle
import kotlinx.coroutines.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class K {
    companion object {
        lateinit var authRepository: AuthRepository
        lateinit var mainRepository: MainRepository
        var arrayList: ArrayList<UpdateActivityDataClass> = ArrayList()
        var tinyDB: TinyDB = TinyDB(MyApplication.appContext)
        var myTimer: Timer? = null
        var primaryColor = "#7A59FC"
//            get() {
//                return tinyDB.getString("primaryColor") ?: "#7A59FC"
//            }

        var secondrayColor = "#653FFB"
//            get() {
//                return tinyDB.getString("secondrayColor") ?: "#653FFB"
//            }

        const val splashToOtp = "splashToOtp"

        fun timeDifference(tinyDB: TinyDB, context: Context, resumeCheck: Boolean, workBreak: Int) {

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
            Log.d("finalssss time difference in sec is", "$finalTimeDiff")
            MyApplication.backPressCheck = 200

            //check which time is need to set


            var checkTimer = tinyDB.getString("checkTimer")
            if (checkTimer == "workTime") {
                if (resumeCheck == true) {
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
//

            } else if (checkTimer == "breakTime") {
                if (resumeCheck == true) {
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
                    finalTimeDiff = finalTimeDiff + serverBreak
//                    var defaultBreak=workBreak * 60
//                    if(finalTimeDiff > defaultBreak){
//                        finalTimeDiff = defaultBreak.toLong()
//                    }8
                    tinyDB.putInt("lasttimebreak", finalTimeDiff.toInt())
                }
//                intent.setTimer()
//                Timer().schedule(200) {
//                    intent.startTimerBreak()
//                }

            }


        }



        @Throws(InterruptedException::class, IOException::class)
        fun isConnected(): Boolean {
            val command = "ping -i 1 -c 1 google.com"
            var value = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                value= Runtime.getRuntime().exec(command).waitFor(2,TimeUnit.SECONDS)
            }else{
                 value = Runtime.getRuntime().exec(command).waitFor()==0
            }
            return value

        }


//        fun isConnected2(context: Context): Boolean {
//            val cm = context
//                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            val activeNetwork = cm.activeNetworkInfo
//            return if (activeNetwork != null && activeNetwork.isConnected) {
//                try {
//                    val url = URL("https://www.google.com/")
//                    val urlc: HttpURLConnection = url.openConnection() as HttpURLConnection
//                    urlc.setRequestProperty("User-Agent", "test")
//                    urlc.setRequestProperty("Connection", "close")
//                    urlc.setConnectTimeout(2000) // mTimeout is in seconds
//                    urlc.connect()
//                    if (urlc.getResponseCode() == 200) {
//                        Log.d("Testing_net","Here")
//                        true
//                    } else {
//                        false
//                    }
//                } catch (e: IOException) {
//                    Log.i("warning", "Error checking internet connection", e)
//                    false
//                }
//            } else false
//        }

        fun checkNet() {


            tinyDB.putBoolean("PENDINGCHECK", true)
            myTimer = Timer()
            myTimer!!.schedule(object : TimerTask() {

                override fun run() {
                    var netCheck = isConnected()
                    if (netCheck) {
//                      checkPendingData(tinyDB,myTimer!!)
                        //faris start work here
                        CoroutineScope(Job()).launch(Dispatchers.IO) {

//                            val coroutineJob = Job()
//                            CoroutineScope(coroutineJob).launch(Dispatchers.IO) {
                            ServerCheck.serverCheck(null) {   checkStateAndUploadActivityDB() }
//                                checkStateAndUploadActivityDB()
                            //}
                            Log.d("PENDING_DATA_TESTING","before")
//                            coroutineJob.join()
                            Log.d("PENDING_DATA_TESTING","After")
//                            checkUpdateLanguageNotifyState()
                        }
                    }
                    Log.d("NETCHECKTEST", "---- $netCheck")
                }
            }, 0, 10000)
        }


         fun checkStateAndUploadActivityDB() {
            Log.d("PENDING_DATA_TESTING","Call checkStateAndUploadActivityDB")
             CoroutineScope(Job()).launch(Dispatchers.IO) {


                 myTimer!!.cancel()
                 if (mainRepository.isExistsUnsentStateUpdateDB()) {

                     val getUnsentStateUpdateValues =
                         mainRepository.getUnsentStateUpdateDetails().toCollection(ArrayList())


                     for (getUnsentStateData in getUnsentStateUpdateValues) {

                         val getState = State(
                             0,
                             getUnsentStateData.stateId,
                             getUnsentStateData.stateDescription
                         )
                         val getVehicle = Vehicle(
                             0,
                             getUnsentStateData.vehicleId,
                             getUnsentStateData.vehicleDescription,
                             getUnsentStateData.vehiclePlateNumber
                         )
                         val getGeoPosition = GeoPosition(
                             getUnsentStateData.latitudeGeoPosition,
                             getUnsentStateData.longitudeGeoPosition
                         )


                         updateState(
                             getUnsentStateData.roomDBId,
                             getUnsentStateData.datetime,
                             getUnsentStateData.totalTime,
                             getState,
                             getGeoPosition,
                             getVehicle
                         )
                     }
                     Log.d("PENDING_DATA_TESTING", "STATE UPDATE")

                     Log.d("PENDING_DATA_TESTING", "AFter STATE UPDATE")


                 }
                 if (mainRepository.isExistsUnsentUploadActivityDB()) {


                     val getUnsentUploadActivityValues =
                         mainRepository.getUnsentUploadActivityDetails().toCollection(ArrayList())

                     for (getUnsentUploadActivityData in getUnsentUploadActivityValues) {


                         val getVehicle = Vehicle(
                             0,
                             getUnsentUploadActivityData.vehicleId,
                             getUnsentUploadActivityData.vehicleDescription,
                             getUnsentUploadActivityData.vehiclePlateNumber
                         )
                         val getGeoPosition = GeoPosition(
                             getUnsentUploadActivityData.latitudeGeoPosition,
                             getUnsentUploadActivityData.longitudeGeoPosition
                         )

                         uploadPendingDataActivity(
                             getUnsentUploadActivityData.roomDBId,
                             getUnsentUploadActivityData.dateTime,
                             getUnsentUploadActivityData.totalTime,
                             getUnsentUploadActivityData.activity,
                             getGeoPosition,
                             getVehicle,
                             authRepository
                         )
                     }
                     Log.d("PENDING_DATA_TESTING", "Activity UPDATE")
//                   coroutineUploadActivity.join()
                     Log.d("PENDING_DATA_TESTING", "after Activity UPDATE")
                 }


                 if (!(mainRepository.isExistsUnsentStateUpdateDB()) && !(mainRepository.isExistsUnsentUploadActivityDB())) {
                     tinyDB.putBoolean("PENDINGCHECK", false)
                     tinyDB.putBoolean("SYNC_CHECK", true)
                 } else {
                     ServerCheck.serverCheck(null) {
                         checkStateAndUploadActivityDB()
                     }
                 }

             }
        }


        suspend fun checkUpdateLanguageNotifyState() {
            if (mainRepository.isExistsUpdateLanguageDB()) {
                val getUnsentLanguageUpdateValue = mainRepository.getUnsentLanguageUpdateDetails()
            }
            if (mainRepository.isExistsUnsentNotifyStateUploadDB()) {
                val getUnsentNotifyStateUploadValue =
                    mainRepository.getUnsentNotifyStateUploadDetails()
            }
        }


//        fun checkPendingData(tinyDB: TinyDB, myTimer: Timer) {
//            arrayList = tinyDB.getListObject(
//                "PENDINGDATALIST",
//                UpdateActivityDataClass::class.java
//            ) as ArrayList<UpdateActivityDataClass>
//            myTimer.cancel()
//            CoroutineScope(Job()).launch(Dispatchers.IO) {
//                for (item in arrayList) {
//                    if (item.state != null) {
//                        Log.d("PENDINGDATATESTING_STATE", "DATA IS____ $item")
//                        updateState(
//                            item.datetime,
//                            item.totalTime,
//                            item.state,
//                            item.geoPosition,
//                            item.vehicle
//                        )
//                    } else {
//                        Log.d("PENDINGDATATESTING", "DATA IS____ $item")
//                        uploadPendingDataActivity(
//                            item.datetime,
//                            item.totalTime,
//                            item.activity,
//                            item.geoPosition,
//                            item.vehicle,
//                            authRepository
//                        )
//                    }
//                }
//                tinyDB.putBoolean("PENDINGCHECK", false)
//
//                arrayList.clear()
//                tinyDB.putListObject("PENDINGDATALIST", arrayList as ArrayList<Object>)
//                Log.d("PENDINGDATATESTING", "YES NOW RUN")
//                tinyDB.putBoolean("SYNC_CHECK", true)
//
//            }
//
//        }

        suspend fun uploadPendingDataActivity(
            roomId: Int,
            datetime: String?,
            totalTime: Int?,
            activity: Int?,
            geoPosition: GeoPosition?,
            vehicle: Vehicle?,
            authRepository: AuthRepository
        ) {


            try {
                var Token = tinyDB.getString("Cookie")
                val response = authRepository.updateActivity(
                    datetime,
                    totalTime,
                    activity,
                    geoPosition,
                    vehicle,
                    Token!!
                )
                mainRepository.deleteUnsentUploadActivity(roomId)
                println("SuccessResponse $response")


            } catch (e: ResponseException) {

                println("ErrorResponse")
            } catch (e: ApiException) {
                e.printStackTrace()
            } catch (e: NoInternetException) {
                println("position 2")
                e.printStackTrace()
                withContext(Dispatchers.Main) {

                }
            } catch (e: SocketTimeoutException) {
                withContext(Dispatchers.Main) {
                }
            } catch (e: SocketException) {
                withContext(Dispatchers.Main) {

                }
                Log.d("connection Exception", "Connect Not Available")
            }


        }


        suspend fun updateState(
            roomId: Int,
            datetime: String?,
            totalTime: Int?,
            state: State?,
            geoPosition: GeoPosition?,
            vehicle: Vehicle?
        ) {

            var Token = tinyDB.getString("Cookie")

            try {

                val response = authRepository.updateState(
                    datetime,
                    totalTime,
                    state,
                    geoPosition,
                    vehicle,
                    Token!!
                )
                mainRepository.deleteUnsentStateUpdate(roomId)


                println("SuccessResponse $response")


            } catch (e: ResponseException) {

                println("ErrorResponse")
            } catch (e: ApiException) {
                e.printStackTrace()
            } catch (e: NoInternetException) {
                println("position 2")
                e.printStackTrace()
            } catch (e: SocketTimeoutException) {

            } catch (e: SocketException) {

                Log.d("connection Exception", "Connect Not Available")

            }
        }
    }


    fun Job.status(): String = when {
        isActive -> "Active/Completing"
        isCompleted && isCancelled -> "Cancelled"
        isCancelled -> "Cancelling"
        isCompleted -> "Completed"
        else -> "New"
    }
}


