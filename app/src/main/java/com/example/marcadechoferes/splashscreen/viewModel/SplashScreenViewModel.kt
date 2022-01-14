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
import com.example.marcadechoferes.loadingScreen.LoadingScreen
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
import java.net.SocketException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

@HiltViewModel
class SplashScreenViewModel @Inject constructor(val authRepository: AuthRepository) : ViewModel() {
    var activityContext: Context? = null
    lateinit var tinyDB: TinyDB
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    val currentDate = sdf.format(Date())
    var check = 0
    var TAG2 = ""
    fun viewsOfActivity(context: Context) {
        activityContext = context
        tinyDB = TinyDB(context)
        tagsForToast()


    }


    fun syncdata() {
        var Token = tinyDB.getString("Cookie")
        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                try {
                    val response =
                        authRepository.userSync(Token!!)

                    println("SuccessResponse $response")

                    if (response != null) {
                        authRepository.clearData()

                        authRepository.InsertSigninData(response)
                        val Language = response.profile?.language
                        val notify: Boolean = response.profile?.notify!!
                        getPreviousTime(response)
//                        setObj(response)
                        tinyDB.putInt("defaultWork", response.work!!.workingHours)
                        tinyDB.putInt("defaultBreak", response.work.workBreak)
                        tinyDB.putInt("lastVehicleid", response.lastVar!!.lastIdVehicle!!.id!!)
                        tinyDB.putString("language", Language.toString())
                        tinyDB.putString("loadingBG", response.images.loadinScreen ?: "")
                        tinyDB.putString("SplashBG", response.images.splashScreen ?: "")

                        if (response.lastVar.lastActivity != 3) {
                            var state = response.lastVar.lastState!!
                            tinyDB.putInt("state", state + 1)
                        } else {
                            tinyDB.putInt("state", 1)
                        }

                        if (response.colors.primary.isNotEmpty()) {
                            K.primaryColor = response.colors.primary ?: "#7A59FC"
                            K.secondrayColor = response.colors.secondary ?: "#653FFB"
                        }
                        checkStateByServer(response)
                        tinyDB.putBoolean("notify", notify)
                        tinyDB.putInt("againCome", 200)
                        MyApplication.check = 200

                        Timer().schedule(1500) {
                            var intent = Intent(activityContext, MainActivity::class.java)
                            ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
                            (activityContext as SplashScreen).finish()

                        }


                    }
                } catch (e: ResponseException) {
                    println("ErrorResponse")
                } catch (e: ApiException) {
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            activityContext,
                            TAG2,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Timer().schedule(5000) {
                        Log.d("connection Exception", "connection lost")
                        LoadingScreen.onEndLoadingCallbacks?.endLoading()
                    }
                } catch (e: SocketTimeoutException) {

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            activityContext,
                            TAG2,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    println("ErrorResponse ${e.localizedMessage}")
                } catch (e: SocketException) {
                    Log.d("connection Exception", "Connect Not Available")
                    Timer().schedule(5000) {
                        Log.d("connection Exception", "connection lost")
                        LoadingScreen.onEndLoadingCallbacks?.endLoading()
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            activityContext,
                            TAG2,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        }
    }


    fun getSplashScreen() {

        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                try {

                    val response = authRepository.getSplashScreen()

                    println("SuccessResponse $response")



                    if (response != null) {
                        var image = response.splashScreen
                        tinyDB.putString("SplashBG", response.splashScreen ?: "")
                        withContext(Dispatchers.Main) {
                            (activityContext as SplashScreen).base64ToBitmap(image)
                        }


                    }

                } catch (e: ApiException) {
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activityContext, TAG2, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: SocketException) {
                    Log.d("connection Exception", "Connect Not Available")
                    LoadingScreen.onEndLoadingCallbacks!!.endLoading()
                }
            }
        }


    }


    private fun getPreviousTime(response: SigninResponse) {

//        var workDate = tinyDB.getString("ActivityDate")
//        var workDate = response.lastVar!!.lastWorkedHoursDateIni
//        if(workDate!!.isNotEmpty()){
//            workDate = workDate!!.split("T").toTypedArray()[0]
//            Log.d("workDate Is","date is $workDate")
//        }
////        if(workDate != currentDate){
//            tinyDB.putInt("lasttimebreak", response.lastVar!!.lastWorkBreakTotal!!)
//        }else if(workDate == currentDate && response.lastVar!!.lastActivity != 0)
//        {
//            tinyDB.putInt("lasttimebreak", response.lastVar!!.lastWorkBreakTotal!!)
//        }

//
        tinyDB.putInt("lasttimebreak", response.lastVar!!.lastWorkBreakTotal!!)
        tinyDB.putInt("lasttimework", response.lastVar!!.lastWorkedHoursTotal!!)

        when (response.lastVar.lastActivity) {
            0 -> {
                getWorkTime(response)
            }
            1 -> {
                tinyDB.putString("checkTimer", "breakTime")
                var breakDate = response.lastVar!!.lastWorkBreakDateIni
                if (breakDate!!.isNotEmpty()) {
                    breakDate = breakDate!!.split(".").toTypedArray()[0]
                    breakDate = breakDate!!.split("T").toTypedArray()[1]
                    Log.d("workDate Is", "date is $breakDate")
                }
                tinyDB.putString("goBackTime", breakDate)
                tinyDB.putInt("ServerBreakTime", response.lastVar.lastWorkBreakTotal!!)
                K.timeDifference(tinyDB, activityContext!!, false, response.work!!.workBreak)

                getWorkTime(response)

            }
            2 -> {
               getWorkTime(response)
            }
        }


    }
    fun getWorkTime(response: SigninResponse) {
        tinyDB.putString("checkTimer", "workTime")
        var workDate = response.lastVar!!.lastWorkedHoursDateIni
        if (workDate!!.isNotEmpty()) {
            workDate = workDate!!.split(".").toTypedArray()[0]
            workDate = workDate!!.split("T").toTypedArray()[1]
            Log.d("workDate Is", "date is $workDate")
        }
        tinyDB.putString("goBackTime", workDate)
        K.timeDifference(tinyDB, activityContext!!, false, response.work!!.workBreak)
    }


    private fun checkStateByServer(response: SigninResponse) {
        var workDate = response.lastVar!!.lastWorkedHoursDateIni
        if (workDate!!.isNotEmpty()) {
            workDate = workDate!!.split("T").toTypedArray()[0]
            Log.d("workDate Is", "date is $workDate")
        }
        Log.d("Dates is ", "$currentDate")

        if (workDate != currentDate) {
            check = 3
        } else {
            check = response.lastVar!!.lastActivity!!
        }


        tinyDB.putInt("selectedStateByServer", check!!)
        Log.d("checkByServer", "check $check")
        when (check) {
            0 -> {
                tinyDB.putString("selectedState", "goToActiveState")

            }
            1 -> {
                tinyDB.putString("selectedState", "goTosecondState")
            }
            2 -> {
                tinyDB.putString("selectedState", "goToActiveState")

            }
            3 -> {
                tinyDB.putString("selectedState", "endDay")
            }
        }


    }

    fun tagsForToast() {
        var language = tinyDB.getString("language")
        if (language == "0") {

            TAG2 = "Comprueba tu conexión a Internet"

        } else if (language == "1") {


            TAG2 = "Check Your Internet Connection"
        } else {
            TAG2 = "Verifique a sua conexão com a internet"
        }

    }


//    fun setObj(response: SigninResponse) {
//
//        var datetime =
//        var totalTime = 0
//        var activity= response.lastVar!!.lastActivity
//        var geoPosition = GeoPosition(response.lastVar!!.lastStateLatitud,response.lastVar.lastStateLongitud)
//        var vehicle =
//
//        var obj = UpdateActivityDataClass(datetime,totalTime,activity,geoPosition,vehicle)
//
//        tinyDB.putObject(
//            "upadteActivity",obj)
//
//    }


}