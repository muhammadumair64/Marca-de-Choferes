package com.logicasur.appchoferes.Extra

import android.content.Context
import android.util.Log
import com.logicasur.appchoferes.auth.repository.AuthRepository
import com.logicasur.appchoferes.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.mainscreen.MainActivity
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.ApiException
import com.logicasur.appchoferes.network.GeoPosition
import com.logicasur.appchoferes.network.NoInternetException
import com.logicasur.appchoferes.network.ResponseException
import com.logicasur.appchoferes.network.signinResponse.State
import com.logicasur.appchoferes.network.signinResponse.Vehicle
import kotlinx.coroutines.*
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class K {
    companion object {
         lateinit var authRepository:AuthRepository
         var arrayList :ArrayList<UpdateActivityDataClass> = ArrayList()
         var tinyDB:TinyDB = TinyDB(MyApplication.appContext)

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
            Log.d("finalssss time difference in sec is","$finalTimeDiff")
            MyApplication.backPressCheck=200

            //check which time is need to set



            var checkTimer = tinyDB.getString("checkTimer")
            if(checkTimer=="workTime"){
                if(resumeCheck==true){
                    var previoustime=tinyDB.getInt("lasttimework")
                    var newTime = previoustime+finalTimeDiff
                    tinyDB.putInt("lasttimework",newTime.toInt())
                    var intent= (context as MainActivity)
                    intent.setTimer()
                Timer().schedule(200) {
                    intent.startTimer()
                }
                }else{
                    tinyDB.putInt("lasttimework",finalTimeDiff.toInt())
                }
//

            }
            else if(checkTimer=="breakTime"){
                if(resumeCheck==true){
                    var previoustime=tinyDB.getInt("lasttimebreak")
                    var newTime = previoustime+finalTimeDiff
                    var defaultBreak=workBreak * 60
                    if(newTime > defaultBreak){
                        newTime = defaultBreak.toLong()
                    }
                    tinyDB.putInt("lasttimebreak",newTime.toInt())
                    var intent= (context as MainActivity)
                    intent.setTimer()
                    Timer().schedule(200) {
                        intent.startTimerBreak()
                    }
                }else{
                 var serverBreak=tinyDB.getInt("ServerBreakTime")
                    finalTimeDiff= finalTimeDiff+serverBreak
//                    var defaultBreak=workBreak * 60
//                    if(finalTimeDiff > defaultBreak){
//                        finalTimeDiff = defaultBreak.toLong()
//                    }
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

            val command = "ping -c 1 google.com"
            return Runtime.getRuntime().exec(command).waitFor() == 0
        }


        fun checkNet(){
            tinyDB.putBoolean("PENDINGCHECK",true)
            var myTimer = Timer()
            myTimer.schedule(object : TimerTask() {
                override fun run() {
                    var netCheck=isConnected()
                    if(netCheck){
                      checkPendingData(tinyDB,myTimer)
                    }
                    Log.d("NETCHECKTEST","---- $netCheck")
                }
            }, 0, 10000)
        }


        fun checkPendingData(tinyDB: TinyDB, myTimer: Timer){
            arrayList = tinyDB.getListObject("PENDINGDATALIST",UpdateActivityDataClass::class.java) as ArrayList<UpdateActivityDataClass>

                CoroutineScope(Job()).launch(Dispatchers.IO) {
                for(item in arrayList){
                    if(item.state != null){
                        Log.d("PENDINGDATATESTING_STATE","DATA IS____ $item")
                        updateState(item.datetime,item.totalTime,item.state,item.geoPosition,item.vehicle)
                    }else{
                        Log.d("PENDINGDATATESTING","DATA IS____ $item")
                        uploadPendingData(item.datetime,item.totalTime,item.activity,item.geoPosition,item.vehicle,
                            authRepository)
                    }


                }
                tinyDB.putBoolean("PENDINGCHECK",false)
                myTimer.cancel()
                    arrayList.clear()
                    tinyDB.putListObject("PENDINGDATALIST", arrayList as ArrayList<Object>)
                        Log.d("PENDINGDATATESTING","YES NOW RUN")
                    tinyDB.putBoolean("SYNC_CHECK",true)
            }


        }

       suspend fun uploadPendingData(datetime: String?,
                              totalTime: Int?,
                              activity: Int?,
                              geoPosition: GeoPosition?,
                              vehicle: Vehicle?,
                              authRepository: AuthRepository
        ){


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
                    println("SuccessResponse $response")


                    if (response != null) {
                        withContext(Dispatchers.Main) {
                            (MyApplication.loadingContext as LoadingScreen).finish()
                        }
                    }

                } catch (e: ResponseException) {

                    println("ErrorResponse")
                } catch (e: ApiException) {
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {

                    }
                }
                catch (e: SocketTimeoutException){
                    withContext(Dispatchers.Main) {
                    }
                }
                catch(e: SocketException){
                    withContext(Dispatchers.Main) {

                    }
                    Log.d("connection Exception","Connect Not Available")
                }


        }



        suspend fun updateState(
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
        }


