package com.logicasur.appchoferes.mainscreen.home.timerServices

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.auth.repository.AuthRepository
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.ApiException
import com.logicasur.appchoferes.network.NoInternetException
import com.logicasur.appchoferes.network.ResponseException
import kotlinx.coroutines.*
import java.net.SocketTimeoutException

import android.graphics.BitmapFactory

import android.app.PendingIntent
import android.content.res.Resources
import com.logicasur.appchoferes.network.GeoPosition
import com.logicasur.appchoferes.network.signinResponse.Vehicle
import java.text.SimpleDateFormat
import java.util.*


class UploadRemaingDataService : Service() {
    companion object {
        var time: Int = 0
        var activity = 0
        lateinit var authRepository: AuthRepository
        var activityContext: Context? = null
        var apiJob = Job()


        fun getStartIntent(
            totalTime: Int,
            activity: Int,
            authRepository: AuthRepository,
            context: Context
        ): Intent {
            time = totalTime
            this.activity = activity
            this.authRepository = authRepository
            activityContext = context
            val intent = Intent(context, UploadRemaingDataService::class.java)
            return intent
        }

    }

    lateinit var tinyDB: TinyDB


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        tinyDB = TinyDB(MyApplication.appContext)
        CoroutineScope(apiJob).launch {
            updateActivity(authRepository, activityContext!!)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onCreate() {
        super.onCreate()
        startForeground()

    }

    fun updateActivity(
        authRepository: AuthRepository, context: Context
    ) {
        val childJob = Job(apiJob)

        CoroutineScope(childJob).launch {

            withContext(Dispatchers.IO) {

                var vehicle = tinyDB.getObject("VehicleForBackgroundPush",Vehicle::class.java)
                var geoPosition= tinyDB.getObject("GeoPosition",GeoPosition::class.java)
                val sdf = SimpleDateFormat("yyyy-MM-dd:hh:mm:ss")
                val currentDate = sdf.format(Date())


                try {
                    var Token = tinyDB.getString("Cookie")
                    val response = authRepository.updateActivity(
                         currentDate,
                        time,
                        activity,
                         geoPosition,
                         vehicle,
                        Token!!
                    )
                    println("SuccessResponse $response")


                    if (response != null) {
                        stopSelf()
                    }

                } catch (e: ResponseException) {
                    stopSelf()
                    println("ErrorResponse")
                } catch (e: ApiException) {
                    stopSelf()
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    stopSelf()
                    println("position 2")
                    e.printStackTrace()
                } catch (e: SocketTimeoutException) {
                    stopSelf()
                }
            }
        }


    }




    private fun startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel("my_service", "My Background Service")
                } else {
                    // If earlier version channel ID is not used
                    // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                    ""
                }

            val notificationBuilder = NotificationCompat.Builder(this, channelId)
            val notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.app_icon)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setProgress(100,20,true)
                .build()
            startForeground(101, notification)
        } else {
            val currentapiVersion = Build.VERSION.SDK_INT
            if (currentapiVersion >= 16) {
                val context: Context = this
                val notificationIntent = Intent(context, UploadRemaingDataService::class.java)
                val contentIntent = PendingIntent.getService(
                    context,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
                val nm = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                val res: Resources = context.resources
                val builder = Notification.Builder(context)
                builder.setContentIntent(contentIntent)
                    .setSmallIcon(R.mipmap.app_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.app_icon))
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setContentTitle(res.getString(R.string.app_name)
                    )

                val n = builder.build()
                nm.notify(7, n)
            }


        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


}
