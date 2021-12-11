package com.example.marcadechoferes.splashscreen.viewModel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.auth.repository.AuthRepository
import com.example.marcadechoferes.loadingScreen.LoadingScreen
import com.example.marcadechoferes.mainscreen.MainActivity
import com.example.marcadechoferes.myApplication.MyApplication
import com.example.marcadechoferes.network.ResponseException
import com.example.marcadechoferes.splashscreen.SplashScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

@HiltViewModel
class SplashScreenViewModel @Inject constructor(val authRepository: AuthRepository):ViewModel(){
      var activityContext:Context?=null
    lateinit var tinyDB: TinyDB
    fun viewsOfActivity(context: Context){
        activityContext = context
        tinyDB = TinyDB(context)

    }


    fun syncdata(){
        var intent = Intent(activityContext, LoadingScreen::class.java)
        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
        var Token=tinyDB.getString("Cookie")
        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                try {
                    val response =
                        authRepository.userSync(Token!!)

                    println("SuccessResponse $response")

                    if(response!=null) {
                              authRepository.clearData()

                            authRepository.InsertSigninData(response)
                        val Language =response.profile?.language
                        val notify:Boolean =response.profile?.notify!!
                        tinyDB.putInt("lasttimework", response.lastVar!!.lastWorkedHoursTotal!!)
                        tinyDB.putInt("lasttimebreak", response.lastVar!!.lastWorkBreakTotal!!)
                        tinyDB.putInt("defaultWork",response.work!!.workingHours)
                        tinyDB.putInt("defaultBreak",response.work.workBreak)
                        tinyDB.putInt("lastVehicleid", response.lastVar!!.lastIdVehicle!!.id!!)
                        tinyDB.putString("language", Language.toString())
                        tinyDB.putBoolean("notify",notify)
                        tinyDB.putInt("againCome",200)
                        MyApplication.check=200
                        Timer().schedule(1500) {
                            var intent = Intent(activityContext, MainActivity::class.java)
                            ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
                            (activityContext as SplashScreen).finish()

                        }


                    }
                } catch (e: ResponseException) {
                    println("ErrorResponse")
                }
            }
        }
    }
}