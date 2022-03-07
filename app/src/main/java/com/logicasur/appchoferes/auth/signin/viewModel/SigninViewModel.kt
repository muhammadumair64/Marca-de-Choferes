package com.logicasur.appchoferes.auth.signin.viewModel

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logicasur.appchoferes.Extra.ResendApis
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.auth.forgotPassword.ForgotPasswordActivity
import com.logicasur.appchoferes.auth.repository.AuthRepository
import com.logicasur.appchoferes.auth.signin.SignInActivity
import com.logicasur.appchoferes.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.mainscreen.MainActivity
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.ApiException
import com.logicasur.appchoferes.network.NoInternetException
import com.logicasur.appchoferes.network.ResponseException
import com.logicasur.appchoferes.network.signinResponse.SigninResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.logicasur.appchoferes.Extra.CheckConnection
import com.logicasur.appchoferes.Extra.TimeCalculator
import com.logicasur.appchoferes.Extra.serverCheck.ServerCheck
import com.logicasur.appchoferes.databinding.ActivitySignInBinding
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
import java.lang.Exception
import java.net.SocketException
import javax.inject.Inject

@HiltViewModel
class SigninViewModel @Inject constructor(val authRepository: AuthRepository,
                                          val mainRepository: MainRepository,
                                          val resendApis: ResendApis,val timerCalculator: TimeCalculator,    val serverCheck: ServerCheck
) : ViewModel() {
    var activityContext: Context? = null
    lateinit var tinyDB: TinyDB
    var Token=""


    fun viewsOfActivitySignin(context: Context, binding: ActivitySignInBinding) {
        tinyDB= TinyDB(context)
        activityContext = context
        binding.apply {
            showPassBtn.setOnClickListener {
                if (editPassword.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
                    editPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                    editPassword.setSelection(editPassword.getText().length);
                    showPassBtn.setImageResource(R.drawable.hide_password)
                } else {
                    editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance())
                       editPassword.setSelection(editPassword.getText().length);
                    showPassBtn.setImageResource(R.drawable.ic_icon_visibility)
                }


            }

            forgotPassword.setOnClickListener {
                var intent = Intent(context, ForgotPasswordActivity::class.java)
                ContextCompat.startActivity(context, intent, Bundle.EMPTY)

            }

            signInBtn.setOnClickListener {
                var emailCheck: String = email.text.toString()
                val passwordCheck= editPassword.text.toString()
                emailCheck = emailCheck.trim()
                val validater= emailCheck.isValidEmail()

                if(emailCheck.isEmpty()){
                    Toast.makeText(activityContext, "Enter Email", Toast.LENGTH_SHORT).show()
                }
                else{
                    if(validater==true && passwordCheck.length>=4){
                        if(CheckConnection.netCheck(context)){
                            viewModelScope.launch(Dispatchers.IO) {
                                MyApplication.authCheck = true
//                                serverCheck.serverCheck {
//                                    signinAuth(emailCheck,passwordCheck)
//                                }
                                signinAuth(emailCheck.trim(),passwordCheck)
                                Log.d("EmailTesting","SIGN IN EMAIL ${emailCheck.trim()}")
                            }
                            var intent = Intent(activityContext,LoadingScreen::class.java)
                            ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
                        }
                       else{
                            Toast.makeText(activityContext,"Comprueba tu conexión a Internet" , Toast.LENGTH_SHORT).show()
                        }

                    }else if(validater==false){
                        Toast.makeText(activityContext, "Invalid Email", Toast.LENGTH_SHORT).show()

                    }
                    else{
                        Toast.makeText(activityContext, "Invalid password", Toast.LENGTH_SHORT).show()

                    }
                }



            }

            getToken()


        }
    }


    fun signinAuth(userName:String,userPassword:String) {
        val iPath: File = Environment.getDataDirectory()
        val iStat = StatFs(iPath.path)
        val iBlockSize = iStat.blockSizeLong
        val iAvailableBlocks = iStat.availableBlocksLong
        val iTotalBlocks = iStat.blockCountLong
        val iAvailableSpace = formatSize(iAvailableBlocks * iBlockSize)
        val iTotalSpace = formatSize(iTotalBlocks * iBlockSize)
        var unUsed =(iTotalBlocks * iBlockSize)-(iAvailableBlocks * iBlockSize)
        val usedSpace= formatSize(unUsed)





        var name = userName
        var password = userPassword
        var idApp: String? = MyFirebaseMessagingService.getToken(activityContext!!)
        var memUsed: String? = usedSpace
        var diskFree: String? = iAvailableSpace
        var diskTotal: String? =iTotalSpace
        var model: String? = Build.MODEL
        var operatingSystem: String? = "android"
        var osVersion: String? = getAndroidVersion()
        var appVersion: String? = "3"
        var appBuild: String? =  Build.ID
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

                    val response =
                        authRepository.userSignin(
                            name, password, idApp!!,
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
                            isVirtual!!
                        )



                    println("SuccessResponse $response")
                    authRepository.InsertSigninData(response)




                    if(response!=null) {
                        tinyDB.putInt("lasttimework", response.lastVar!!.lastWorkedHoursTotal!!)
                        tinyDB.putInt("lasttimebreak", response.lastVar!!.lastWorkBreakTotal!!)
                        tinyDB.putInt("defaultWork",response.work!!.workingHours)
                        tinyDB.putInt("defaultBreak",response.work.workBreak)
                        tinyDB.putInt("lastVehicleid", response.lastVar!!.lastIdVehicle!!.id!!)
                        var max = response.work.workBreak * 60
                        println("Max Value from Server $max")
                        tinyDB.putInt("MaxBreakBar",max)


                        var maxWork = response.work!!.workingHours * 60
                        println("Max Value from Server $maxWork")
                        tinyDB.putInt("MaxBar",maxWork)

                        if(response.lastVar.lastActivity != 3){
                            var state=response.lastVar.lastState!!
                            tinyDB.putInt("state", state+1)
                        }



                        if (response.colors.primary.isNotEmpty()) {
                            ResendApis.primaryColor = response.colors.primary ?: "#7A59FC"
                            ResendApis.secondaryColor = response.colors.secondary ?: "#653FFB"
                            tinyDB.putString("primaryColor",ResendApis.primaryColor)
                            tinyDB.putString("secondrayColor",ResendApis.secondaryColor)
                        }
                        
                        tinyDB.putString("User",userName)
                        MyApplication.check=200
                        var temp=  tinyDB.getString("User")
                        Log.d("total time ","tem $temp")
                        val Language =response.profile?.language
                        val notify:Boolean =response.profile?.notify!!
                        tinyDB.putString("language", Language.toString())
                        tinyDB.putBoolean("notify",notify)
                        checkStateByServer(response)
                        Token = tinyDB.getString("Cookie").toString()
                        Log.d("TOKENTESTING","$Token")
                        getPreviousTime(response)
                        getLoadingScreenImage()

                    }
                } catch (e: ResponseException) {
                    val response = convertErrorBody(e.response)


                    withContext(Dispatchers.Main){
//                        (activityContext as SignInActivity).finish()
//                        Toast.makeText(activityContext, "Error de inicio de sesion", Toast.LENGTH_SHORT).show()
//                        var intent = Intent(activityContext, SignInActivity::class.java)
//                        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)

                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()

                    }
                    println("ErrorResponse $response")
                }
                catch (e: ApiException) {
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
//                        Toast.makeText(
//                            activityContext,
//                            "Comprueba tu conexión a Internet" ,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                    e.printStackTrace()
                }
                catch (e: NoInternetException) {

                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main){
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
//                        Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
                    }
                }
                catch(e: SocketException){

                    Log.d("connection Exception","Connect Not Available")
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
//                        Toast.makeText(
//                            activityContext,
//                            "Comprueba tu conexión a Internet" ,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                }
                catch(e: Exception){
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
//                        Toast.makeText(
//                            activityContext,
//                            "Comprueba tu conexión a Internet" ,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                    Log.d("connection Exception", "Connect Not Available")
                }
            }
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

    fun String.isValidEmail(): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }


    suspend fun getLoadingScreenImage(){
        Log.d("LoadingImage","IN API")

                try {
                    val response = authRepository.getLoadingScreen(Token)
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
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
//                        Toast.makeText(
//                            activityContext,
//                            "Comprueba tu conexión a Internet" ,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                    Log.d("LoadingImage","API EXCEPTION ${e.localizedMessage}")
                }
                catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
//                        Toast.makeText(
//                            activityContext,
//                            "Comprueba tu conexión a Internet" ,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                    Log.d("LoadingImage","No Internet EXCEPTION ${e.localizedMessage}")

//                    withContext(Dispatchers.Main){
//                        Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
//                    }
                }
                catch (e: ResponseException) {
                    println("ErrorResponse")
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
//                        Toast.makeText(
//                            activityContext,
//                            "Comprueba tu conexión a Internet" ,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                    Log.d("LoadingImage","Response Exception ${e.localizedMessage}")

                }
                catch(e: SocketException){
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
//                        Toast.makeText(
//                            activityContext,
//                            "Comprueba tu conexión a Internet" ,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                    Log.d("connection Exception","Connect Not Available")
//                    withContext(Dispatchers.Main){
//                        Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
//                    }
                }
                catch(e:Exception){
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
//                        Toast.makeText(
//                            activityContext,
//                            "Comprueba tu conexión a Internet" ,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                    Log.d("connection Exception", "Connect Not Available")
                }




    }



    fun getAvatar(){
        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                try {
                   var user=tinyDB.getString("User")

                    val response = authRepository.getUserAvatar(user!!,Token)

                    println("SuccessResponse $response")



                    if(response!=null) {

                        tinyDB.putString("Avatar",response.avatar)

                        var intent = Intent(activityContext,MainActivity::class.java)
                        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
                        (activityContext as SignInActivity).finish()


                    }

                }
                catch (e: ApiException) {
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
//                        Toast.makeText(
//                            activityContext,
//                            "Comprueba tu conexión a Internet" ,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                    e.printStackTrace()
                }
              catch (e: NoInternetException) {

                println("position 2")
                     e.printStackTrace()

                  withContext(Dispatchers.Main){
                      MyApplication.authCheck = true
                      LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
//                      Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
                  }
                     }
                catch (e: ResponseException) {
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
//                        Toast.makeText(
//                            activityContext,
//                            "Comprueba tu conexión a Internet" ,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                    println("ErrorResponse")
                }
                catch(e: SocketException){

                    Log.d("connection Exception","Connect Not Available")
                    withContext(Dispatchers.Main){
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
//                        Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
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



    }

    private fun getPreviousTime(response: SigninResponse) {

        Log.d("NEGATIVE_TESTING", "in function")

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
                Log.d("NEGATIVE_TESTING", "in function2")
                getWorkTime(response)
            }
            1 -> {
                Log.d("NEGATIVE_TESTING", "in function break")
                tinyDB.putString("checkTimer", "breakTime")
                var breakDate = response.lastVar!!.lastWorkBreakDateIni
                if (breakDate!!.isNotEmpty()) {
                    breakDate = breakDate!!.split(".").toTypedArray()[0]
                    breakDate = breakDate!!.replace("T"," ")
                    breakDate = breakDate!!.replace("-","/")
                    Log.d("workDate Is", "date is $breakDate")
                }
                tinyDB.putString("goBackTime", breakDate)
                tinyDB.putInt("ServerBreakTime", response.lastVar.lastWorkBreakTotal!!)
                timerCalculator.timeDifference(tinyDB, activityContext!!, false, response.work!!.workBreak)

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
        Log.d("NEGATIVE_TESTING", "in function 3")
        tinyDB.putString("checkTimer", "workTime")
        var workDate = response.lastVar!!.lastWorkedHoursDateIni
        if (workDate!!.isNotEmpty()) {
            workDate = workDate!!.split(".").toTypedArray()[0]
            workDate = workDate!!.replace("T"," ")
            workDate = workDate!!.replace("-","/")
            Log.d("workDate Is", "date is $workDate")
        }
        tinyDB.putString("goBackTime", workDate)
        timerCalculator.timeDifference(tinyDB, activityContext!!, false, response.work!!.workBreak)
    }

    fun getToken(){
        var Token = MyFirebaseMessagingService.getToken(activityContext!!)
        Log.d("FCM_TOKEN_","$Token")

    }
}



