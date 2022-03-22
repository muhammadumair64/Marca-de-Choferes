package com.logicasur.appchoferes.beforeAuth.otpScreen.viewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logicasur.appchoferes.utils.ResendApis

import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.beforeAuth.createPasswordScreen.CreateNewPasswordScreen
import com.logicasur.appchoferes.beforeAuth.otpScreen.OtpActivity
import com.logicasur.appchoferes.data.repository.AuthRepository
import com.logicasur.appchoferes.databinding.ActivityOtpBinding
import com.logicasur.appchoferes.common.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.utils.myApplication.MyApplication
import com.logicasur.appchoferes.data.network.ApiException
import com.logicasur.appchoferes.data.network.NoInternetException
import com.logicasur.appchoferes.data.network.ResponseException
import com.logicasur.appchoferes.data.network.signinResponse.SigninResponse
import com.logicasur.appchoferes.beforeAuth.splashscreen.SplashScreen
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.logicasur.appchoferes.Extra.CheckConnection
import com.logicasur.appchoferes.Extra.TimeCalculator
import com.logicasur.appchoferes.common.serverCheck.ServerCheck
import com.logicasur.appchoferes.data.repository.MainRepository
import com.logicasur.appchoferes.data.network.unsentApis.UnsentStartBreakTime
import com.logicasur.appchoferes.data.network.unsentApis.UnsentStartWorkTime
import com.logicasur.appchoferes.utils.MyFirebaseMessagingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.Reader
import java.lang.Exception
import java.net.SocketException
import javax.inject.Inject

@HiltViewModel
class OTPViewModel @Inject constructor(
    val authRepository: AuthRepository,
    val mainRepository: MainRepository,
    val resendApis: ResendApis, val timeCalculator: TimeCalculator, val serverCheck: ServerCheck
) : ViewModel() {
    var activityContext: Context? = null
    lateinit var tinyDB: TinyDB
    var fromSplash: Boolean? = null;
    fun viewsForOTPScreen(context: Context, binding: ActivityOtpBinding) {
        activityContext = context
        tinyDB = TinyDB(context)
        binding.arrowBack.setBackgroundColor(Color.parseColor("#7A59FC"))

        otpEditTextAutoForward(binding)
        //Submit Button
        binding.SubmitButton.setOnClickListener {
            if (binding.edt1.text.toString() != "" && binding.edt1.text.toString() != "" && binding.edt1.text.toString() != "" && binding.edt1.text.toString() != "") {
                val otp = "${binding.edt1.text}${binding.edt2.text}${binding.edt3.text}${binding.edt4.text}".trim()
                println("otpScreen is ${otp.toInt()}")
                val user = tinyDB.getString("UserOTP")
                if (checkNetAndValidateDate()) {
                    otpAuth(user!!, otp.toInt())
                }


            }

        }

        //Back button
        binding.backButton.setOnClickListener {
            if (fromSplash != null) {
                fromSplashToOtp()
            } else {
                (context as Activity).finish()
            }

        }

    }


    @SuppressLint("HardwareIds")
    fun otpAuth(userName: String, otp: Int) {
        val iPath: File = Environment.getDataDirectory()
        val iStat = StatFs(iPath.path)
        val iBlockSize = iStat.blockSizeLong
        val iAvailableBlocks = iStat.availableBlocksLong
        val iTotalBlocks = iStat.blockCountLong
        val iAvailableSpace = formatSize(iAvailableBlocks * iBlockSize)
        val iTotalSpace = formatSize(iTotalBlocks * iBlockSize)
        val unUsed = (iTotalBlocks * iBlockSize) - (iAvailableBlocks * iBlockSize)
        val usedSpace = formatSize(unUsed)


        val name = userName
        val idApp: String? = MyFirebaseMessagingService.getToken(activityContext!!)
        val memUsed: String = usedSpace
        val diskFree: String = iAvailableSpace
        val diskTotal: String = iTotalSpace
        val model: String? = Build.MODEL
        val operatingSystem = "android"
        val osVersion: String? = getAndroidVersion()
        val appVersion = "5"
        val appBuild: String? = Build.ID
        val platform = "Android"
        val manufacturer: String? = Build.MANUFACTURER
        val uuid: String? = Settings.Secure.getString(
            activityContext?.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        val isVirtual: String = isEmulator().toString()




        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                try {
                    val Token = tinyDB.getString("Cookie").toString()
                    Log.d("UserName", name)
                    val response =
                        authRepository.otp(
                            otp, name, idApp!!,
                            memUsed,
                            diskFree,
                            diskTotal,
                            model!!,
                            operatingSystem,
                            osVersion!!,
                            appVersion,
                            appBuild!!,
                            platform,
                            manufacturer!!,
                            uuid!!,
                            isVirtual,
                            Token
                        )

                    println("SuccessResponse OTP $response")

                    if (response != null) {
                        insertDataInTinyDataBase(response, name)
                        getPreviousTime(response)
                        checkStateByServer(response)
                        getLoadingScreenImage()

                    }
                } catch (e: ResponseException) {
                    tinyDB.putString("User", "")
                    showServerPopup(true,"OTP incorrecta")
                    println("ErrorResponse ${e.localizedMessage}")
                } catch (e: ApiException) {
                    showServerPopup(false, "OTP incorrecta")
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    showServerPopup(false, "")
                } catch (e: SocketException) {

                    Log.d("connection Exception", "Connect Not Available")
                    showServerPopup(true, "")
                } catch (e: Exception) {
                    showServerPopup(true, "")
                }

            }
        }
    }


    fun getLoadingScreenImage() {
        val token = tinyDB.getString("Cookie").toString()
        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                try {

                    val response = authRepository.getLoadingScreen(token)

                    println("SuccessResponse $response")



                    if (response != null) {
                        tinyDB.putString("loadingBG", response.loadingScreen ?: "")
                        getAvatar()
                    }

                } catch (e: ApiException) {
                    showServerPopup(true, "")
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()

                    showServerPopup(false, "")
                } catch (e: ResponseException) {
                    showServerPopup(true, "")
                    println("ErrorResponse")

                } catch (e: SocketException) {

                    Log.d("connection Exception", "Connect Not Available")
                    showServerPopup(true, "")
                } catch (e: Exception) {
                    showServerPopup(true, "")
                }
            }
        }


    }

    fun getAvatar() {
        val token = tinyDB.getString("Cookie").toString()
        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                try {
                    val user = tinyDB.getString("User")

                    val response = authRepository.getUserAvatar(user!!, token)

                    println("SuccessResponse $response")



                    if (response != null) {

                        tinyDB.putString("Avatar", response.avatar)

                        val intent = Intent(activityContext, CreateNewPasswordScreen::class.java)
                        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
                        (activityContext as OtpActivity).finish()


                    }

                } catch (e: ResponseException) {
                    showServerPopup(true, "")
                    e.printStackTrace()
                } catch (e: ApiException) {
                    showServerPopup(false, "")

                    e.printStackTrace()
                } catch (e: NoInternetException) {

                    println("position 2")
                    e.printStackTrace()
                    showServerPopup(false, "")
                } catch (e: SocketException) {

                    showServerPopup(true, "")
                    Log.d("connection Exception", "Connect Not Available")

                } catch (e: Exception) {
                    showServerPopup(true, "")
                }
            }
        }


    }


    private fun checkStateByServer(response: SigninResponse) {
        val check = response.lastVar!!.lastActivity
        tinyDB.putInt("selectedStateByServer", check!!)
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


        insertWorkAndBreakTimeInDataBase(response)


    }


    private fun getPreviousTime(response: SigninResponse) {

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
                    response.work!!.workBreak,
                    response
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
        insertWorkAndBreakTimeInDataBase(response)


    }


    //--------------------------------------------Utils------------------------------------------
    private suspend fun insertDataInTinyDataBase(response: SigninResponse, name: String) {
        tinyDB.putString("User", name)
        val lastUser = tinyDB.getString("LastUser")
        if (lastUser != null) {
            if (name.trim() != lastUser.trim()) {
                tinyDB.putString("WorkDate", "")
                tinyDB.putString("BreakDate", "")
                tinyDB.putString("LastUser", "")
            }

        }

        authRepository.InsertSigninData(response)
        Log.d("workinghour", "${response.lastVar?.lastWorkedHoursTotal}")
        tinyDB.putInt("lasttimework", response.lastVar?.lastWorkedHoursTotal ?: 0)
        tinyDB.putInt("lasttimebreak", response.lastVar?.lastWorkBreakTotal ?: 0)
        tinyDB.putInt("defaultWork", response.work?.workingHours ?: 0)
        tinyDB.putInt("defaultBreak", response.work?.workBreak ?: 0)
        tinyDB.putInt("lastVehicleid", response.lastVar?.lastIdVehicle?.id ?: 0)
        tinyDB.putString("loadingBG", response.images.loadingScreen ?: "")
        tinyDB.putString("SplashBG", response.images.splashScreen ?: "")
        val max = response.work!!.workBreak * 60
        println("Max Value from Server $max")
        tinyDB.putInt("MaxBreakBar", max)

        val maxWork = response.work.workingHours * 60
        println("Max Value from Server $maxWork")
        tinyDB.putInt("MaxBar", maxWork)

        if (response.lastVar!!.lastActivity != 3) {
            val state = response.lastVar.lastState!!
            tinyDB.putInt("state", state + 1)
        }

        if (response.colors.primary.isNotEmpty()) {
            ResendApis.primaryColor = response.colors.primary ?: "#7A59FC"
            ResendApis.secondaryColor = response.colors.secondary ?: "#653FFB"
            Log.d("COLORCHECKTESTING", response.colors.primary)
            tinyDB.putString("primaryColor", ResendApis.primaryColor)
            tinyDB.putString("secondrayColor", ResendApis.secondaryColor)
        }

        tinyDB.putString("User", name)
        MyApplication.check = 200
        val Language = response.profile?.language
        val notify: Boolean = response.profile?.notify!!
        tinyDB.putString("language", Language.toString())
        tinyDB.putBoolean("notify", notify)
    }

    private suspend fun showServerPopup(forServer: Boolean, message: String) {
        withContext(Dispatchers.Main) {
            MyApplication.authCheck = true
            LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup(forServer, message)

        }

    }

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }

    private fun getAndroidVersion(): String? {
        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        return "Android SDK: $sdkVersion ($release)"
    }

    private fun insertWorkAndBreakTimeInDataBase(response: SigninResponse) {
        var workStartTime = response.lastVar?.lastWorkedHoursDateIni
        var breakStartTime = response.lastVar?.lastWorkBreakDateIni
        Log.d("NEW_USER_DATA_TESTING", "is Here $breakStartTime ..... $workStartTime")

        if (workStartTime != null) {
            workStartTime = workStartTime.replace("T", ",")
            workStartTime = workStartTime.split(".").toTypedArray()[0]
            workStartTime += "Z"

            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    if (mainRepository.getUnsentStartWorkTimeDetails() != null) {
                        mainRepository.deleteAllUnsentStartWorkTime()
                    }
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
            breakStartTime += "Z"

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

    private fun getWorkTime(response: SigninResponse) {
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

    private fun otpEditTextAutoForward(binding: ActivityOtpBinding) {
        binding.edt1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s.toString().trim().isEmpty()) {
                    binding.edt2.requestFocus()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        binding.edt2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s.toString().trim().isEmpty()) {
                    binding.edt3.requestFocus()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        binding.edt3.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s.toString().trim().isEmpty()) {
                    binding.edt4.requestFocus()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        //otpScreen text back press
        binding.edt2.setOnKeyListener { _, _, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (event!!.action == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL
                || binding.edt2.text == null
            ) {
                //this is for backspace
                binding.edt1.requestFocus()

            }
            false
        }
        binding.edt3.setOnKeyListener { _, _, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (event!!.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DEL
                || binding.edt3.text == null
            ) {
                //this is for backspace
                binding.edt2.requestFocus()

            }
            false
        }
        binding.edt4.setOnKeyListener { _, _, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (event!!.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DEL
                || binding.edt4.text == null
            ) {
                //this is for backspace
                binding.edt3.requestFocus()

            }
            false
        }
    }

    private fun fromSplashToOtp() {
        if (fromSplash!!) {
            // time reset
            tinyDB.clear();
            val intent = Intent(activityContext, SplashScreen::class.java)
            (activityContext as OtpActivity).startActivity(intent)
            (activityContext as OtpActivity).finish()

        } else {
            (activityContext as Activity).finish()
        }

    }

    private fun convertErrorBody(responseString: Reader?): SigninResponse {
        val gson = Gson()
        val type = object : TypeToken<SigninResponse>() {}.type
        val errorResponse: SigninResponse? = gson.fromJson(responseString, type)
        return errorResponse!!
    }

    private fun formatSize(size: Long): String {
        println("orignal size $size")
        var memorySize = size
        var suffix: String? = null
        if (memorySize >= 1024) {
            suffix = "KB"
            memorySize /= 1024
            if (memorySize >= 1024) {
                suffix = "MB"
                memorySize /= 1024
                if (memorySize >= 1024) {
                    suffix = "GB"
                    memorySize /= 1024


                }
            }
        }
        val resultBuffer = StringBuilder(java.lang.Long.toString(memorySize))
        var commaOffset = resultBuffer.length - 3
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',')
            commaOffset -= 3
        }
        if (suffix != null) resultBuffer.append(suffix)

        return resultBuffer.toString()


    }

    private fun moveToLoadingScreen() {
        val intent = Intent(activityContext, LoadingScreen::class.java)
        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
    }

    private fun checkNetAndValidateDate(): Boolean {
        if (CheckConnection.netCheck(activityContext!!)) {
            viewModelScope.launch(Dispatchers.IO) {
                MyApplication.authCheck = true
            }
            moveToLoadingScreen()
            return true
        } else {
            Toast.makeText(
                activityContext,
                "Comprueba tu conexión a Internet",
                Toast.LENGTH_SHORT
            ).show()
        }
        return false
    }
}