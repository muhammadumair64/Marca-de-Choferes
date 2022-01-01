package com.example.marcadechoferes.auth.otp.viewModel

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.example.marcadechoferes.BuildConfig
import com.example.marcadechoferes.Extra.K

import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.auth.createpassword.CreateNewPasswordScreen
import com.example.marcadechoferes.auth.otp.OTP_Activity
import com.example.marcadechoferes.auth.repository.AuthRepository
import com.example.marcadechoferes.databinding.ActivityOtpBinding
import com.example.marcadechoferes.loadingScreen.LoadingScreen
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
import javax.inject.Inject

@HiltViewModel
class OTPViewModel @Inject constructor(val authRepository: AuthRepository) : ViewModel() {
    var activityContext: Context? = null
    lateinit var tinyDB: TinyDB
    fun viewsForOTPScreen(context: Context, binding: ActivityOtpBinding) {
        activityContext = context
        tinyDB = TinyDB(context)

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
                var user = tinyDB.getString("User")
                otpAuth(user!!, otp.toInt())
                var intent = Intent(activityContext, LoadingScreen::class.java)
                ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
            }

        }

        //Back button
        binding.backButton.setOnClickListener {
            (context as Activity).finish()
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
        var idApp: String? = BuildConfig.APPLICATION_ID
        var memUsed: String? = usedSpace
        var diskFree: String? = iAvailableSpace
        var diskTotal: String? = iTotalSpace
        var model: String? = Build.MODEL
        var operatingSystem: String? = "android"
        var osVersion: String? = getAndroidVersion()
        var appVersion: String? = "15"
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
                        authRepository.InsertSigninData(response)
                        Log.d("workinghour","${response.lastVar?.lastWorkedHoursTotal}")
                        tinyDB.putInt("lasttimework", response.lastVar?.lastWorkedHoursTotal ?: 0)
                        tinyDB.putInt("lasttimebreak", response.lastVar?.lastWorkBreakTotal ?: 0)
                        tinyDB.putInt("defaultWork", response.work?.workingHours ?: 0)
                        tinyDB.putInt("defaultBreak", response.work?.workBreak ?: 0)
                        tinyDB.putInt("lastVehicleid", response.lastVar?.lastIdVehicle?.id ?: 0)
                        tinyDB.putString("loadingBG",response.images.loadinScreen ?: "")
                        tinyDB.putString("SplashBG",response.images.splashScreen ?: "")

                        if(response.lastVar!!.lastActivity != 3){
                            var state=response.lastVar.lastState!!
                            tinyDB.putInt("state", state+1)
                        }

                        if(response.colors.primary.isNotEmpty()){
                            K.primaryColor=response.colors.primary ?: "#7A59FC"
                            K.secondrayColor = response.colors.secondary ?: "#653FFB"
                        }
                        tinyDB.putString("User",userName)
                        MyApplication.check=200
                        val Language = response.profile?.language
                        val notify: Boolean = response.profile?.notify!!
                        tinyDB.putString("language", Language.toString())
                        tinyDB.putBoolean("notify", notify)
                        checkStateByServer(response)
                        getLoadingScreenImage()

                    }
                } catch (e: ResponseException) {
                    tinyDB.putString("User", "")
                    val response = convertErrorBody(e.response)
                    withContext(Dispatchers.Main){
                        (activityContext as OTP_Activity).finish()
                        var intent = Intent(activityContext, OTP_Activity::class.java)
                        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
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
                            "Check Your Internet Connection",
                            Toast.LENGTH_SHORT
                        ).show()
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
                            "Check Your Internet Connection",
                            Toast.LENGTH_SHORT
                        ).show()
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

}