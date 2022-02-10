package com.logicasur.appchoferes.auth.otp.viewModel

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
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logicasur.appchoferes.BuildConfig
import com.logicasur.appchoferes.Extra.K

import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.auth.createpassword.CreateNewPasswordScreen
import com.logicasur.appchoferes.auth.otp.OTP_Activity
import com.logicasur.appchoferes.auth.repository.AuthRepository
import com.logicasur.appchoferes.databinding.ActivityOtpBinding
import com.logicasur.appchoferes.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.ApiException
import com.logicasur.appchoferes.network.NoInternetException
import com.logicasur.appchoferes.network.ResponseException
import com.logicasur.appchoferes.network.signinResponse.SigninResponse
import com.logicasur.appchoferes.splashscreen.SplashScreen
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.logicasur.appchoferes.mainscreen.repository.MainRepository
import com.logicasur.appchoferes.network.unsentApis.UnsentStartBreakTime
import com.logicasur.appchoferes.network.unsentApis.UnsentStartWorkTime
import com.logicasur.appchoferes.utils.MyFirebaseMessagingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.Reader
import java.net.SocketException
import java.util.*
import javax.inject.Inject

@HiltViewModel
class OTPViewModel @Inject constructor(val authRepository: AuthRepository,val mainRepository: MainRepository) : ViewModel() {
    var activityContext: Context? = null
    lateinit var tinyDB: TinyDB
     var fromSplash :Boolean? = null;
    fun viewsForOTPScreen(context: Context, binding: ActivityOtpBinding) {
        activityContext = context
        tinyDB = TinyDB(context)
        binding.arrowBack.setBackgroundColor(Color.parseColor(K.primaryColor))
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

        //otp text back press
        binding.edt2.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                if (event!!.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL
                    || binding.edt2.text == null
                ) {
                    //this is for backspace
                    binding.edt1.requestFocus()

                }
                return false
            }
        })
        binding.edt3.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                if (event!!.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL
                    || binding.edt3.text == null
                ) {
                    //this is for backspace
                    binding.edt2.requestFocus()

                }
                return false
            }
        })
        binding.edt4.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                if (event!!.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL
                    || binding.edt4.text == null
                ) {
                    //this is for backspace
                    binding.edt3.requestFocus()

                }
                return false
            }
        })

        //Submit Button
        binding.SubmitButton.setOnClickListener {
            if (binding.edt1.text.toString() != "" && binding.edt1.text.toString() != "" && binding.edt1.text.toString() != "" && binding.edt1.text.toString() != "") {
                var otp =
                    "${binding.edt1.text}${binding.edt2.text}${binding.edt3.text}${binding.edt4.text}".trim()
                println("otp is ${otp.toInt()}")
                var user = tinyDB.getString("UserOTP")
                otpAuth(user!!, otp.toInt())
                var intent = Intent(activityContext, LoadingScreen::class.java)
                ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
            }

        }

        //Back button
        binding.backButton.setOnClickListener {
            if(fromSplash != null){
                fromSplashToOtp()
            }else{
                (context as Activity).finish()
            }

        }

    }


    fun otpAuth(userName: String, otp: Int) {
        val iPath: File = Environment.getDataDirectory()
        val iStat = StatFs(iPath.path)
        val iBlockSize = iStat.blockSizeLong
        val iAvailableBlocks = iStat.availableBlocksLong
        val iTotalBlocks = iStat.blockCountLong
        val iAvailableSpace = formatSize(iAvailableBlocks * iBlockSize)
        val iTotalSpace = formatSize(iTotalBlocks * iBlockSize)
        var unUsed = (iTotalBlocks * iBlockSize) - (iAvailableBlocks * iBlockSize)
        val usedSpace = formatSize(unUsed)


        var name = userName
        var idApp: String? = MyFirebaseMessagingService.getToken(activityContext!!)
        var memUsed: String? = usedSpace
        var diskFree: String? = iAvailableSpace
        var diskTotal: String? = iTotalSpace
        var model: String? = Build.MODEL
        var operatingSystem: String? = "android"
        var osVersion: String? = getAndroidVersion()
        var appVersion: String? = "7"
        var appBuild: String? = Build.ID
        var platform: String? = "Android"
        var manufacturer: String? = Build.MANUFACTURER
        var uuid: String? = Settings.Secure.getString(
            activityContext?.getContentResolver(),
            Settings.Secure.ANDROID_ID
        )

        var isVirtual: String? = isEmulator().toString()




        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                try {
                    var Token = tinyDB.getString("Cookie").toString()
                    Log.d("UserName","$name")
                    val response =
                        authRepository.otp(
                            otp, name, idApp!!,
                            memUsed!!,
                            diskFree!!,
                            diskTotal!!,
                            model!!,
                            operatingSystem!!,
                            osVersion!!,
                            appVersion!!,
                            appBuild!!,
                            platform!!,
                            manufacturer!!,
                            uuid!!,
                            isVirtual!!,
                            Token
                        )

                    println("SuccessResponse OTP $response")

                    if (response != null ) {
                        tinyDB.putString("User",name)
                        authRepository.InsertSigninData(response)
                        Log.d("workinghour","${response.lastVar?.lastWorkedHoursTotal}")
                        tinyDB.putInt("lasttimework", response.lastVar?.lastWorkedHoursTotal ?: 0)
                        tinyDB.putInt("lasttimebreak", response.lastVar?.lastWorkBreakTotal ?: 0)
                        tinyDB.putInt("defaultWork", response.work?.workingHours ?: 0)
                        tinyDB.putInt("defaultBreak", response.work?.workBreak ?: 0)
                        tinyDB.putInt("lastVehicleid", response.lastVar?.lastIdVehicle?.id ?: 0)
                        tinyDB.putString("loadingBG",response.images.loadingScreen ?: "")
                        tinyDB.putString("SplashBG",response.images.splashScreen ?: "")
                        var max = response.work!!.workBreak * 60
                        println("Max Value from Server $max")
                        tinyDB.putInt("MaxBreakBar",max)

                        var maxWork = response.work!!.workingHours * 60
                        println("Max Value from Server $maxWork")
                        tinyDB.putInt("MaxBar",maxWork)

                        if(response.lastVar!!.lastActivity != 3){
                            var state=response.lastVar.lastState!!
                            tinyDB.putInt("state", state+1)
                        }

                        if (response.colors.primary.isNotEmpty()) {
                            K.primaryColor = response.colors.primary ?: "#7A59FC"
                            K.secondrayColor = response.colors.secondary ?: "#653FFB"
                            Log.d("COLORCHECKTESTING",response.colors.primary )
                            tinyDB.putString("primaryColor",K.primaryColor)
                            tinyDB.putString("secondrayColor",K.secondrayColor)
                        }

                        tinyDB.putString("User",userName)
                        MyApplication.check=200
                        val Language = response.profile?.language
                        val notify: Boolean = response.profile?.notify!!
                        tinyDB.putString("language", Language.toString())
                        tinyDB.putBoolean("notify", notify)
                        getPreviousTime(response)
                        checkStateByServer(response)
                        getLoadingScreenImage()

                    }
                } catch (e: ResponseException) {
                    tinyDB.putString("User", "")
                    val response = convertErrorBody(e.response)
                    withContext(Dispatchers.Main){
                    LoadingScreen.onEndLoadingCallbacks?.endLoading()
//                        (activityContext as OTP_Activity).finish()
//                        var intent = Intent(activityContext, OTP_Activity::class.java)
//                        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
                        Toast.makeText(
                            activityContext,
                            "OTP no válida",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    println("ErrorResponse $response")
                } catch (e: ApiException) {
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            activityContext,
                            "verifica tu conexión de red",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                catch(e: SocketException){
                    LoadingScreen.onEndLoadingCallbacks?.endLoading()
                    Log.d("connection Exception","Connect Not Available")
                    withContext(Dispatchers.Main){
                        Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }



    fun fromSplashToOtp(){
        if(fromSplash!!){
             // time reset
            tinyDB.clear();
            val intent  =  Intent(activityContext,SplashScreen::class.java)
            (activityContext as OTP_Activity).startActivity(intent)
            (activityContext as OTP_Activity).finish()

        }else{
            (activityContext as Activity).finish()
        }

    }

    fun convertErrorBody(responseString: Reader?): SigninResponse {
        val gson = Gson()
        val type = object : TypeToken<SigninResponse>() {}.type
        val errorResponse: SigninResponse? = gson.fromJson(responseString, type)
        return errorResponse!!
    }

    private fun formatSize(size: Long): String? {
        println("orignal size $size")
        var size = size
        var suffix: String? = null
        if (size >= 1024) {
            suffix = "KB"
            size /= 1024
            if (size >= 1024) {
                suffix = "MB"
                size /= 1024
                if (size >= 1024) {
                    suffix = "GB"
                    size /= 1024


                }
            }
        }
        val resultBuffer = StringBuilder(java.lang.Long.toString(size))
        var commaOffset = resultBuffer.length - 3
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',')
            commaOffset -= 3
        }
        if (suffix != null) resultBuffer.append(suffix)

        return resultBuffer.toString()


    }




    fun getLoadingScreenImage(){
        var Token = tinyDB.getString("Cookie").toString()
        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                try {

                    val response = authRepository.getLoadingScreen(Token)

                    println("SuccessResponse $response")



                    if(response!=null) {
                        tinyDB.putString("loadingBG",response.loadingScreen ?: "")
                        getAvatar()
                    }

                }
                catch (e: ApiException) {
                    e.printStackTrace()
                }
                catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    LoadingScreen.onEndLoadingCallbacks?.endLoading()
                    withContext(Dispatchers.Main){
                        Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: ResponseException) {
                    println("ErrorResponse")

                }
                catch(e: SocketException){
                    LoadingScreen.onEndLoadingCallbacks?.endLoading()
                    Log.d("connection Exception","Connect Not Available")
                    withContext(Dispatchers.Main){
                        Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }

    fun getAvatar() {
        var Token = tinyDB.getString("Cookie").toString()
        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                try {
                    var user = tinyDB.getString("User")

                    val response = authRepository.getUserAvatar(user!!, Token)

                    println("SuccessResponse $response")



                    if (response != null) {

                        tinyDB.putString("Avatar", response.avatar)

                        var intent = Intent(activityContext, CreateNewPasswordScreen::class.java)
                        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
                        (activityContext as OTP_Activity).finish()


                    }

                } catch (e: ResponseException) {
                    withContext(Dispatchers.Main){

                    }
                    e.printStackTrace()
                } catch (e: ApiException) {
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            activityContext,
                            "Comprueba tu conexión a Internet",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                catch(e: SocketException){
                    LoadingScreen.onEndLoadingCallbacks?.endLoading()
                    Log.d("connection Exception","Connect Not Available")
                    withContext(Dispatchers.Main){
                        Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }

    fun getAndroidVersion(): String? {
        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        return "Android SDK: $sdkVersion ($release)"
    }

    private fun checkStateByServer(response: SigninResponse) {
        var check = response.lastVar!!.lastActivity
        tinyDB.putInt("selectedStateByServer", check!!)
        when(check){
            0->{
                tinyDB.putString("selectedState","goToActiveState")

            }
            1->{
                tinyDB.putString("selectedState","goTosecondState")
            }
            2->{
                tinyDB.putString("selectedState","goToActiveState")

            }
            3->{
                tinyDB.putString("selectedState","endDay")
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
                tinyDB.putString("checkTimer", "workTime")
                var workDate = response.lastVar!!.lastWorkedHoursDateIni
                if (workDate!!.isNotEmpty()) {
                    workDate = workDate!!.split(".").toTypedArray()[0]
                    workDate = workDate!!.split("T").toTypedArray()[1]
                    Log.d("TimeOfLastWork", "date is $workDate")
                }
                tinyDB.putString("goBackTime", workDate)
                K.timeDifference(tinyDB, activityContext!!, false,response.work!!.workBreak)
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
            }
            2 -> {
                MyApplication.dayEndCheck = 100
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
            3->{
                MyApplication.dayEndCheck = 200
            }
        }


    }


}