package com.example.marcadechoferes.mainscreen.home.timerServices

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.Extra.UpdateActivityDataClass
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
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onCreate() {
        super.onCreate()

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
                    println("ErrorResponse")
                } catch (e: ApiException) {
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                }
                catch (e: SocketTimeoutException){
                }
            }
        }


    }


}
