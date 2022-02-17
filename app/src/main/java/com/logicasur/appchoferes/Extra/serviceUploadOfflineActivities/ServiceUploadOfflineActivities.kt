package com.logicasur.appchoferes.Extra.serviceUploadOfflineActivities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.Extra.serverCheck.ServerCheck
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.auth.repository.AuthRepository
import com.logicasur.appchoferes.mainscreen.repository.MainRepository
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.ApiException
import com.logicasur.appchoferes.network.GeoPosition
import com.logicasur.appchoferes.network.NoInternetException
import com.logicasur.appchoferes.network.ResponseException
import com.logicasur.appchoferes.network.signinResponse.State
import com.logicasur.appchoferes.network.signinResponse.Vehicle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.SocketException
import java.net.SocketTimeoutException

class ServiceUploadOfflineActivities : Service() {

    private var manager: NotificationManager? = null
    private var notification: Notification? = null
    private var notificationBuilder: Notification.Builder? = null
    private var notificationBuilderLowerVersion: NotificationCompat.Builder? = null
    var percentageValue = 0
    var sizeOfDbData=0
    var increaseIndex=0



    companion object {
        lateinit var serverCheck: ServerCheck
        lateinit var tinyDB: TinyDB
        fun getStartIntent(

            serverCheck: ServerCheck,
            tinyDB: TinyDB

        ): Intent {
            this.serverCheck = serverCheck
            this.tinyDB = tinyDB
            return Intent(MyApplication.appContext, ServiceUploadOfflineActivities::class.java)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        CoroutineScope(Job()).launch {
            getNotification(MyApplication.appContext, 101)
        }

        checkStateAndUploadActivityDB()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        manager?.cancelAll()
        super.onDestroy()
    }

    // -----------------Create Notification----------

    private fun getNotification(
        context: Context,
        id: Int
    ): Pair<Int, Notification>? {
        createNotificationChannel(context)
        notification = createNotification(context)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, notification)
        return Pair(id, notification!!)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                101.toString(),
                "MY BROADCAST MESSAGE",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            manager?.createNotificationChannel(channel)

        }
    }

    private fun createNotification(context: Context): Notification {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = Notification.Builder(context, 101.toString())
                .setSmallIcon(R.mipmap.app_icon)
                .setContentTitle("Upload Offline activities")
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setProgress(100, percentageValue, false)
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
            notificationBuilder!!.build()


        } else {
            notificationBuilderLowerVersion = NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.app_icon)
                .setContentTitle("Upload Offline activities")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setProgress(100, percentageValue, false)
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
            notificationBuilderLowerVersion!!.build()
        }


    }

    fun checkStateAndUploadActivityDB() {
        Log.d("PENDING_DATA_TESTING", "Call checkStateAndUploadActivityDB")
        CoroutineScope(Job()).launch(Dispatchers.IO) {


            if (serverCheck.mainRepository.isExistsUnsentUploadActivityDB()) {
                val getAllDataFromDB = serverCheck.mainRepository.getUnsentUploadActivityDetails()
                sizeOfDbData=getAllDataFromDB.size

                for (unsentActivity in getAllDataFromDB) {

                    val vehicle = Vehicle(
                        0,
                        unsentActivity.vehicleId,
                        unsentActivity.vehicleDescription,
                        unsentActivity.vehiclePlateNumber
                    )
                    val geoPosition = GeoPosition(
                        unsentActivity.latitudeGeoPosition,
                        unsentActivity.longitudeGeoPosition
                    )

                    if (unsentActivity.stateId == null) {

                        uploadPendingDataActivity(
                            unsentActivity.roomDBId,
                            unsentActivity.dateTime,
                            unsentActivity.totalTime,
                            unsentActivity.activity,
                            geoPosition,
                            vehicle,
                            serverCheck.authRepository
                        )
                    } else {
                        val state = State(
                            0,
                            unsentActivity.stateId,
                            unsentActivity.stateDescription!!
                        )
                        updateState(
                            unsentActivity.roomDBId,
                            unsentActivity.dateTime,
                            unsentActivity.totalTime,
                            state,
                            geoPosition,
                            vehicle
                        )
                    }
                }
                Log.d("PENDING_DATA_TESTING", "Activity UPDATE")
                Log.d("PENDING_DATA_TESTING", "after Activity UPDATE")
            }


            checkForRemainingCalls()
        }
    }


    private suspend fun uploadPendingDataActivity(
        roomId: Int,
        datetime: String?,
        totalTime: Int?,
        activity: Int?,
        geoPosition: GeoPosition?,
        vehicle: Vehicle?,
        authRepository: AuthRepository
    ) {

        try {

            tinyDB.getString("Cookie")?.let { token ->
                val response = authRepository.updateActivity(
                    datetime,
                    totalTime,
                    activity,
                    geoPosition,
                    vehicle,
                    token
                )
                serverCheck.mainRepository.deleteUnsentUploadActivity(roomId)
                increaseIndex++
                percentageValue=(increaseIndex/sizeOfDbData)*100
                notificationBuilder?.setProgress(100,percentageValue,false)
                manager?.notify(101,notification)
                println("SuccessResponse $response")
            }

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
        } catch (e: Exception) {
            Log.d("connection Exception", "Connect Not Available")
        }


    }


    private suspend fun updateState(
        roomId: Int,
        datetime: String?,
        totalTime: Int?,
        state: State?,
        geoPosition: GeoPosition?,
        vehicle: Vehicle?
    ) {

        tinyDB.getString("Cookie")?.let { token ->
            try {

                val response = serverCheck.authRepository.updateState(
                    datetime,
                    totalTime,
                    state,
                    geoPosition,
                    vehicle,
                    token
                )
                serverCheck.mainRepository.deleteUnsentUploadActivity(roomId)
                increaseIndex++
                percentageValue=(increaseIndex/sizeOfDbData)*100
                notificationBuilder?.setProgress(100,percentageValue,false)
                manager?.notify(101,notification)
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

            } catch (e: Exception) {
                Log.d("connection Exception", "Connect Not Available")
            }
        }

    }


///----------------------- DB Operations ----------------------//

    private suspend fun checkForRemainingCalls() {
        if (!(serverCheck.mainRepository.isExistsUnsentUploadActivityDB())) {

            tinyDB.putBoolean("PENDINGCHECK", false)
            tinyDB.putBoolean("SYNC_CHECK", true)
            stopSelf()
        } else {
            serverCheck.serverCheck {
                checkStateAndUploadActivityDB()
            }
        }
    }
}