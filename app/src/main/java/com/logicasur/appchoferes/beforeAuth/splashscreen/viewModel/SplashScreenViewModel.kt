package com.logicasur.appchoferes.beforeAuth.splashscreen.viewModel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logicasur.appchoferes.Extra.CheckConnection
import com.logicasur.appchoferes.utils.ResendApis
import com.logicasur.appchoferes.Extra.TimeCalculator
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.data.repository.AuthRepository
import com.logicasur.appchoferes.common.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.afterAuth.mainscreen.MainActivity
import com.logicasur.appchoferes.data.repository.MainRepository
import com.logicasur.appchoferes.utils.myApplication.MyApplication
import com.logicasur.appchoferes.data.network.ApiException
import com.logicasur.appchoferes.data.network.NoInternetException
import com.logicasur.appchoferes.data.network.ResponseException
import com.logicasur.appchoferes.data.network.signinResponse.SigninResponse
import com.logicasur.appchoferes.data.network.unsentApis.UnsentStartBreakTime
import com.logicasur.appchoferes.data.network.unsentApis.UnsentStartWorkTime
import com.logicasur.appchoferes.beforeAuth.splashscreen.SplashScreen
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

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    val authRepository: AuthRepository,
    val mainRepository: MainRepository,
    val resendApis: ResendApis, val timeCalculator: TimeCalculator
) : ViewModel() {
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
    }

    fun checkData() {

        MyApplication.checKForSyncLoading = true
        MyApplication.syncCheck = true

        // Start the loader
        moveToLoadingScreen()
        // Start Service for resend apis
        resendApis.checkNetAndUpload()
        myTimer = Timer()
        Log.d("SYNC_CHECK_TESTING", "Before timer  ${MyApplication.syncCheck}")

        myTimer!!.schedule(object : TimerTask() {
            override fun run() {

                viewModelScope.launch(Dispatchers.IO) {

                    if (tinyDB.getBoolean("SYNC_CHECK")) {
                        Log.d("SYNC_CHECK_TESTING", "RUN SYNC")
                        syncData(false)
                        myTimer!!.cancel()
                    } else {
                        Log.d("SYNC_CHECK_TESTING", "In False")
                        val netCheck = activityContext?.let { CheckConnection.netCheck(it) }
                        if (netCheck == false) {
                            MyApplication.checKForPopup = true

                            withContext(Dispatchers.Main) {
                                delay(3000)
                                LoadingScreen.OnEndLoadingCallbacks?.openPopup(
                                    myTimer!!,
                                    b = false,
                                    forServer = false
                                )
                                myTimer!!.cancel()
                            }

                        }
                    }
                }


            }
        }, 0, 4000)
    }


    suspend fun syncData(toShowLoader: Boolean) {
        val token = tinyDB.getString("Cookie")

        if(toShowLoader){
            moveToLoadingScreen()
        }

        try {

            val response =
                authRepository.userSync(token!!)

            println("SuccessResponse $response")

            if (response != null) {
                authRepository.clearData()
                authRepository.InsertSigninData(response)
                saveDataInTinyDatabase(response)
                checkLocalBaseAndPassData(response)
                checkLoadingScreenImage(response)
            }
        } catch (e: ResponseException) {
            repeatSync()
            println("ErrorResponse")
        } catch (e: ApiException) {
            repeatSync()
            e.printStackTrace()
        } catch (e: NoInternetException) {
            println("position 2")
            e.printStackTrace()
            repeatSync()
            showToast()

        } catch (e: SocketTimeoutException) {
            repeatSync()
            LoadingScreen.OnEndLoadingCallbacks?.openPopup(myTimer!!, false, false)
            showToast()
        } catch (e: Exception) {
            repeatSync()
            println("ErrorResponse ${e.localizedMessage}")
        } catch (e: SocketException) {
            repeatSync()
            Log.d("connection Exception", "Connect Not Available")
            showToast()


        }


    }


    suspend fun getLoadingScreenImage() {
        Log.d("LoadingImage", "IN API")
        val Token = tinyDB.getString("Cookie")
        try {
            val response = authRepository.getLoadingScreen(Token!!)
            if (response != null) {
                Log.d("LoadingImage", "We got the string ${response.loadingScreen}")
                tinyDB.putString("loadingBG", response.loadingScreen ?: "")
            } else {
                Log.d("LoadingImage", "The response is null")
            }

            getAvatar()


        } catch (e: ApiException) {
            getAvatar()
            e.printStackTrace()
            Log.d("LoadingImage", "API EXCEPTION ${e.localizedMessage}")
        } catch (e: NoInternetException) {
            getAvatar()
            println("position 2")
            e.printStackTrace()
            Log.d("LoadingImage", "No Internet EXCEPTION ${e.localizedMessage}")

            showToast()
        } catch (e: ResponseException) {
            println("ErrorResponse")
            getAvatar()
            Log.d("LoadingImage", "Response Exception ${e.localizedMessage}")

        } catch (e: SocketException) {
            getAvatar()
            Log.d("connection Exception", "Connect Not Available")
            showToast()
        } catch (e: SocketTimeoutException) {
            getAvatar()
        } catch (e: Exception) {
            getAvatar()
        }


    }

    fun getAvatar() {
        viewModelScope.launch {
            val Token = tinyDB.getString("Cookie")
            withContext(Dispatchers.IO) {

                try {
                    val user = tinyDB.getString("User")

                    val response = authRepository.getUserAvatar(user!!, Token!!)

                    println("SuccessResponse $response")



                    if (response != null) {

                        tinyDB.putString("Avatar", response.avatar)

                        moveToMainScreen()


                    }

                } catch (e: ApiException) {
                    moveToMainScreen()
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    moveToMainScreen()
                    println("position 2")
                    e.printStackTrace()

                    showToast()
                } catch (e: ResponseException) {
                    moveToMainScreen()
                    println("ErrorResponse")
                } catch (e: SocketException) {
                    moveToMainScreen()
                    Log.d("connection Exception", "Connect Not Available")
                    showToast()
                } catch (e: SocketTimeoutException) {
                    moveToMainScreen()
                    Log.d("connection Exception", "Connect Not Available")
                    showToast()
                } catch (e: Exception) {
                    moveToMainScreen()
                    Log.d("connection Exception", "Connect Not Available")
                    showToast()
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
                        val image = response.splashScreen
                        tinyDB.putString("SplashBG", response.splashScreen)
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
                    LoadingScreen.OnEndLoadingCallbacks!!.endLoading("From Splash Screen line nbr 262")
                } catch (e: Exception) {
                    Log.d("connection Exception", "Connect Not Available")
                }
            }
        }


    }

    private fun getPreviousTime(response: SigninResponse) {

        Log.d("NEGATIVE_TESTING", "in function")
        tinyDB.putInt("lasttimebreak", response.lastVar!!.lastWorkBreakTotal!!)
        tinyDB.putInt("lasttimework", response.lastVar.lastWorkedHoursTotal!!)

        when (response.lastVar.lastActivity) {
            0 -> {
                Log.d("NEGATIVE_TESTING", "in function2")
                getWorkTime(response)
            }
            1 -> {
                Log.d("NEGATIVE_TESTING", "in function break")
                tinyDB.putString("checkTimer", "breakTime")
                var breakDate = response.lastVar.lastWorkBreakDateIni
                if (breakDate!!.isNotEmpty()) {
                    breakDate = breakDate.split(".").toTypedArray()[0]
                    breakDate = breakDate.replace("T", " ")
                    breakDate = breakDate.replace("-", "/")
                    Log.d("workDate Is", "date is $breakDate")
                }
                tinyDB.putString("goBackTime", breakDate)
                tinyDB.putInt("ServerBreakTime", response.lastVar.lastWorkBreakTotal!!)
                timeCalculator.timeDifference(
                    tinyDB,
                    activityContext!!,
                    false,
                    response.work!!.workBreak, response
                )

                getWorkTime(response)

            }
            2 -> {
                MyApplication.dayEndCheck = 100
                getWorkTime(response)
            }
            3 -> {
                MyApplication.dayEndCheck = 200
            }
        }

        insertDataInDataBase(response)


    }

    fun getWorkTime(response: SigninResponse) {
        Log.d("NEGATIVE_TESTING", "in function 3")
        tinyDB.putString("checkTimer", "workTime")
        var workDate = response.lastVar!!.lastWorkedHoursDateIni
        if (workDate!!.isNotEmpty()) {
            workDate = workDate.split(".").toTypedArray()[0]
            workDate = workDate.replace("T", " ")
            workDate = workDate.replace("-", "/")
            Log.d("workDate Is", "date is $workDate")
        }
        tinyDB.putString("goBackTime", workDate)
        timeCalculator.timeDifference(
            tinyDB,
            activityContext!!,
            false,
            response.work!!.workBreak,
            response
        )
    }


//--------------------------------------------Utils------------

    private suspend fun checkLocalBaseAndPassData(response: SigninResponse) {
        if (resendApis.serverCheck.mainRepository.isExistsUnsentUploadActivityDB()) {
            Log.d("TIMER_TESTING_SYNC","IN IF BLOCK")
            LoadingScreen.OnEndLoadingCallbacks!!.calculateTimeFromLocalDB()
        } else {
            Log.d("TIMER_TESTING_SYNC","IN ElSE BLOCK")
            getPreviousTime(response)
            tinyDB.putInt("lastVehicleid", response.lastVar!!.lastIdVehicle!!.id!!)
            if (response.lastVar.lastActivity != 3) {
                val state = response.lastVar.lastState!!
                tinyDB.putInt("state", state + 1)
            } else {
                tinyDB.putInt("state", 1)
            }
            checkStateByServer(response)
            tinyDB.putInt("againCome", 200)
            MyApplication.check = 200
        }
    }

    suspend fun checkLoadingScreenImage(response: SigninResponse) {
        val imageCheck = tinyDB.getString("LOADINGIMAGE")
        Log.d("LOADINGIMAGETESTING", "-------> $imageCheck")
        if (response.images.loadingScreen != imageCheck || response.images.loadingScreen == "") {
            tinyDB.putString("LOADINGIMAGE", response.images.loadingScreen)
            getLoadingScreenImage()


        } else {
            getAvatar()
        }
    }

    private fun saveDataInTinyDatabase(response: SigninResponse) {
        val language = response.profile?.language
        val notify: Boolean = response.profile?.notify!!

        if (response.colors.primary.isNotEmpty()) {
            ResendApis.primaryColor = response.colors.primary
            ResendApis.secondaryColor = response.colors.secondary

//            ResendApis.primaryColor = "#FC9918"
//            ResendApis.secondaryColor = "#FC9918"


            Log.d("COLORCHECKTESTING", response.colors.primary)
            tinyDB.putString("primaryColor", ResendApis.primaryColor)
            tinyDB.putString("secondrayColor", ResendApis.secondaryColor)
        }
        tinyDB.putInt("defaultWork", response.work!!.workingHours)
        tinyDB.putInt("defaultBreak", response.work.workBreak)
        tinyDB.putString("language", language.toString())

        val max = response.work.workBreak * 60
        println("Max Value from Server $max")
        tinyDB.putInt("MaxBreakBar", max)

        val maxWork = response.work.workingHours * 60
        println("Max Value from Server $maxWork")
        tinyDB.putInt("MaxBar", maxWork)
        tinyDB.putBoolean("notify", notify)
    }

    fun repeatSync() {
        tinyDB.putBoolean("SYNC_CHECK", false)
        checkData()
    }

    private fun tagsForToast() {
        val language = tinyDB.getString("language")
        if (language == "0") {

            TAG2 = "Comprueba tu conexión a Internet"

        } else if (language == "1") {


            TAG2 = "Check Your Internet Connection"
        } else {
            TAG2 = "Verifique a sua conexão com a internet"
        }

    }

    private fun checkStateByServer(response: SigninResponse) {
        var workDate = response.lastVar!!.lastWorkedHoursDateIni
        if (workDate!!.isNotEmpty()) {
            workDate = workDate.split("T").toTypedArray()[0]
            Log.d("workDate Is", "date is $workDate")
        }
        Log.d("Dates is ", currentDate)
        check = response.lastVar.lastActivity!!

        tinyDB.putInt("selectedStateByServer", check)
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

    private fun insertDataInDataBase(response: SigninResponse) {
        var workStartTime = response.lastVar?.lastWorkedHoursDateIni
        var breakStartTime = response.lastVar?.lastWorkBreakDateIni
        if (workStartTime != null) {
            workStartTime = workStartTime.replace("T", ",")
            workStartTime = workStartTime.split(".").toTypedArray()[0]
            workStartTime += "Z"

            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    mainRepository.deleteAllUnsentStartWorkTime()
                    mainRepository.insertUnsentStartWorkTime(
                        UnsentStartWorkTime(
                            0,
                            workStartTime
                        )
                    )
                }
            }
        }


        if (breakStartTime != null) {
            breakStartTime = breakStartTime.replace("T", ",")
            breakStartTime = breakStartTime.split(".").toTypedArray()[0]
            breakStartTime = breakStartTime + "Z"

            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    if (mainRepository.getUnsentStartBreakTimeDetails() != null) {
                        mainRepository.deleteAllUnsentStartBreakTime()
                    }
                    mainRepository.insertUnsentStartBreakTime(
                        UnsentStartBreakTime(
                            0,
                            breakStartTime
                        )
                    )
                }
            }
        }
    }

    private suspend fun showToast() {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                activityContext,
                TAG2,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun moveToMainScreen() {
        val intent = Intent(activityContext, MainActivity::class.java)
        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
        (activityContext as SplashScreen).finish()
    }

    private fun moveToLoadingScreen() {
        LoadingScreen.OnEndLoadingCallbacks?.endLoading("Ending From Splash 541")
        val intent = Intent(activityContext, LoadingScreen::class.java)
        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
    }
}