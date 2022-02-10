package com.logicasur.appchoferes.splashscreen.viewModel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logicasur.appchoferes.Extra.K
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.Extra.serverCheck.ServerCheck
import com.logicasur.appchoferes.auth.repository.AuthRepository
import com.logicasur.appchoferes.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.mainscreen.MainActivity
import com.logicasur.appchoferes.mainscreen.repository.MainRepository
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.ApiException
import com.logicasur.appchoferes.network.NoInternetException
import com.logicasur.appchoferes.network.ResponseException
import com.logicasur.appchoferes.network.signinResponse.SigninResponse
import com.logicasur.appchoferes.network.unsentApis.UnsentStartBreakTime
import com.logicasur.appchoferes.network.unsentApis.UnsentStartWorkTime
import com.logicasur.appchoferes.splashscreen.SplashScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

@HiltViewModel
class SplashScreenViewModel @Inject constructor(val authRepository: AuthRepository,val mainRepository: MainRepository) : ViewModel() {
    var activityContext: Context? = null
    lateinit var tinyDB: TinyDB
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    val currentDate = sdf.format(Date())
    var check = 0
    var TAG2 = ""
    var myTimer: Timer? = null

    fun viewsOfActivity(context: Context) {
        activityContext = context
        tinyDB = TinyDB(context)
        tagsForToast()
        K.authRepository= authRepository
        K.mainRepository=mainRepository
        ServerCheck.authRepository=authRepository
        ServerCheck.mainRepository=mainRepository


    }

    fun checkData(){
        K.checkNet()
        myTimer = Timer()
        myTimer!!.schedule(object : TimerTask() {
            override fun run() {
           var check = tinyDB.getBoolean("SYNC_CHECK")
                if(check==true){
                    Log.d("SYNC_CHECK_TESTING","RUN SYNC")
                    syncdata()
                    myTimer!!.cancel()
                }
                else{
                    Log.d("SYNC_CHECK_TESTING","In False")
                    var check=K.isConnected()
                    if(check == false){
                        MyApplication.checKForPopup = true
                 viewModelScope.launch {
    withContext(Dispatchers.Main){
        delay(2000)
        LoadingScreen.OnEndLoadingCallbacks?.openPopup(myTimer!!)
    }
}

                            //changed
                                //<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>
                              //  Toast.makeText(activityContext, TAG2, Toast.LENGTH_SHORT).show()


                    }
                }
            }
        }, 0, 10000)
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
//                        tinyDB.putString("loadingBG", response.images.loadinScreen ?: "")
//                        tinyDB.putString("SplashBG", response.images.splashScreen ?: "")
                        var max = response.work.workBreak * 60
                        println("Max Value from Server $max")
                        tinyDB.putInt("MaxBreakBar",max)

                        var maxWork = response.work!!.workingHours * 60
                        println("Max Value from Server $maxWork")
                        tinyDB.putInt("MaxBar",maxWork)


                        if (response.lastVar.lastActivity != 3) {
                            var state = response.lastVar.lastState!!
                            tinyDB.putInt("state", state + 1)
                        } else {
                            tinyDB.putInt("state", 1)
                        }

                        if (response.colors.primary.isNotEmpty()) {
                            K.primaryColor = response.colors.primary ?: "#7A59FC"
                            K.secondrayColor = response.colors.secondary ?: "#653FFB"
                            Log.d("COLORCHECKTESTING",response.colors.primary )
                            tinyDB.putString("primaryColor",K.primaryColor)
                            tinyDB.putString("secondrayColor",K.secondrayColor)
                        }

                       var color= tinyDB.getString("primaryColor")
                        Log.d("COLORCHECKTESTING22",color!!)
                        checkStateByServer(response)
                        tinyDB.putBoolean("notify", notify)
                        tinyDB.putInt("againCome", 200)
                        MyApplication.check = 200
                        Log.d("LOADINGIMAGETESTING","here1")

                        var image = response.images.loadingScreen
                        println("hello testing $image")
//                        Log.d("CheckLoading",image)
                        //changed
                        val imageCheck=tinyDB.getString("LOADINGIMAGE")

                        if(response.images.loadingScreen!=imageCheck || response.images.loadingScreen ==""){
                            println("LOADING IMAGE IS HERE ")
                            tinyDB.putString("LOADINGIMAGE",response.images.loadingScreen)
                            getLoadingScreenImage()


                        }else{
                            println("LOADING IMAGE IS HERE IN ELSE")
                            getAvatar()
                        }

                        println("LOADING IMAGE IS HERE NO WHERE")




                    }
              }
                catch (e: ResponseException) {
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
                        LoadingScreen.OnEndLoadingCallbacks?.endLoading()
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
                        LoadingScreen.OnEndLoadingCallbacks?.endLoading()
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


    suspend fun getLoadingScreenImage(){
        Log.d("LoadingImage","IN API")
        val Token = tinyDB.getString("Cookie")
        try {
            val response = authRepository.getLoadingScreen(Token!!)
            if(response!=null) {
                Log.d("LoadingImage","We got the string ${response.loadingScreen}")
                tinyDB.putString("loadingBG",response.loadingScreen ?: "")
            }else{
                Log.d("LoadingImage","The response is null")
            }

            println("SuccessResponse $response")
            getAvatar()


        }
        catch (e: ApiException) {
            e.printStackTrace()
            Log.d("LoadingImage","API EXCEPTION ${e.localizedMessage}")
        }
        catch (e: NoInternetException) {
            println("position 2")
            e.printStackTrace()
            Log.d("LoadingImage","No Internet EXCEPTION ${e.localizedMessage}")

            withContext(Dispatchers.Main){
                Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
            }
        }
        catch (e: ResponseException) {
            println("ErrorResponse")
            Log.d("LoadingImage","Response Exception ${e.localizedMessage}")

        }
        catch(e: SocketException){
            LoadingScreen.OnEndLoadingCallbacks?.endLoading()
            Log.d("connection Exception","Connect Not Available")
            withContext(Dispatchers.Main){
                Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
            }
        }




    }

    fun getAvatar(){
        Log.d("AvtarImage","IN Avtar API")
        viewModelScope.launch {
            var Token = tinyDB.getString("Cookie")
            withContext(Dispatchers.IO) {

                try {
                    var user=tinyDB.getString("User")

                    val response = authRepository.getUserAvatar(user!!,Token!!)

                    println("SuccessResponse $response")



                    if(response!=null) {

                        tinyDB.putString("Avatar",response.avatar)

                        var intent = Intent(activityContext,MainActivity::class.java)
                        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
                        (activityContext as SplashScreen).finish()


                    }

                }
                catch (e: ApiException) {
                    e.printStackTrace()
                }
                catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()

                    withContext(Dispatchers.Main){
                        Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: ResponseException) {
                    println("ErrorResponse")
                }
                catch(e: SocketException){
//                    LoadingScreen.onEndLoadingCallbacks?.endLoading()
                    Log.d("connection Exception","Connect Not Available")
                    withContext(Dispatchers.Main){
                        Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
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
                    LoadingScreen.OnEndLoadingCallbacks!!.endLoading()
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
                MyApplication.dayEndCheck = 100
               getWorkTime(response)
            }
            3->{
              MyApplication.dayEndCheck = 200
            }
        }


        var workStartTime=response.lastVar.lastWorkedHoursDateIni
        var breakStartTime =response.lastVar.lastWorkBreakDateIni
        if(workStartTime != null){
            workStartTime= workStartTime!!.replace("T",",")
            workStartTime= workStartTime!!.split(".").toTypedArray()[0]
            workStartTime = workStartTime+"Z"

            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    if (mainRepository.getUnsentStartWorkTimeDetails() != null) {
                        mainRepository.deleteAllUnsentStartWorkTime()
                    }
                    mainRepository!!.insertUnsentStartWorkTime(
                        UnsentStartWorkTime(
                            0,
                            workStartTime
                        )
                    )
                }
            }
        }

        if(breakStartTime != null){
            breakStartTime= breakStartTime!!.replace("T",",")
            breakStartTime= breakStartTime!!.split(".").toTypedArray()[0]
            breakStartTime = breakStartTime+"Z"

            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    if (mainRepository.getUnsentStartBreakTimeDetails() != null) {
                        mainRepository.deleteAllUnsentStartBreakTime()
                    }
                    mainRepository!!.insertUnsentStartBreakTime(
                        UnsentStartBreakTime(
                            0,
                            breakStartTime
                        )
                    )
                }
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