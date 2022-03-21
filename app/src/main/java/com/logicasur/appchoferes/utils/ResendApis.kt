package com.logicasur.appchoferes.utils

import android.app.ActivityManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startForegroundService
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.common.serverCheck.ServerCheck
import com.logicasur.appchoferes.common.serviceUploadOfflineActivities.ServiceUploadOfflineActivities
import com.logicasur.appchoferes.utils.myApplication.MyApplication
import kotlinx.coroutines.*
import java.util.*
import com.logicasur.appchoferes.common.loadingScreen.LoadingScreen
import java.lang.Exception


class ResendApis constructor(
    val serverCheck: ServerCheck,
    val tinyDB: TinyDB
) {
    companion object {
        var value = false
        var primaryColor = "#7A59FC"
        var secondaryColor = "#653FFB"
        const val splashToOtp = "splashToOtp"
    }


    var checkNetTimer: Timer? = null


    fun checkNetAndUpload() {
        tinyDB.putBoolean("PENDINGCHECK", true)
        if(checkNetTimer != null){
            try {
                checkNetTimer?.cancel()
                checkNetTimer?.purge()
            }catch (e : Exception){
                Log.d("SERVICE_TESTING","Exception in cancelling timer "+e.localizedMessage)
            }
        }
        checkNetTimer = Timer()
        checkNetTimer?.schedule(object : TimerTask() {
            override fun run() {
                Log.d("POPUP_ISSUE_TESTING","-------- ${MyApplication.syncCheck}")
                CoroutineScope(Job()).launch(Dispatchers.IO) {
                    serverCheck.serverCheck {
                        startService()
                    }


                    endIfLoadingIsStarted()
                }


            }
        }, 0, 10000)
    }

    fun startService() {
        CoroutineScope(Job()).launch(Dispatchers.IO) {
            cancelTimer()

            tinyDB.putBoolean("SYNC_CHECK", true)
            if (serverCheck.mainRepository.isExistsUnsentUploadActivityDB() && !isMyServiceRunning(
                    ServiceUploadOfflineActivities::class.java
                )
            ) {
                Log.d("ResendApis", "start Service")

                startForegroundService(
                    MyApplication.appContext,
                    ServiceUploadOfflineActivities.getStartIntent(
                        serverCheck,
                        tinyDB
                    )
                )
            }
        }


    }


//    fun checkStateAndUploadActivityDB() {
//        Log.d("PENDING_DATA_TESTING", "Call checkStateAndUploadActivityDB")
//        CoroutineScope(Job()).launch(Dispatchers.IO) {
//
//
//
//            if (serverCheck.mainRepository.isExistsUnsentUploadActivityDB()) {
//
//                for (unsentActivity in serverCheck.mainRepository.getUnsentUploadActivityDetails()) {
//
//                    val vehicle = Vehicle(
//                        0,
//                        unsentActivity.vehicleId,
//                        unsentActivity.vehicleDescription,
//                        unsentActivity.vehiclePlateNumber
//                    )
//                    val geoPosition = GeoPosition(
//                        unsentActivity.latitudeGeoPosition,
//                        unsentActivity.longitudeGeoPosition
//                    )
//
//                    if (unsentActivity.stateId == null) {
//
//                        uploadPendingDataActivity(
//                            unsentActivity.roomDBId,
//                            unsentActivity.dateTime,
//                            unsentActivity.totalTime,
//                            unsentActivity.activity,
//                            geoPosition,
//                            vehicle,
//                            serverCheck.authRepository
//                        )
//                    } else {
//                        val state = State(
//                            0,
//                            unsentActivity.stateId,
//                            unsentActivity.stateDescription!!
//                        )
//                        updateState(
//                            unsentActivity.roomDBId,
//                            unsentActivity.dateTime,
//                            unsentActivity.totalTime,
//                            state,
//                            geoPosition,
//                            vehicle
//                        )
//                    }
//                }
//                Log.d("PENDING_DATA_TESTING", "Activity UPDATE")
//                Log.d("PENDING_DATA_TESTING", "after Activity UPDATE")
//            }
//
//
//            checkForRemainingCalls()
//        }
//    }
//
//
//    private suspend fun uploadPendingDataActivity(
//        roomId: Int,
//        datetime: String?,
//        totalTime: Int?,
//        activity: Int?,
//        geoPosition: GeoPosition?,
//        vehicle: Vehicle?,
//        authRepository: AuthRepository
//    ) {
//
//        try {
//
//            tinyDB.getString("Cookie")?.let { token ->
//                val response = authRepository.updateActivity(
//                    datetime,
//                    totalTime,
//                    activity,
//                    geoPosition,
//                    vehicle,
//                    token
//                )
//                serverCheck.mainRepository.deleteUnsentUploadActivity(roomId)
//                println("SuccessResponse $response")
//            }
//
//        } catch (e: ResponseException) {
//
//            println("ErrorResponse")
//        } catch (e: ApiException) {
//            e.printStackTrace()
//        } catch (e: NoInternetException) {
//            println("position 2")
//            e.printStackTrace()
//        } catch (e: SocketTimeoutException) {
//
//        } catch (e: SocketException) {
//            Log.d("connection Exception", "Connect Not Available")
//        } catch (e: Exception) {
//            Log.d("connection Exception", "Connect Not Available")
//        }
//
//
//    }
//
//
//    private suspend fun updateState(
//        roomId: Int,
//        datetime: String?,
//        totalTime: Int?,
//        state: State?,
//        geoPosition: GeoPosition?,
//        vehicle: Vehicle?
//    ) {
//
//        tinyDB.getString("Cookie")?.let { token ->
//            try {
//
//                val response = serverCheck.authRepository.updateState(
//                    datetime,
//                    totalTime,
//                    state,
//                    geoPosition,
//                    vehicle,
//                    token
//                )
//                serverCheck.mainRepository.deleteUnsentUploadActivity(roomId)
//                println("SuccessResponse $response")
//
//
//            } catch (e: ResponseException) {
//
//                println("ErrorResponse")
//            } catch (e: ApiException) {
//                e.printStackTrace()
//            } catch (e: NoInternetException) {
//                println("position 2")
//                e.printStackTrace()
//            } catch (e: SocketTimeoutException) {
//
//            } catch (e: SocketException) {
//
//                Log.d("connection Exception", "Connect Not Available")
//
//            } catch (e: Exception) {
//                Log.d("connection Exception", "Connect Not Available")
//            }
//        }
//
//    }
//
//
//
//
//
//
//
/////----------------------- DB Operations ----------------------//
//
//    private suspend fun checkForRemainingCalls() {
//        if (!(serverCheck.mainRepository.isExistsUnsentUploadActivityDB())) {
//            checkNetTimer = null
//
//            tinyDB.putBoolean("PENDINGCHECK", false)
//            tinyDB.putBoolean("SYNC_CHECK", true)
//        } else {
//            serverCheck.serverCheck{
//                checkStateAndUploadActivityDB()
//            }
//        }
//    }


////////////---------------- Utils ----------------------------------


    private fun cancelTimer() {
        checkNetTimer?.let { timer ->
            tinyDB.putBoolean("PENDINGCHECK", false)
            timer.cancel()
            timer.purge()
            checkNetTimer = null
        }
    }

    private fun endIfLoadingIsStarted() {
        // If User  does not came from sync than exit loading screen
        if (MyApplication.checKForActivityLoading) {
            Log.d("LoadingEND", "----true----")
            LoadingScreen.OnEndLoadingCallbacks?.endLoading("From ResendApi")
            MyApplication.checKForActivityLoading = false
        }
    }


    fun Job.status(): String = when {
        isActive -> "Active/Completing"
        isCompleted && isCancelled -> "Cancelled"
        isCancelled -> "Cancelling"
        isCompleted -> "Completed"
        else -> "New"
    }

    fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager =
            MyApplication.appContext.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


}


