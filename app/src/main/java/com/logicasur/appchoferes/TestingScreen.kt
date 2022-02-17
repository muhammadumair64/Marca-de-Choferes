package com.logicasur.appchoferes

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.logicasur.appchoferes.utils.TestingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestingScreen : AppCompatActivity() {
    lateinit var send: TextView
    var manager:NotificationManager? = null
    var notification :Notification? = null
    var value = 10
    val viewModel: TestingViewModel by viewModels()
     var notificationBuilder:Notification.Builder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing_screen)
        viewModel.servercheck123()
        initView()


    }

    fun initView() {
         manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getNotification(this,"Upload offline activities",101)
        }
        send = findViewById(R.id.send)
        send.setOnClickListener {
            value += 10
             notificationBuilder?.setProgress(100,value,false)
            manager?.notify(101,notification)
        }
    }

    fun forToast() {
        lifecycleScope.launch {
            Toast.makeText(
                this@TestingScreen,
                "APi hit Successfully",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
//    private fun startForeground() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channelId =
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    createNotificationChannel("my_service", "My Background Service")
//                } else {
//                    // If earlier version channel ID is not used
//                    // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
//                    ""
//                }
//
//            val notificationBuilder = NotificationCompat.Builder(this, channelId)
//            val notification = notificationBuilder.setOngoing(true)
//                .setSmallIcon(R.mipmap.app_icon)
//                .setPriority(NotificationCompat.PRIORITY_MIN)
//                .setCategory(Notification.CATEGORY_PROGRESS)
//                .setProgress(100,20,true)
//                .build()
//
//
//        } else {
//            val currentapiVersion = Build.VERSION.SDK_INT
//            if (currentapiVersion >= 16) {
//                val context: Context = this
//                val notificationIntent = Intent(context, UploadRemaingDataService::class.java)
//                val contentIntent = PendingIntent.getService(
//                    context,
//                    0,
//                    notificationIntent,
//                    PendingIntent.FLAG_CANCEL_CURRENT
//                )
//                val nm = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//                val res: Resources = context.resources
//                val builder = Notification.Builder(context)
//                builder.setContentIntent(contentIntent)
//                    .setSmallIcon(R.mipmap.app_icon)
//                    .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.app_icon))
//                    .setWhen(System.currentTimeMillis())
//                    .setAutoCancel(true)
//                    .setContentTitle(res.getString(R.string.app_name)
//                    )
//
//                val n = builder.build()
//                nm.notify(7, n)
//            }
//
//
//        }
//
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun createNotificationChannel(channelId: String, channelName: String): String {
//        val chan = NotificationChannel(
//            channelId,
//            channelName, NotificationManager.IMPORTANCE_NONE
//        )
//        chan.lightColor = Color.BLUE
//        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
//        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        service.createNotificationChannel(chan)
//        return channelId
//    }





    @RequiresApi(Build.VERSION_CODES.O)
    fun getNotification(context: Context, contentText: String, id: Int): Pair<Int, Notification>? {
        createNotificationChannel(context)
      notification = createNotification(context, contentText)
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

    private fun createNotification(context: Context, contentText: String): Notification {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      notificationBuilder = Notification.Builder(context, 101.toString())
                .setSmallIcon(R.mipmap.app_icon)
                .setContentTitle("Upload Offline activities")
                .setCategory( Notification.CATEGORY_PROGRESS)
                .setProgress(100,value,false)
                .setAutoCancel(false)
                 .setOngoing(true)
                 .setOnlyAlertOnce(true)
                notificationBuilder!!.build()



        } else {
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(context)
                .setContentTitle("Marca de Choferes")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setAutoCancel(false)
                .setOngoing(true)
            builder.build(

            )
        }


    }


}