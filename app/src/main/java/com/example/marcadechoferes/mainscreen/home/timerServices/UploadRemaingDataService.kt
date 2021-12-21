package com.example.marcadechoferes.mainscreen.home.timerServices

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
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.Extra.UpdateActivityDataClass
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.repository.AuthRepository
import com.example.marcadechoferes.myApplication.MyApplication
import com.example.marcadechoferes.network.ApiException
import com.example.marcadechoferes.network.NoInternetException
import com.example.marcadechoferes.network.ResponseException
import kotlinx.coroutines.*
import java.net.SocketTimeoutException

class UploadRemaingDataService: Service() {
    companion object{

       var time :Int =0
       var activity=0
       lateinit var authRepository: AuthRepository
       var activityContext:Context?=null
        var apiJob = Job()

    fun getStartIntent(totalTime:Int,activity:Int,authRepository: AuthRepository,context: Context):Intent{
        time=totalTime
        this.activity= activity
        this.authRepository = authRepository
        activityContext=context
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
        CoroutineScope(apiJob).launch{

          var obj =  tinyDB.getObject("upadteActivity",UpdateActivityDataClass::class.java)

            updateActivity(obj, authRepository, activityContext!!)
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
        obj:UpdateActivityDataClass, authRepository:AuthRepository, context:Context
    ) {
        val childJob = Job(apiJob)

        CoroutineScope(childJob).launch {

            withContext(Dispatchers.IO) {

                try {
                    var Token = tinyDB.getString("Cookie")
                    val response = authRepository.updateActivity(
                        obj.datetime,
                        time,
                        activity,
                        obj.geoPosition,
                        obj.vehicle,
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
                }
                catch (e: SocketTimeoutException){
                    stopSelf()
                }
            }
        }


    }

    private fun startForeground() {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notificationBuilder = NotificationCompat.Builder(this, channelId )
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.app_icon)
            .setPriority(PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


}
