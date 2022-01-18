package com.logicasur.appchoferes.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.splashscreen.SplashScreen
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MyFirebaseMessagingService  : FirebaseMessagingService() {
    val TAG = "FirebaseMessagingService"


companion object{
    fun getToken(context: Context): String? {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fcm_token", "empty")
    }
}


    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fcm_token",p0).apply()
        Log.d("FCM_TOKEN_","$p0")
    }

    @SuppressLint("LongLogTag")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Push Notification: ${remoteMessage.from} body is ${remoteMessage.notification?.body}")

        if (remoteMessage.notification != null) {

            // Toast.makeText(this, ""+ remoteMessage.notification?.body, Toast.LENGTH_SHORT).show()
             showNotification(remoteMessage.notification?.title, remoteMessage.notification?.body)
        }
    }


    private fun showNotification(title: String?, body: String?) {

        val intent = Intent(this, SplashScreen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val channelId = "Default"
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.app_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true).setContentIntent(pendingIntent)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        manager.notify(0, builder.build())


    }









}