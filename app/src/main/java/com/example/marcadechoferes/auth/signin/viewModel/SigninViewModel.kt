package com.example.marcadechoferes.auth.signin.viewModel

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
import com.example.marcadechoferes.BuildConfig
import com.example.marcadechoferes.Extra.K
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.forgotPassword.ForgotPasswordActivity
import com.example.marcadechoferes.auth.repository.AuthRepository
import com.example.marcadechoferes.auth.signin.SignInActivity
import com.example.marcadechoferes.databinding.ActivitySignInBinding
import com.example.marcadechoferes.loadingScreen.LoadingScreen
import com.example.marcadechoferes.mainscreen.MainActivity
import com.example.marcadechoferes.myApplication.MyApplication
import com.example.marcadechoferes.network.ApiException
import com.example.marcadechoferes.network.NoInternetException
import com.example.marcadechoferes.network.ResponseException
import com.example.marcadechoferes.network.signinResponse.SigninResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.Reader
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

@HiltViewModel
class SigninViewModel @Inject constructor(val authRepository: AuthRepository) : ViewModel() {
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
                val emailCheck: String = email.text.toString()
                val passwordCheck= editPassword.text.toString()
                val validater= emailCheck.isValidEmail()

                if(emailCheck.isEmpty()){
                    Toast.makeText(activityContext, "Enter Email", Toast.LENGTH_SHORT).show()
                }
                else{
                    if(validater==true && passwordCheck.length>=4){

                        signinAuth(emailCheck,passwordCheck)
                        var intent = Intent(activityContext,LoadingScreen::class.java)
                        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)

                    }else if(validater==false){
                        Toast.makeText(activityContext, "Invalid Email", Toast.LENGTH_SHORT).show()

                    }
                    else{
                        Toast.makeText(activityContext, "Invalid password", Toast.LENGTH_SHORT).show()

                    }
                }



            }


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
        var idApp: String? = BuildConfig.APPLICATION_ID
        var memUsed: String? = usedSpace
        var diskFree: String? = iAvailableSpace
        var diskTotal: String? =iTotalSpace
        var model: String? = Build.MODEL
        var operatingSystem: String? = "android"
        var osVersion: String? = getAndroidVersion()
        var appVersion: String? = "15"
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
                        if(response.lastVar.lastActivity != 3){
                            var state=response.lastVar.lastState!!
                            tinyDB.putInt("state", state+1)
                        }



                        if(response.colors.primary.isNotEmpty()){
                            K.primaryColor=response.colors.primary ?: "#7A59FC"
                            K.secondrayColor = response.colors.secondary ?: "#653FFB"
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
                        Log.d("LoadingImage","Before")
                        getLoadingScreenImage()

                    }
                } catch (e: ResponseException) {
                    val response = convertErrorBody(e.response)


                    withContext(Dispatchers.Main){
                        (activityContext as SignInActivity).finish()
                        Toast.makeText(activityContext, "Error de inicio de sesion", Toast.LENGTH_SHORT).show()
                        var intent = Intent(activityContext, SignInActivity::class.java)
                        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)

                    }
                    println("ErrorResponse $response")
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
}



