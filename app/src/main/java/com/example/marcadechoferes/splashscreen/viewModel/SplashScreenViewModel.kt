package com.example.marcadechoferes.splashscreen.viewModel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marcadechoferes.Extra.K
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.auth.repository.AuthRepository
import com.example.marcadechoferes.mainscreen.MainActivity
import com.example.marcadechoferes.myApplication.MyApplication
import com.example.marcadechoferes.network.ApiException
import com.example.marcadechoferes.network.NoInternetException
import com.example.marcadechoferes.network.ResponseException
import com.example.marcadechoferes.network.signinResponse.SigninResponse
import com.example.marcadechoferes.splashscreen.SplashScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
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
                       getPreviousTime(response)
                        tinyDB.putInt("defaultWork",response.work!!.workingHours)
                        tinyDB.putInt("defaultBreak",response.work.workBreak)
                        tinyDB.putInt("lastVehicleid", response.lastVar!!.lastIdVehicle!!.id!!)
                        tinyDB.putString("language", Language.toString())
                        tinyDB.putString("loadingBG",response.images.loadinScreen ?: "")
                        tinyDB.putString("SplashBG",response.images.splashScreen ?: "")

                        if(response.colors.primary.isNotEmpty()){
                            K.primaryColor=response.colors.primary ?: "#7A59FC"
                            K.secondrayColor = response.colors.secondary ?: "#653FFB"
                        }


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
                catch (e: ApiException) {
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            activityContext,
                            "Check Your Internet Connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                catch (e: SocketTimeoutException){

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            activityContext,
                            "Check Your Internet Connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }



    fun getSplashScreen(){

        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                try {

                    val response = authRepository.getSplashScreen()

                    println("SuccessResponse $response")



                    if(response!=null) {
                        var image = response.splashScreen
                        tinyDB.putString("SplashBG",response.splashScreen ?: "")
                        withContext(Dispatchers.Main){
                            (activityContext as SplashScreen).base64ToBitmap(image)
                        }


                    }

                }
                catch (e: ApiException) {
                    e.printStackTrace()
                }
                catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()

                    withContext(Dispatchers.Main){
                        Toast.makeText(activityContext, "Check Your Internet Connection", Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: ResponseException) {
                    println("ErrorResponse")


                }
            }
        }


    }


    private fun getPreviousTime(response: SigninResponse) {
        val sdf = SimpleDateFormat("yyyy-M-dd")
        val currentDate = sdf.format(Date())
        var workDate = tinyDB.getString("ActivityDate")
        if(workDate!!.isNotEmpty()){
            workDate = workDate!!.split(":").toTypedArray()[0]
            Log.d("workDate","date is $workDate")
        }

        if(workDate!=currentDate){
            tinyDB.putInt("lasttimebreak", response.lastVar!!.lastWorkBreakTotal!!)
        }
        tinyDB.putInt("lasttimework", response.lastVar!!.lastWorkedHoursTotal!!)

    }
}