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
import com.logicasur.appchoferes.Extra.CheckConnection
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.Extra.serverCheck.ServerCheck
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.auth.repository.AuthRepository
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
import java.util.*

class ServiceUploadOfflineActivities : Service() {

    private var manager: NotificationManager? = null
    private var notification: Notification? = null
    private var notificationBuilder: Notification.Builder? = null
    private var notificationBuilderLowerVersion: NotificationCompat.Builder? = null
    var percentageValue = 0.0
    var sizeOfDbData = 0
    var increaseIndex = 0
    var notificationTitle = ""
    var TAG = "SERVICE_TESTING"
    var timerCheckInternet = Timer()


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
        timerCheckInternet()
        manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        CoroutineScope(Job()).launch {

            getNotification(MyApplication.appContext, 101)
        }

        notificationTitle()

        Log.d("SERVICE_TESTING", "onStartCommand")
        checkStateAndUploadActivityDB()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        timerCancel()
        manager?.cancel(101)
        Log.d("SERVICE_TESTING", "IN ON DESTROY")
    }

    fun timerCheckInternet() {
        timerCheckInternet.schedule(object : TimerTask() {
            override fun run() {
                if (!CheckConnection.netCheck(MyApplication.appContext)) {

                    timerCancel()

                    stopSelf()

                }
            }
        }, 0, 5000)
    }

    fun timerCancel() {
        tinyDB.putBoolean("PENDINGCHECK", false)
        tinyDB.putBoolean("SYNC_CHECK", true)
        timerCheckInternet.cancel()
        timerCheckInternet.purge()

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
        startForeground(101, notification!!)
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
                .setContentTitle(notificationTitle)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setProgress(100, percentageValue.toInt(), false)
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
            notificationBuilder!!.build()


        } else {
            notificationBuilderLowerVersion = NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.app_icon)
                .setContentTitle(notificationTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setProgress(100, percentageValue.toInt(), false)
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
                sizeOfDbData = getAllDataFromDB.size

                Log.d("SERVICE_TESTING_DB", "Size of DB$sizeOfDbData")

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
                Log.d("SERVICE_TESTING", "IN For activity")
                updateProgressBar()
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

    fun updateProgressBar() {
        increaseIndex++
        Log.d("SERVICE_TESTING", "Index increase  $increaseIndex      -----  $sizeOfDbData")
        percentageValue = increaseIndex.toDouble().div(sizeOfDbData.toDouble())
        Log.d("SERVICE_TESTING", "before multi $percentageValue")
        percentageValue *= 100
        Log.d("SERVICE_TESTING", "%%%%%  $percentageValue")
        notificationBuilder?.setProgress(100, percentageValue.toInt(), false)
        manager?.notify(101, notification)
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
                Log.d("SERVICE_TESTING", "IN for state")
                updateProgressBar()
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
            Log.d(
                "SERVICE_TESTING_DB",
                "Check DB Existance ${serverCheck.mainRepository.isExistsUnsentUploadActivityDB()}"
            )

            timerCancel()
            stopSelf()
        } else {
            serverCheck.serverCheck {

                increaseIndex = 0
                sizeOfDbData = 0
                percentageValue = 0.0

                Log.d(
                    "SERVICE_TESTING",
                    "checkForRemainingCalls  $percentageValue  ... $sizeOfDbData...$increaseIndex"
                )
                checkStateAndUploadActivityDB()
            }
        }
    }

    fun notificationTitle() {
        var language = tinyDB.getString("language")
        if (language == "0") {
            notificationTitle = "Cargar actividades sin conexión"

        } else if (language == "1") {


            notificationTitle = "Upload offline activities"
        } else {

            notificationTitle = "Carregar atividades offline"
        }

    }
}