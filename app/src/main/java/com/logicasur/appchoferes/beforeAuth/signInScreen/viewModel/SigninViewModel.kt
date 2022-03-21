package com.logicasur.appchoferes.beforeAuth.signInScreen.viewModel

import android.annotation.SuppressLint
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
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logicasur.appchoferes.utils.ResendApis
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.beforeAuth.forgotPasswordScreen.ForgotPasswordActivity
import com.logicasur.appchoferes.data.repository.AuthRepository
import com.logicasur.appchoferes.beforeAuth.signInScreen.SignInActivity
import com.logicasur.appchoferes.common.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.afterAuth.mainscreen.MainActivity
import com.logicasur.appchoferes.utils.myApplication.MyApplication
import com.logicasur.appchoferes.data.network.ApiException
import com.logicasur.appchoferes.data.network.NoInternetException
import com.logicasur.appchoferes.data.network.ResponseException
import com.logicasur.appchoferes.data.network.signinResponse.SigninResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.logicasur.appchoferes.Extra.CheckConnection
import com.logicasur.appchoferes.Extra.TimeCalculator
import com.logicasur.appchoferes.common.serverCheck.ServerCheck
import com.logicasur.appchoferes.databinding.ActivitySignInBinding
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
class SigninViewModel @Inject constructor(val authRepository: AuthRepository, val tinyDB: TinyDB,
                                          val mainRepository: MainRepository,
                                          val resendApis: ResendApis, val timerCalculator: TimeCalculator, val serverCheck: ServerCheck
) : ViewModel() {
    lateinit var activityContext: Context
    var Token=""


    fun viewsOfActivitySign(context: Context, binding: ActivitySignInBinding) {
        activityContext = context
        binding.apply {
            showPassBtn.setOnClickListener {
                hideOrUnhidePassword(showPassBtn,editPassword)
            }

            forgotPassword.setOnClickListener {
                moveToForgetPassword()
            }

            signInBtn.setOnClickListener {

                if(validateEmailAndPassword(email,editPassword)){
                    if(checkDeviceNetActiveAndNotify()){
                        viewModelScope.launch(Dispatchers.IO) {
                            MyApplication.authCheck = true
                            sigInAuthApi(email.text.toString().trim(),editPassword.text.toString().trim())
                        }
                        showLoadingScreen()
                    }

                }

            }

            logToken()


        }
    }

    private fun showLoadingScreen() {
        val intent = Intent(activityContext,LoadingScreen::class.java)
        ContextCompat.startActivity(activityContext, intent, Bundle.EMPTY)
    }

    private fun moveToForgetPassword() {
        val intent = Intent(activityContext, ForgotPasswordActivity::class.java)
        ContextCompat.startActivity(activityContext, intent, Bundle.EMPTY)
    }


    @SuppressLint("HardwareIds")
    private fun sigInAuthApi(userName:String, userPassword:String) {
        val iPath: File = Environment.getDataDirectory()
        val iStat = StatFs(iPath.path)
        val iBlockSize = iStat.blockSizeLong
        val iAvailableBlocks = iStat.availableBlocksLong
        val iTotalBlocks = iStat.blockCountLong
        val iAvailableSpace = formatSize(iAvailableBlocks * iBlockSize)
        val iTotalSpace = formatSize(iTotalBlocks * iBlockSize)
        var unUsed =(iTotalBlocks * iBlockSize)-(iAvailableBlocks * iBlockSize)
        val usedSpace= formatSize(unUsed)
        
        val name = userName
        val password = userPassword
        val idApp: String? = MyFirebaseMessagingService.getToken(activityContext)
        val memUsed: String = usedSpace
        val diskFree: String = iAvailableSpace
        val diskTotal: String =iTotalSpace
        val model: String? = Build.MODEL
        val operatingSystem = "android"
        val osVersion: String? = getAndroidVersion()
        val appVersion = "5"
        val appBuild: String? =  Build.ID
        val platform = "Android"
        val manufacturer: String? = Build.MANUFACTURER
        val uuid: String? = Settings.Secure.getString(
            activityContext.getContentResolver(),
            Settings.Secure.ANDROID_ID
        )

        val isVirtual: String = isEmulator().toString()




        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                try {

                    val response =
                        authRepository.userSignin(
                            name, password, idApp!!,
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
                            isVirtual
                        )
                    authRepository.InsertSigninData(response)
                    saveDataInTinyDataBase(response,userName)
                    checkStateByServer(response)
                    Token = tinyDB.getString("Cookie").toString()
                    Log.d("TOKENTESTING", Token)
                    getPreviousTime(response)
                    getLoadingScreenImage()

                } catch (e: ResponseException) {



                    withContext(Dispatchers.Main){
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup(false)

                    }
                    println("ErrorResponse ${e.localizedMessage}")
                }
                catch (e: ApiException) {
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup(false)
                    }
                    e.printStackTrace()
                }
                catch (e: NoInternetException) {

                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main){
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup(false)
                    }
                }
                catch(e: SocketException){

                    Log.d("connection Exception","Connect Not Available")
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup(false)
                    }
                }
                catch(e: Exception){
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup(false)
                    }
                    Log.d("connection Exception", "Connect Not Available")
                }
            }
        }
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
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup(false)
                    }
                    Log.d("LoadingImage","API EXCEPTION ${e.localizedMessage}")
                }
                catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup(false)
                    }
                    Log.d("LoadingImage","No Internet EXCEPTION ${e.localizedMessage}")
                }
                catch (e: ResponseException) {
                    println("ErrorResponse")
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup(false)
                    }
                    Log.d("LoadingImage","Response Exception ${e.localizedMessage}")

                }
                catch(e: SocketException){
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup(false)
                    }
                    Log.d("connection Exception","Connect Not Available")
                }
                catch(e:Exception){
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup(false)
                    }
                    Log.d("connection Exception", "Connect Not Available")
                }




    }



    fun getAvatar(){
        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                try {
                   val user=tinyDB.getString("User")

                    val response = authRepository.getUserAvatar(user!!,Token)

                    println("SuccessResponse $response")



                    if(response!=null) {

                        tinyDB.putString("Avatar",response.avatar)

                        var intent = Intent(activityContext,MainActivity::class.java)
                        ContextCompat.startActivity(activityContext, intent, Bundle.EMPTY)
                        (activityContext as SignInActivity).finish()


                    }

                }
                catch (e: ApiException) {
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup(false)
                    }
                    e.printStackTrace()
                }
              catch (e: NoInternetException) {

                println("position 2")
                     e.printStackTrace()

                  withContext(Dispatchers.Main){
                      MyApplication.authCheck = true
                      LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup(false)
                  }
                     }
                catch (e: ResponseException) {
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup(false)
                    }
                    println("ErrorResponse")
                }
                catch(e: SocketException){

                    Log.d("connection Exception","Connect Not Available")
                    withContext(Dispatchers.Main){
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup(false)
                    }
                }

            }
        }


    }






    private fun checkStateByServer(response: SigninResponse) {
        val check = response.lastVar!!.lastActivity
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
                    breakDate = breakDate.replace("T"," ")
                    breakDate = breakDate.replace("-","/")
                    Log.d("workDate Is", "date is $breakDate")
                }
                tinyDB.putString("goBackTime", breakDate)
                tinyDB.putInt("ServerBreakTime", response.lastVar.lastWorkBreakTotal!!)
                timerCalculator.timeDifference(tinyDB, activityContext, false, response.work!!.workBreak,response)

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
            workStartTime= workStartTime.replace("T",",")
            workStartTime= workStartTime.split(".").toTypedArray()[0]
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
        if(breakStartTime != null){
            breakStartTime= breakStartTime.replace("T",",")
            breakStartTime= breakStartTime.split(".").toTypedArray()[0]
            breakStartTime = breakStartTime+"Z"

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




    // -------------------------- Utils --------------------------------


    private fun saveDataInTinyDataBase(response: SigninResponse, userName: String){
        tinyDB.putBoolean("NOSPLASH",true)
        val lastUser= tinyDB.getString("LastUser")
        if(lastUser != null){
            if(userName.trim() != lastUser.trim()){
                tinyDB.putString("WorkDate","")
                tinyDB.putString("BreakDate","")
                tinyDB.putString("LastUser","")
            }

        }
        tinyDB.putInt("lasttimework", response.lastVar!!.lastWorkedHoursTotal!!)
        tinyDB.putInt("lasttimebreak", response.lastVar.lastWorkBreakTotal!!)
        tinyDB.putInt("defaultWork",response.work!!.workingHours)
        tinyDB.putInt("defaultBreak",response.work.workBreak)
        tinyDB.putInt("lastVehicleid", response.lastVar.lastIdVehicle!!.id!!)
        val max = response.work.workBreak * 60
        println("Max Value from Server $max")
        tinyDB.putInt("MaxBreakBar",max)
        val maxWork = response.work.workingHours * 60
        println("Max Value from Server $maxWork")
        tinyDB.putInt("MaxBar",maxWork)

        if(response.lastVar.lastActivity != 3){
            val state=response.lastVar.lastState!!
            tinyDB.putInt("state", state+1)
        }
        if (response.colors.primary.isNotEmpty()) {
            ResendApis.primaryColor = response.colors.primary
            ResendApis.secondaryColor = response.colors.secondary
            tinyDB.putString("primaryColor", ResendApis.primaryColor)
            tinyDB.putString("secondrayColor", ResendApis.secondaryColor)
        }

        tinyDB.putString("User",userName)
        MyApplication.check=200
        val temp=  tinyDB.getString("User")
        Log.d("total time ","tem $temp")
        val Language =response.profile?.language
        val notify:Boolean =response.profile?.notify!!
        tinyDB.putString("language", Language.toString())
        tinyDB.putBoolean("notify",notify)
    }

    private fun checkDeviceNetActiveAndNotify(): Boolean{
        if(!CheckConnection.netCheck(activityContext)){
            Toast.makeText(activityContext,"Comprueba tu conexión a Internet" , Toast.LENGTH_SHORT).show()
        }
        else{
           return true
        }
        return false
    }

    private fun validateEmailAndPassword(emailET: EditText,passwordET : EditText) : Boolean{
        val emailCheck: String = emailET.text.toString().trim()
        val passwordCheck= passwordET.text.toString()
        val validator= emailCheck.isValidEmail()

        if(emailCheck.isEmpty()){
            Toast.makeText(activityContext, "Enter Email", Toast.LENGTH_SHORT).show()
        }
        else{
            if(validator && passwordCheck.length>=4){
                Log.d("EmailTesting","SIGN IN EMAIL $emailCheck and password is $passwordCheck")
                return true
            }else if(!validator){
                Toast.makeText(activityContext, "Invalid Email", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(activityContext, "Invalid password", Toast.LENGTH_SHORT).show()

            }
        }

       return false
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
    private fun convertErrorBody(responseString: Reader?): SigninResponse {
        val gson = Gson()
        val type = object : TypeToken<SigninResponse>() {}.type
        val errorResponse: SigninResponse? = gson.fromJson(responseString, type)
        return errorResponse!!
    }

    fun getAndroidVersion(): String? {
        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        return "Android SDK: $sdkVersion ($release)"
    }

    private fun formatSize(size: Long): String {
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

    private fun String.isValidEmail(): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    private fun hideOrUnhidePassword(iconImage : ImageView, editText: EditText){

        if (editText.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            editText.setSelection(editText.text.length)
            iconImage.setImageResource(R.drawable.hide_password)
        } else {
            Log.d("SIGNINSCREEN","In Else block")
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance())
            editText.setSelection(editText.getText().length)
            iconImage.setImageResource(R.drawable.ic_icon_visibility)
        }
    }




    private fun getWorkTime(response: SigninResponse) {
        Log.d("NEGATIVE_TESTING", "in function 3")
        tinyDB.putString("checkTimer", "workTime")
        var workDate = response.lastVar!!.lastWorkedHoursDateIni
        if (workDate!!.isNotEmpty()) {
            workDate = workDate.split(".").toTypedArray()[0]
            workDate = workDate.replace("T"," ")
            workDate = workDate.replace("-","/")
            Log.d("workDate Is", "date is $workDate")
        }
        tinyDB.putString("goBackTime", workDate)
        timerCalculator.timeDifference(tinyDB, activityContext, false, response.work!!.workBreak,response)
    }

    private fun logToken(){
        Log.d("FCM_TOKEN_","${MyFirebaseMessagingService.getToken(activityContext)}")
    }
}



