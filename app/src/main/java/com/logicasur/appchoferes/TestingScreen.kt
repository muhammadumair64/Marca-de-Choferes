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